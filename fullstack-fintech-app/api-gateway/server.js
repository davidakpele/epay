const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const axios = require('axios');
const cors = require('cors');
const crypto = require('crypto');
const { URL } = require('url');
const multer = require('multer');
const FormData = require('form-data');
const { Tracer, BatchRecorder, jsonEncoder: { JSON_V2 } } = require('zipkin');
const { HttpLogger } = require('zipkin-transport-http');
const CLSContext = require('zipkin-context-cls');
const zipkinMiddleware = require('zipkin-instrumentation-express').expressMiddleware;

// ==================== Configuration ====================

const serviceName = 'node-api-service';
const zipkinBaseUrl = process.env.ZIPKIN_BASE_URL || 'http://localhost:9411'; 
const ctxImpl = new CLSContext('zipkin');
const recorder = new BatchRecorder({
  logger: new HttpLogger({
    endpoint: `${zipkinBaseUrl}/api/v2/spans`,
    jsonEncoder: JSON_V2
  })
});

const tracer = new Tracer({ ctxImpl, recorder, localServiceName: serviceName });
const PORT = process.env.PORT || 8292;
const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8000/v1';
const BACKEND_WS_URL = process.env.BACKEND_WS_URL || 'ws://localhost:8000/v1';
const RATE_LIMIT_PER_MINUTE = parseInt(process.env.RATE_LIMIT_PER_MINUTE || '100', 10);

// ==================== Custom Zipkin Axios Wrapper ====================
const upload = multer({ storage: multer.memoryStorage() });

function createTracedAxiosClient(axiosInstance, tracer, remoteServiceName) {
    const tracedClient = async (config) => {
        return tracer.scoped(() => {
            const traceId = tracer.createChildId();
            tracer.setId(traceId);
            
            // Record RPC call
            tracer.recordServiceName(serviceName);
            tracer.recordRpc('http.request');
            tracer.recordBinary('http.method', config.method?.toUpperCase() || 'GET');
            tracer.recordBinary('http.url', config.url);
            tracer.recordBinary('peer.service', remoteServiceName);
            tracer.recordAnnotation('cs'); 
            
            // Add B3 headers for trace propagation
            if (!config.headers) config.headers = {};
            config.headers['X-B3-TraceId'] = traceId.traceId;
            config.headers['X-B3-SpanId'] = traceId.spanId;
            config.headers['X-B3-ParentSpanId'] = traceId.parentId;
            config.headers['X-B3-Sampled'] = '1';
            
            const startTime = Date.now();
            
            return axiosInstance(config)
                .then(response => {
                    const duration = Date.now() - startTime;
                    tracer.recordBinary('http.status_code', response.status);
                    tracer.recordBinary('http.response_time_ms', duration);
                    tracer.recordAnnotation('cr'); // Client receive
                    
                    if (response.status >= 400) {
                        tracer.recordBinary('error', true);
                        tracer.recordBinary('http.error', `HTTP ${response.status}`);
                    }
                    
                    return response;
                })
                .catch(error => {
                    const duration = Date.now() - startTime;
                    tracer.recordBinary('http.response_time_ms', duration);
                    tracer.recordBinary('error', true);
                    tracer.recordBinary('error.message', error.message);
                    tracer.recordBinary('error.code', error.code || 'UNKNOWN');
                    tracer.recordAnnotation('cr'); // Client receive (with error)
                    throw error;
                });
        });
    };

    // Support both direct calls and method shortcuts
    tracedClient.get = (url, config = {}) => tracedClient({ ...config, method: 'get', url });
    tracedClient.post = (url, data, config = {}) => tracedClient({ ...config, method: 'post', url, data });
    tracedClient.put = (url, data, config = {}) => tracedClient({ ...config, method: 'put', url, data });
    tracedClient.patch = (url, data, config = {}) => tracedClient({ ...config, method: 'patch', url, data });
    tracedClient.delete = (url, config = {}) => tracedClient({ ...config, method: 'delete', url });
    tracedClient.head = (url, config = {}) => tracedClient({ ...config, method: 'head', url });
    tracedClient.options = (url, config = {}) => tracedClient({ ...config, method: 'options', url });

    return tracedClient;
}

// ==================== Security Classes ====================

class RateLimiter {
    constructor(maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.requests = new Map();
    }

    checkLimit(ip) {
        const now = Date.now();
        const oneMinuteAgo = now - 60000;
        
        if (!this.requests.has(ip)) {
            this.requests.set(ip, [now]);
            return true;
        }

        const timestamps = this.requests.get(ip).filter(ts => ts > oneMinuteAgo);
        
        if (timestamps.length >= this.maxRequestsPerMinute) {
            this.requests.set(ip, timestamps);
            return false;
        }

        timestamps.push(now);
        this.requests.set(ip, timestamps);
        return true;
    }

    cleanup() {
        const oneMinuteAgo = Date.now() - 60000;
        for (const [ip, timestamps] of this.requests.entries()) {
            const filtered = timestamps.filter(ts => ts > oneMinuteAgo);
            if (filtered.length === 0) {
                this.requests.delete(ip);
            } else {
                this.requests.set(ip, filtered);
            }
        }
    }
}

class SqlInjectionDetector {
    constructor() {
        this.patterns = [
            /(?:union\s+(?:all\s+)?select\s+.*from)/i,
            /(?:union\s+(?:all\s+)?select\s+null)/i,
            /;\s*(?:select|insert|update|delete|drop|create|alter|truncate|exec|execute|call|declare)\s/i,
            /(?:information_schema\.)/i,
            /(?:pg_catalog\.)/i,
            /(?:sys\.)/i,
            /(?:sleep|benchmark|waitfor|pg_sleep)\s*\(/i,
            /(?:load_file|into\s+(?:outfile|dumpfile))/i,
            /(?:extractvalue\s*\(|updatexml\s*\()/i,
        ];

        this.suspiciousKeywords = [
            'union', 'select', 'insert', 'update', 'delete', 'drop', 'create',
            'alter', 'truncate', 'exec', 'execute', 'shutdown', 'xp_', 'sp_',
            'information_schema', 'pg_catalog', 'sys', 'dual', 'version',
            'sleep', 'benchmark', 'waitfor', 'pg_sleep', 'load_file',
            'outfile', 'dumpfile', 'extractvalue', 'updatexml',
        ];

        this.maxSuspiciousScore = 5;
    }

    detect(input) {
        let score = 0;
        const reasons = [];
        const lowerInput = input.toLowerCase();

        for (const pattern of this.patterns) {
            if (pattern.test(lowerInput)) {
                score += 3;
                reasons.push(`Pattern match: ${pattern.source}`);
            }
        }

        for (const keyword of this.suspiciousKeywords) {
            const keywordPattern = new RegExp(`\\b${keyword}\\b`, 'i');
            if (keywordPattern.test(lowerInput)) {
                score += 1;
                reasons.push(`Suspicious keyword: ${keyword}`);
            }
        }

        if (lowerInput.includes("' or '") || lowerInput.includes('" or "')) {
            score += 2;
            reasons.push('OR injection pattern');
        }

        if (lowerInput.includes("' and '") || lowerInput.includes('" and "')) {
            score += 2;
            reasons.push('AND injection pattern');
        }

        if (lowerInput.includes('--') || lowerInput.includes('#') ||
            lowerInput.includes('/*') || lowerInput.includes('*/')) {
            score += 1;
            reasons.push('SQL comment sequence');
        }

        const specialChars = input.replace(/[a-zA-Z0-9\s]/g, '');
        const specialCharRatio = specialChars.length / input.length;
        if (specialCharRatio > 0.3 && input.length > 10) {
            score += 1;
            reasons.push('Excessive special characters');
        }

        const detected = score >= this.maxSuspiciousScore;
        return { detected, score, reason: reasons.join(', ') };
    }
}

class SecurityScanner {
    constructor() {
        this.sqlInjectionPatterns = [
            /(?:union\s+(?:all\s+)?select)/i,
            /(?:select\s+.*from\s+.*where\s+.*\d+\s*=\s*\d+)/i,
            /\s+or\s+['"]?\d+['"]?\s*=\s*['"]?\d+['"]?\s*--/i,
            /\s+and\s+['"]?\d+['"]?\s*=\s*['"]?\d+['"]?\s*--/i,
            /\s+(?:or|and)\s+['"]?1['"]?\s*=\s*['"]?1['"]?(?:\s*--|\s*#|\/\*)/i,
            /\s+(?:or|and)\s+['"]?1['"]?\s*=\s*['"]?0['"]?(?:\s*--|\s*#|\/\*)/i,
            /(?:union|select|insert|update|delete|drop)\s+.*(?:--|#|\/\*|\*\/)/i,
            /(?:benchmark\s*\(\s*\d+\s*,\s*.*\))/i,
            /(?:sleep\s*\(\s*\d+\s*\))/i,
            /(?:waitfor\s+delay\s+'[^']*')/i,
            /(?:pg_sleep\s*\(\s*\d+\s*\))/i,
            /;\s*(?:select|insert|update|delete|drop|create|exec|execute|shutdown)/i,
            /(?:information_schema\.(?:tables|columns|schemata|routines))/i,
            /(?:@@version|version\(\)|database\(\)|user\(\))/i,
            /(?:load_file\s*\()/i,
            /(?:into\s+(?:outfile|dumpfile)\s+)/i,
            /(?:\.\.\/\.\.\/\.\.\/)/i,
            /(?:extractvalue\s*\(|updatexml\s*\()/i,
            /(?:\$where|\$ne|\$gt|\$lt|\$regex)/i,
        ];

        this.xssPatterns = [
            /<script[^>]*>.*?<\/script>/i,
            /javascript:\s*/i,
            /(?:data|vbscript):/i,
            /on(?:load|click|error|mouse(?:over|out|move)|key(?:press|down|up)|focus|blur|change|submit|reset|select)\s*=/i,
            /(?:alert|confirm|prompt|console\.(?:log|error|warn)|document\.write)\s*\(/i,
            /<(?:iframe|embed|object|applet|frame|frameset)[^>]*>/i,
            /(?:eval|setTimeout|setInterval|Function|setImmediate)\s*\(/i,
            /document\.(?:cookie|domain|location|referrer|URL|URLUnencoded|body|forms|images|links)/i,
            /window\.(?:location|open|navigator|history|localStorage|sessionStorage)/i,
            /<(?:svg|math|img|input|button|div|span|a|link|meta|style)[^>]*\s+(?:on\w+\s*=|\w+\s*:\s*javascript)/i,
            /&lt;script|&lt;\/script|%3Cscript|%3C\/script/i,
            /\\x3Cscript|\\u003Cscript/i,
            /<svg[^>]*onload=/i,
            /<img[^>]*src=[^>]*onerror=/i,
            /\.(?:innerHTML|outerHTML|insertAdjacentHTML|document\.write)/i,
        ];

        this.pathTraversalPatterns = [
            /(?:\.\.\/|\.\.\\)/i,
            /(?:\/etc\/(?:passwd|shadow|hosts|group|services|resolv\.conf|motd)|\/proc\/|\/sys\/|\/boot\/)/i,
            /(?:c:\\windows\\system32\\|c:\\winnt\\|c:\\program files\\|c:\\programdata\\)/i,
            /(?:\.(?:env|git|svn|hg|bzr|cvs|DS_Store|swp|swo|bak|old|backup))/i,
            /(?:\/(?:\.ssh|\.aws|\.config|\.cache|\.local|\.gnupg)\/)/i,
            /(?:\.(?:php|asp|aspx|jsp|jspx|do|action|pl|cgi|py|rb|sh|exe|dll|so|dylib))/i,
            /(?:config|settings|secrets?|credentials?|password|token|key)\.(?:json|yml|yaml|xml|ini|conf|cfg|properties)/i,
            /(?:\.\.%2f|\.\.%5c|%2e%2e%2f|%252e%252e%252f)/i,
            /(?:\\x2e\\x2e\\x2f|\\u002e\\u002e\\u002f)/i,
            /%00|\x00/,
        ];

        this.maliciousUserAgents = [
            /(?:sqlmap|nikto|wpscan|dirb|gobuster|dirbuster|ffuf|wfuzz|arjun|x8|parameth|nosqlmap)/i,
            /(?:nessus|openvas|nexpose|qualys|acunetix|netsparker|appscan|burpsuite|zaproxy|w3af|skipfish|wapiti)/i,
            /(?:nmap|masscan|zmap|unicornscan|rustscan|naabu|shodan|censys|zoomeye|fofa)/i,
            /(?:metasploit|empire|cobaltstrike|beacon|mimikatz|bloodhound|responder|impacket)/i,
            /(?:scanner|spider|crawler|bot|harvester|collector|extractor|grabber)/i,
            /(?:curl|wget|libcurl|python-requests|go-http-client|java|okhttp|apache-httpclient|perl|ruby|node-fetch)/i,
            /(?:anonymous|proxy|vpn|tor|hacker|attack|exploit|payload|shell|reverse)/i,
            /(?:hydra|medusa|patator|ncrack|john|hashcat|cain|abel|rainbowcrack)/i,
            /(?:phantomjs|puppeteer|playwright|selenium|chromium|headlesschrome|headlessfirefox)/i,
            /(?:postman|insomnia|httpx|httpie|restclient|soapui|jmeter|loadrunner|gatling)/i,
        ];

        this.sqlDetector = new SqlInjectionDetector();
    }

    scanRequest(method, uri, headers, body) {
        const path = uri;
        const userAgent = headers['user-agent'] || '';

        for (const pattern of this.maliciousUserAgents) {
            if (pattern.test(userAgent)) {
                return {
                    error: 'Request blocked',
                    reason: `Suspicious User-Agent detected: ${userAgent}`
                };
            }
        }

        const sqlResult = this.sqlDetector.detect(path);
        if (sqlResult.detected) {
            return {
                error: 'SQL Injection Attempt',
                reason: `SQL injection detected: ${sqlResult.reason}`
            };
        }

        const lowerPath = path.toLowerCase();
        for (const pattern of this.sqlInjectionPatterns) {
            if (pattern.test(lowerPath)) {
                return {
                    error: 'Request blocked',
                    reason: `SQL injection pattern detected: ${pattern.source}`
                };
            }
        }

        for (const pattern of this.xssPatterns) {
            if (pattern.test(path)) {
                return {
                    error: 'Request blocked',
                    reason: `XSS pattern detected: ${pattern.source}`
                };
            }
        }

        for (const pattern of this.pathTraversalPatterns) {
            if (pattern.test(path)) {
                return {
                    error: 'Request blocked',
                    reason: `Path traversal pattern detected: ${pattern.source}`
                };
            }
        }

        if (body) {
            const bodyStr = typeof body === 'string' ? body : JSON.stringify(body);
            
            const bodySqlResult = this.sqlDetector.detect(bodyStr);
            if (bodySqlResult.detected) {
                return {
                    error: 'SQL Injection in Body',
                    reason: `SQL injection in request body: ${bodySqlResult.reason}`
                };
            }

            for (const pattern of this.xssPatterns) {
                if (pattern.test(bodyStr)) {
                    return {
                        error: 'Request blocked',
                        reason: `XSS in request body: ${pattern.source}`
                    };
                }
            }
        }

        return null;
    }
}

class WAFRules {
    constructor() {
        this.maxContentLength = 5 * 1024 * 1024;
        this.allowedMethods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS'];
        this.blockedContentTypes = [
            'application/x-ms-application',
            'application/x-ms-xbap',
            'application/x-msmanifest',
            'application/x-sh',
            'application/x-shellscript',
            'text/x-php',
            'application/x-php',
            'application/x-httpd-php',
            'application/x-httpd-php-source',
            'application/x-perl',
            'application/x-python',
            'application/x-ruby',
            'application/x-executable',
            'application/x-dosexec',
        ];
        this.maxHeadersCount = 50;
        this.maxHeaderLength = 8192;
    }

    validateRequest(method, headers, contentLength) {
        if (!this.allowedMethods.includes(method)) {
            return {
                error: 'Invalid method',
                reason: `Method ${method} not allowed`
            };
        }

        if (contentLength && contentLength > this.maxContentLength) {
            return {
                error: 'Request too large',
                reason: `Content length ${contentLength} exceeds maximum ${this.maxContentLength}`
            };
        }

        const headerCount = Object.keys(headers).length;
        if (headerCount > this.maxHeadersCount) {
            return {
                error: 'Too many headers',
                reason: `${headerCount} headers exceeds maximum ${this.maxHeadersCount}`
            };
        }

        for (const [name, value] of Object.entries(headers)) {
            if (name.length > this.maxHeaderLength || 
                (value && value.length > this.maxHeaderLength)) {
                return {
                    error: 'Header too large',
                    reason: `Header ${name} exceeds maximum length`
                };
            }
        }

        const contentType = headers['content-type'];
        if (contentType) {
            for (const blockedCt of this.blockedContentTypes) {
                if (contentType.includes(blockedCt)) {
                    return {
                        error: 'Blocked content type',
                        reason: `Content type ${contentType} is not allowed`
                    };
                }
            }
        }

        return null;
    }
}

class SecurityMetrics {
    constructor() {
        this.blockedRequests = 0;
        this.sqlInjectionAttempts = 0;
        this.xssAttempts = 0;
        this.rateLimitHits = 0;
        this.websocketConnections = 0;
    }

    incrementBlocked() { this.blockedRequests++; }
    incrementSqlInjection() { this.sqlInjectionAttempts++; }
    incrementXss() { this.xssAttempts++; }
    incrementRateLimit() { this.rateLimitHits++; }
    incrementWebSocket() { this.websocketConnections++; }
}

class RequestDeduplicator {
    constructor() {
        this.cache = new Map();
        this.cacheTtl = 300000;
    }

    checkDuplicate(requestId) {
        if (this.cache.has(requestId)) {
            const { timestamp, response } = this.cache.get(requestId);
            if (Date.now() - timestamp < this.cacheTtl) {
                return response;
            }
            this.cache.delete(requestId);
        }
        return null;
    }

    storeResponse(requestId, response) {
        this.cache.set(requestId, {
            timestamp: Date.now(),
            response
        });
        this.cleanupExpired();
    }

    cleanupExpired() {
        const now = Date.now();
        for (const [key, { timestamp }] of this.cache.entries()) {
            if (now - timestamp >= this.cacheTtl) {
                this.cache.delete(key);
            }
        }
    }
}

class NonceValidator {
    constructor() {
        this.usedNonces = new Map();
        this.nonceTtl = 300000;
    }

    validateNonce(nonce) {
        if (this.usedNonces.has(nonce)) {
            const timestamp = this.usedNonces.get(nonce);
            if (Date.now() - timestamp < this.nonceTtl) {
                return { valid: false, error: 'Nonce already used' };
            }
            this.usedNonces.delete(nonce);
        }

        this.usedNonces.set(nonce, Date.now());
        this.cleanupExpired();
        return { valid: true };
    }

    cleanupExpired() {
        const now = Date.now();
        for (const [nonce, timestamp] of this.usedNonces.entries()) {
            if (now - timestamp >= this.nonceTtl) {
                this.usedNonces.delete(nonce);
            }
        }
    }
}

class InFlightTracker {
    constructor() {
        this.requests = new Map();
        this.timeout = 200;
    }

    generateRequestHash(method, uri, body, clientIp = '') {
        const hash = crypto.createHash('sha256');
        hash.update(method);
        hash.update(uri);
        hash.update(clientIp);
        if (body) {
            hash.update(typeof body === 'string' ? body : JSON.stringify(body));
        }
        return hash.digest('hex');
    }

    isDuplicate(requestHash) {
        if (this.requests.has(requestHash)) {
            const timestamp = this.requests.get(requestHash);
            if (Date.now() - timestamp < this.timeout) {
                return true;
            }
            this.requests.delete(requestHash);
        }
        return false;
    }

    markInFlight(requestHash) {
        this.requests.set(requestHash, Date.now());
    }

    completeRequest(requestHash) {
        this.requests.delete(requestHash);
    }

    cleanupExpired() {
        const now = Date.now();
        for (const [hash, timestamp] of this.requests.entries()) {
            if (now - timestamp >= this.timeout) {
                this.requests.delete(hash);
            }
        }
    }
}

// ==================== Utility Functions ====================

function getRealClientIp(req) {
    const xff = req.headers['x-forwarded-for'];
    if (xff) {
        const ips = xff.split(',').map(ip => ip.trim());
        if (ips.length > 0 && ips[0]) {
            return ips[0];
        }
    }

    const realIp = req.headers['x-real-ip'];
    if (realIp) return realIp;

    const clientIp = req.headers['x-client-ip'];
    if (clientIp) return clientIp;

    return req.ip || req.connection.remoteAddress || '127.0.0.1';
}

function addSecurityHeaders(res) {
    res.set({
        'X-Content-Type-Options': 'nosniff',
        'X-Frame-Options': 'DENY',
        'X-XSS-Protection': '1; mode=block',
        'Referrer-Policy': 'strict-origin-when-cross-origin',
        'Permissions-Policy': 'geolocation=(), microphone=(), camera=()',
    });

    if (!res.get('Content-Security-Policy')) {
        res.set('Content-Security-Policy', 
            "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
            "style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; " +
            "font-src 'self'; connect-src 'self'; frame-ancestors 'none';");
    }
}

function blockedResponse(res, error, reason) {
    addSecurityHeaders(res);
    return res.status(403).json({
        error,
        reason,
        request_id: crypto.randomUUID(),
        timestamp: new Date().toISOString(),
    });
}

function notFoundResponse(res, forwardedTo) {
    addSecurityHeaders(res);
    return res.status(404).json({
        error: 'Endpoint not found',
        message: 'The requested endpoint was not found on the backend server',
        forwarded_to: forwardedTo,
        available_endpoints: [
            '/api/health',
            '/api/test',
            '/api/security/status',
            '/api/security/metrics',
            '/api/ws/*',
        ],
        timestamp: new Date().toISOString(),
    });
}

// ==================== Initialize Components ====================

const rateLimiter = new RateLimiter(RATE_LIMIT_PER_MINUTE);
const securityScanner = new SecurityScanner();
const wafRules = new WAFRules();
const securityMetrics = new SecurityMetrics();
const deduplicator = new RequestDeduplicator();
const nonceValidator = new NonceValidator();
const inFlightTracker = new InFlightTracker();

// Create base axios client
const baseAxiosClient = axios.create({
    timeout: 30000,
    headers: {
        'User-Agent': 'Secure-API-Gateway/1.0'
    },
    validateStatus: () => true,
});

// Wrap it with Zipkin tracing
const axiosClient = createTracedAxiosClient(baseAxiosClient, tracer, 'backend-service');

// ==================== Express App Setup ====================

const app = express();
const server = http.createServer(app);

// Apply Zipkin middleware FIRST
app.use(zipkinMiddleware({ tracer }));

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));
app.set('trust proxy', true);

// ==================== Security Middleware with Zipkin Tracing ====================

async function securityMiddleware(req, res, next) {
    tracer.scoped(() => {
        const securitySpan = tracer.createChildId();
        tracer.setId(securitySpan);
        tracer.recordServiceName('security-layer');
        tracer.recordRpc('security.check');
        tracer.recordBinary('http.method', req.method);
        tracer.recordBinary('http.path', req.originalUrl);

        const clientIp = getRealClientIp(req);
        tracer.recordBinary('client.ip', clientIp);

        const method = req.method;
        const uri = req.originalUrl;
        const headers = req.headers;
        const idempotencyKey = headers['x-idempotency-key'];
        const contentLength = parseInt(headers['content-length'] || '0', 10);

        // WAF check
        const wafResult = wafRules.validateRequest(method, headers, contentLength);
        if (wafResult) {
            console.warn(`WAF blocked request from ${clientIp}: ${wafResult.error} - ${wafResult.reason}`);
            securityMetrics.incrementBlocked();
            tracer.recordBinary('security.blocked', true);
            tracer.recordBinary('security.reason', wafResult.reason);
            tracer.recordAnnotation('waf.blocked');
            return blockedResponse(res, wafResult.error, wafResult.reason);
        }

        // Nonce check
        const nonce = headers['x-request-nonce'];
        if (nonce) {
            const nonceResult = nonceValidator.validateNonce(nonce);
            if (!nonceResult.valid) {
                tracer.recordBinary('security.nonce.invalid', true);
                tracer.recordAnnotation('nonce.rejected');
                return blockedResponse(res, 'Invalid nonce', nonceResult.error);
            }
            tracer.recordBinary('security.nonce.valid', true);
        }

        // Idempotency check
        if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method) && idempotencyKey) {
            const cached = deduplicator.checkDuplicate(idempotencyKey);
            if (cached) {
                console.log(`Returning cached response for duplicate request: ${idempotencyKey}`);
                tracer.recordBinary('idempotency.cache_hit', true);
                tracer.recordAnnotation('cache.hit');
                addSecurityHeaders(res);
                res.set('X-Idempotency-Replay', 'true');
                return res.status(200).json(cached);
            }
            tracer.recordBinary('idempotency.key', idempotencyKey);
        }

        // In-flight duplicate check — mutations only, scoped per IP
        if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method)) {
            const requestHash = inFlightTracker.generateRequestHash(method, uri, req.body, clientIp);

            if (inFlightTracker.isDuplicate(requestHash)) {
                console.warn(`Duplicate in-flight request detected from ${clientIp}: ${requestHash}`);
                tracer.recordBinary('duplicate.in_flight', true);
                tracer.recordAnnotation('duplicate.rejected');
                return blockedResponse(res, 'Duplicate request', 'An identical request is already being processed');
            }

            inFlightTracker.markInFlight(requestHash);
            req.requestHash = requestHash;
        }

        // Security scan
        const scanResult = securityScanner.scanRequest(method, uri, headers, req.body);
        if (scanResult) {
            console.warn(`Request blocked from IP ${clientIp}: ${scanResult.error} - ${scanResult.reason}`);

            // Clean up in-flight entry if we marked one
            if (req.requestHash) {
                inFlightTracker.completeRequest(req.requestHash);
            }

            securityMetrics.incrementBlocked();
            tracer.recordBinary('security.threat_detected', true);
            tracer.recordBinary('security.threat_type', scanResult.error);
            tracer.recordBinary('security.threat_reason', scanResult.reason);

            if (scanResult.error.includes('SQL')) {
                securityMetrics.incrementSqlInjection();
                tracer.recordAnnotation('sql_injection.blocked');
            } else if (scanResult.error.includes('XSS')) {
                securityMetrics.incrementXss();
                tracer.recordAnnotation('xss.blocked');
            }

            return blockedResponse(res, scanResult.error, scanResult.reason);
        }

        tracer.recordBinary('security.passed', true);
        tracer.recordAnnotation('security.cleared');

        req.idempotencyKey = idempotencyKey;

        next();
    });
}

// Response interceptor
app.use((req, res, next) => {
    const originalJson = res.json;
    res.json = function(data) {
        addSecurityHeaders(res);
        
        if (req.idempotencyKey && 
            ['POST', 'PUT', 'PATCH', 'DELETE'].includes(req.method) &&
            res.statusCode >= 200 && res.statusCode < 300) {
            deduplicator.storeResponse(req.idempotencyKey, data);
            
            tracer.scoped(() => {
                tracer.recordBinary('idempotency.cached', true);
                tracer.recordAnnotation('cache.stored');
            });
        }
        
        return originalJson.call(this, data);
    };
    next();
});

// ==================== API Routes ====================
app.post('/api/settings/upload-profile-image/:id', 
    securityMiddleware,
    upload.single('image'),
    async (req, res) => {
        try {
            const { id } = req.params;
            if (!req.file) {
                return res.status(400).json({ 
                    error: 'No image provided', 
                    message: 'Please include an image file in the request' 
                });
            }
            const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
            if (!allowedTypes.includes(req.file.mimetype)) {
                return res.status(400).json({ 
                    error: 'Invalid file type',
                    message: 'Only JPEG, PNG, GIF and WebP images are allowed'
                });
            }

            const formData = new FormData();
            formData.append('image', req.file.buffer, {
                filename: req.file.originalname,
                contentType: req.file.mimetype,
            });

            const response = await baseAxiosClient.post(
                `${BACKEND_URL}/settings/upload-profile-image/${id}`,
                formData,
                {
                    headers: {
                        ...formData.getHeaders(),
                        'Authorization': req.headers['authorization'],
                    },
                    maxBodyLength: Infinity,   
                    maxContentLength: Infinity, 
                }
            );

            addSecurityHeaders(res);
            res.status(response.status).json(response.data);

        } catch (err) {
            console.error('Upload error:', err.message);
            res.status(500).json({ error: 'Upload failed', message: err.message });
        }
    }
);

// Serve uploaded profile images
app.get('/api/uploads/images/:filename', async (req, res) => {
    try {
        const { filename } = req.params;
        
        const response = await baseAxiosClient.get(
            `${BACKEND_URL}/uploads/images/${filename}`,
            { responseType: 'stream' }  
        );

        res.set('Content-Type', response.headers['content-type']);
        res.set('Cache-Control', 'public, max-age=86400');
        
        response.data.pipe(res); 

    } catch (err) {
        console.error('Image serve error:', err.message);
        res.status(404).json({ error: 'Image not found' });
    }
});

app.use('/api', securityMiddleware);

app.get('/api/health', async (req, res) => {
    let backendReachable = false;
    try {
        // Use a valid health endpoint instead of just the base URL
        const healthUrl = `${BACKEND_URL}/health`;
        await axiosClient.get(healthUrl);
        backendReachable = true;
    } catch (err) {
        // Backend not reachable
    }

    addSecurityHeaders(res);
    res.json({
        status: 'healthy',
        gateway: `http://0.0.0.0:${PORT}`,
        backend: `${BACKEND_URL}/`,
        backend_ws: `${BACKEND_WS_URL}/`,
        backend_reachable: backendReachable,
        security_enabled: true,
        zipkin_enabled: true,
        trace_id: req.header('X-B3-TraceId') || 'N/A',
    });
});

app.get('/api/test', async (req, res) => {
    console.log(`Testing connection to backend: ${BACKEND_URL}`);
    
    try {
        // Use a valid test endpoint
        const testUrl = `${BACKEND_URL}/health`;
        const response = await axiosClient.get(testUrl);
        console.log(`Backend is reachable. Status: ${response.status}`);
        
        addSecurityHeaders(res);
        res.json({
            message: `Backend is reachable with status: ${response.status}`,
            backend_url: BACKEND_URL,
            reachable: true,
            trace_id: req.header('X-B3-TraceId') || 'N/A',
        });
    } catch (err) {
        console.warn(`Cannot connect to backend: ${err.message}`);
        
        addSecurityHeaders(res);
        res.json({
            message: `Cannot connect to backend: ${err.message}`,
            backend_url: BACKEND_URL,
            reachable: false,
            trace_id: req.header('X-B3-TraceId') || 'N/A',
        });
    }
});

app.get('/api/security/status', (req, res) => {
    addSecurityHeaders(res);
    res.json({
        security_patterns: {
            sql_patterns: securityScanner.sqlInjectionPatterns.length,
            xss_patterns: securityScanner.xssPatterns.length,
            path_traversal_patterns: securityScanner.pathTraversalPatterns.length,
            malicious_agents_patterns: securityScanner.maliciousUserAgents.length,
        },
        rate_limit_config: {
            max_requests_per_minute: rateLimiter.maxRequestsPerMinute,
        },
        websocket_enabled: true,
        zipkin_enabled: true,
        trace_id: req.header('X-B3-TraceId') || 'N/A',
    });
});

app.get('/api/security/metrics', (req, res) => {
    addSecurityHeaders(res);
    res.json({
        blocked_requests: securityMetrics.blockedRequests,
        sql_injection_attempts: securityMetrics.sqlInjectionAttempts,
        xss_attempts: securityMetrics.xssAttempts,
        rate_limit_hits: securityMetrics.rateLimitHits,
        websocket_connections: securityMetrics.websocketConnections,
        uptime_seconds: Math.floor(process.uptime()),
        trace_id: req.header('X-B3-TraceId') || 'N/A',
    });
});

// ==================== WebSocket Proxy with Tracing ====================

const wss = new WebSocket.Server({ noServer: true });

server.on('upgrade', (request, socket, head) => {
    const pathname = new URL(request.url, `http://${request.headers.host}`).pathname;
    
    if (pathname.startsWith('/api/ws/')) {
        const clientIp = getRealClientIp({ headers: request.headers, connection: socket });
        
        tracer.scoped(() => {
            const wsSpan = tracer.createRootId();
            tracer.setId(wsSpan);
            tracer.recordServiceName(serviceName);
            tracer.recordRpc('websocket.upgrade');
            tracer.recordBinary('ws.path', pathname);
            tracer.recordBinary('client.ip', clientIp);
            tracer.recordAnnotation('ws.upgrade.start');
            
            console.log(`WebSocket upgrade request - Path: ${pathname}, Client: ${clientIp}, Trace: ${wsSpan.traceId}`);
            
            securityMetrics.incrementWebSocket();
            tracer.recordBinary('ws.connections.total', securityMetrics.websocketConnections);
            
            wss.handleUpgrade(request, socket, head, (ws) => {
                wss.emit('connection', ws, request);
            });
        });
    } else {
        socket.destroy();
    }
});

wss.on('connection', (clientSocket, request) => {
    const parsedUrl = new URL(request.url, `http://${request.headers.host}`);
    const pathname = parsedUrl.pathname;
    const query = parsedUrl.search;
    const clientIp = getRealClientIp({ headers: request.headers, connection: request.socket });
    
    const backendPath = pathname.replace(/^\/api\/ws/, '');
    const backendWsUrl = `${BACKEND_WS_URL}${backendPath}${query}`;
    
    const rootId = tracer.createRootId();
    
    tracer.scoped(() => {
        tracer.setId(rootId);
        tracer.recordServiceName(serviceName);
        tracer.recordRpc('websocket.proxy');
        tracer.recordBinary('ws.backend_url', backendWsUrl);
        tracer.recordBinary('client.ip', clientIp);
        tracer.recordAnnotation('ws.connection.established');
        
        console.log(`WebSocket proxying to: ${backendWsUrl}, Trace: ${rootId.traceId}`);
    });
    
    const backendSocket = new WebSocket(backendWsUrl);
    
    backendSocket.on('open', () => {
        tracer.scoped(() => {
            tracer.setId(rootId);
            tracer.recordAnnotation('ws.backend.connected');
        });
        
        console.log('Successfully connected to backend WebSocket');
        
        clientSocket.on('message', (data, isBinary) => {
            const dataType = isBinary ? 'binary' : 'text';
            
            tracer.scoped(() => {
                const msgSpan = tracer.createChildId();
                tracer.setId(msgSpan);
                tracer.recordServiceName(serviceName);
                tracer.recordRpc('websocket.client_to_backend');
                tracer.recordBinary('message.type', dataType);
                tracer.recordBinary('message.size', data.length);
                tracer.recordAnnotation('ws.message.sent');
            });
            
            console.log(`Client ${clientIp} -> Backend: ${dataType} (${data.length} bytes)`);
            
            if (backendSocket.readyState === WebSocket.OPEN) {
                backendSocket.send(data, { binary: isBinary });
            }
        });
        
        clientSocket.on('ping', (data) => {
            if (backendSocket.readyState === WebSocket.OPEN) {
                backendSocket.ping(data);
            }
        });
        
        clientSocket.on('pong', (data) => {
            if (backendSocket.readyState === WebSocket.OPEN) {
                backendSocket.pong(data);
            }
        });
        
        clientSocket.on('close', (code, reason) => {
            tracer.scoped(() => {
                tracer.setId(rootId);
                tracer.recordBinary('ws.close.code', code);
                tracer.recordBinary('ws.close.reason', reason.toString());
                tracer.recordAnnotation('ws.client.closed');
            });
            
            console.log(`Client ${clientIp} closed: ${code} - ${reason}`);
            if (backendSocket.readyState === WebSocket.OPEN) {
                // Normalize close code - reserved codes (1005, 1006, 1015) cannot be sent
                const normalizedCode = (code === 1005 || code === 1006 || code === 1015) ? 1000 : code;
                backendSocket.close(normalizedCode, reason);
            }
        });
        
        clientSocket.on('error', (err) => {
            tracer.scoped(() => {
                tracer.setId(rootId);
                tracer.recordBinary('error', err.message);
                tracer.recordAnnotation('ws.client.error');
            });
            
            console.error(`Client ${clientIp} error:`, err);
            backendSocket.close();
        });
    });
    
    backendSocket.on('message', (data, isBinary) => {
        const dataType = isBinary ? 'binary' : 'text';
        
        tracer.scoped(() => {
            const msgSpan = tracer.createChildId();
            tracer.setId(msgSpan);
            tracer.recordServiceName(serviceName);
            tracer.recordRpc('websocket.backend_to_client');
            tracer.recordBinary('message.type', dataType);
            tracer.recordBinary('message.size', data.length);
            tracer.recordAnnotation('ws.message.received');
        });
        
        console.log(`Backend -> Client ${clientIp}: ${dataType} (${data.length} bytes)`);
        
        if (clientSocket.readyState === WebSocket.OPEN) {
            clientSocket.send(data, { binary: isBinary });
        }
    });
    
    backendSocket.on('ping', (data) => {
        if (clientSocket.readyState === WebSocket.OPEN) {
            clientSocket.ping(data);
        }
    });
    
    backendSocket.on('pong', (data) => {
        if (clientSocket.readyState === WebSocket.OPEN) {
            clientSocket.pong(data);
        }
    });
    
    backendSocket.on('close', (code, reason) => {
        tracer.scoped(() => {
            tracer.setId(rootId);
            tracer.recordBinary('ws.backend.close.code', code);
            tracer.recordBinary('ws.backend.close.reason', reason.toString());
            tracer.recordAnnotation('ws.backend.closed');
        });
        
        console.log(`Backend closed to client ${clientIp}: ${code} - ${reason}`);
        if (clientSocket.readyState === WebSocket.OPEN) {
            // Normalize close code - reserved codes (1005, 1006, 1015) cannot be sent
            const normalizedCode = (code === 1005 || code === 1006 || code === 1015) ? 1000 : code;
            clientSocket.close(normalizedCode, reason);
        }
    });
    
    backendSocket.on('error', (err) => {
        tracer.scoped(() => {
            tracer.setId(rootId);
            tracer.recordBinary('error', err.message);
            tracer.recordAnnotation('ws.backend.error');
        });
        
        console.error(`Backend WebSocket error ${backendWsUrl}:`, err.message);
        if (clientSocket.readyState === WebSocket.OPEN) {
            clientSocket.close(1011, 'Backend connection failed');
        }
    });
});

// ==================== Proxy Handler (Fallback) ====================

app.use('/api', async (req, res) => {
    const method = req.method;
    const uri = req.originalUrl;
    const path = req.path;
    
    let backendPath = path.replace(/^\/api/, '');
    if (!backendPath) backendPath = '/';
    
    const queryString = req.originalUrl.split('?')[1] || '';
    const url = queryString 
        ? `${BACKEND_URL}${backendPath}?${queryString}`
        : `${BACKEND_URL}${backendPath}`;
    
    console.log(`Forwarding ${method} ${uri} -> ${url}`);
    
    tracer.scoped(() => {
        tracer.recordBinary('proxy.target_url', url);
        tracer.recordBinary('proxy.method', method);
        tracer.recordAnnotation('proxy.forward.start');
    });
    
    try {
        const axiosConfig = {
            method: method.toLowerCase(),
            url,
            headers: { ...req.headers },
            data: req.body,
        };
        
        delete axiosConfig.headers.host;
        delete axiosConfig.headers['content-length'];
        
        if (['POST', 'PUT', 'PATCH'].includes(method) && 
            !axiosConfig.headers['content-type'] && 
            req.body) {
            axiosConfig.headers['content-type'] = 'application/json';
        }
        
        const response = await axiosClient(axiosConfig);
        
        tracer.scoped(() => {
            tracer.recordBinary('http.status_code', response.status);
            tracer.recordAnnotation('proxy.response.received');
        });
        
        console.log(`Backend responded with status: ${response.status}`);
        
        if (response.status === 404) {
            // If backend sent a structured error body, forward it directly
            if (response.data && typeof response.data === 'object') {
                addSecurityHeaders(res);
                return res.status(404).json(response.data);
            }
            console.warn(`Backend returned 404 for: ${url}`);
            return notFoundResponse(res, url);
        }
        Object.entries(response.headers).forEach(([key, value]) => {
            if (key !== 'content-length' && key !== 'transfer-encoding') {
                res.set(key, value);
            }
        });
        
        addSecurityHeaders(res);
        res.status(response.status).send(response.data);
        
    } catch (err) {
        tracer.scoped(() => {
            tracer.recordBinary('error', err.message);
            tracer.recordBinary('error.code', err.code || 'UNKNOWN');
            tracer.recordAnnotation('proxy.error');
        });
        
        console.warn(`Backend request failed: ${err.message}`);
        
        if (err.code === 'ECONNREFUSED' || err.code === 'ENOTFOUND') {
            const errorMsg = `Cannot connect to backend at ${BACKEND_URL}. Is the server running?`;
            console.warn(errorMsg);
            
            addSecurityHeaders(res);
            res.status(502).json({
                error: 'Backend unavailable',
                message: errorMsg,
                backend_url: BACKEND_URL,
            });
        } else if (err.code === 'ETIMEDOUT' || err.code === 'ECONNABORTED') {
            const errorMsg = `Backend timeout after 30 seconds at ${url}`;
            console.warn(errorMsg);
            
            addSecurityHeaders(res);
            res.status(504).json({
                error: 'Backend timeout',
                message: errorMsg,
                url,
            });
        } else {
            console.warn(`Backend endpoint not found or error: ${url}`);
            return notFoundResponse(res, url);
        }
    }
});

// ==================== Cleanup Intervals ====================

setInterval(() => {
    rateLimiter.cleanup();
    deduplicator.cleanupExpired();
    nonceValidator.cleanupExpired();
    inFlightTracker.cleanupExpired();
}, 60000);

// ==================== Server Startup ====================

(async () => {
    console.log('Testing backend connection on startup...');
    try {
        // Use a valid health endpoint for startup check
        const healthUrl = `${BACKEND_URL}/health`;
        const response = await axiosClient.get(healthUrl);
        console.log(`Backend is reachable at ${BACKEND_URL} (Status: ${response.status})`);
    } catch (err) {
        console.warn(`Backend is NOT reachable at ${BACKEND_URL}`);
        console.warn(`Error: ${err.message}`);
        console.warn('Please make sure your backend server is running');
    }
    
    server.listen(PORT, '0.0.0.0', () => {
        console.log(`Secure API Gateway running on http://0.0.0.0:${PORT}`);
        console.log(`Gateway API endpoints available at http://0.0.0.0:${PORT}/api/`);
        console.log(`WebSocket proxy available at ws://0.0.0.0:${PORT}/api/ws/`);
        console.log(`Forwarding HTTP requests to backend at ${BACKEND_URL}`);
        console.log(`Forwarding WebSocket requests to backend at ${BACKEND_WS_URL}`);
        console.log(`Zipkin tracing enabled at ${zipkinBaseUrl}`);
        console.log('Security Features Enabled:');
        console.log('  SQL Injection Protection');
        console.log('  XSS Attack Protection');
        console.log('  Path Traversal Protection');
        console.log('  Bot/Scanner Detection');
        console.log('  WAF Rules');
        console.log('  Nonce Validation');
        console.log('  In-Flight Request Tracking');
        console.log('  Request Deduplication');
        console.log('  WebSocket Proxying');
        console.log('  Rate Limiting: DISABLED');
        console.log('  Distributed Tracing: ENABLED');
        console.log(`Health check: http://0.0.0.0:${PORT}/api/health`);
        console.log(`Security status: http://0.0.0.0:${PORT}/api/security/status`);
        console.log(`Security metrics: http://0.0.0.0:${PORT}/api/security/metrics`);
        console.log(`Zipkin UI: ${zipkinBaseUrl}/zipkin/`);
    });
})();