using System.Text.RegularExpressions;

namespace resiliences_service.Middleware
{
   public class AttackPatternDetector
    {
        private static readonly string[] SqlInjectionPatterns = new[]
        {
            // UNION-based SQLi
            @"(?i)\bunion\b.+\bselect\b",
            @"(?i)\bunion\b.+\bfrom\b",
            
            // Boolean-based blind SQLi
            @"(?i)\b(and|or)\b\s*\d+\s*=\s*\d+",
            @"(?i)\b(and|or)\b\s+['""]?\w+['""]?\s*=\s*['""]?\w+['""]?",
            
            // Time-based SQLi
            @"(?i)\b(sleep|benchmark|waitfor|pg_sleep)\b\s*\(",
            @"(?i)\bif\b\s*\(.+,\s*(sleep|benchmark)\s*\(",
            
            // Stacked queries
            @";\s*(drop|delete|update|insert|create|alter)\b",
            
            // Information schema enumeration
            @"(?i)\binformation_schema\b",
            @"(?i)\bsys\.(tables|columns|objects)\b",
            @"(?i)\bmysql\.(user|db)\b",
            
            // Error-based SQLi
            @"(?i)\bextractvalue\b\s*\(",
            @"(?i)\bupdatexml\b\s*\(",
            @"(?i)\bconvert\b\s*\(\s*int",
            
            // Database functions
            @"(?i)\b(exec|execute|sp_executesql)\b",
            @"(?i)\bload_file\b\s*\(",
            @"(?i)\binto\s+outfile\b",
            
            // SQL comments
            @"(/\*|\*/|--|\#)",
            @"(?i)\bor\b\s+['""]?\d+['""]?\s*=\s*['""]?\d+['""]?",
            
            // NoSQL injection
            @"(\$ne|\$gt|\$lt|\$or|\$and|\$where)",
            @"(?i){\s*\$where\s*:",
            
            // Classic SQLi
            @"(?i)(\bor\b|\band\b)\s+['""]?\d+['""]?\s*=\s*['""]?\d+['""]?",
            @"['""];?\s*(drop|delete|insert|update|select)\b"
        };

        private static readonly string[] XssPatterns = new[]
        {
            // Script tags
            @"<script[^>]*>.*?</script>",
            @"<script[^>]*>",
            
            // JavaScript URIs
            @"javascript:",
            @"vbscript:",
            @"data:text/html",
            
            // Event handlers
            @"\bon\w+\s*=",
            @"(?i)\bon(load|error|click|mouse|focus|blur)\s*=",
            
            // Dangerous functions
            @"(?i)\beval\s*\(",
            @"(?i)\bsetTimeout\s*\(",
            @"(?i)\bsetInterval\s*\(",
            @"(?i)Function\s*\(",
            
            // DOM XSS sinks
            @"(?i)\.innerHTML\s*=",
            @"(?i)document\.write\s*\(",
            @"(?i)document\.writeln\s*\(",
            
            // SVG XSS
            @"<svg[^>]*>",
            @"<svg[^>]*onload",
            
            // HTML encoded attacks
            @"&#\d+;",
            @"&#x[0-9a-f]+;",
            
            // Unicode encoded
            @"\\u[0-9a-f]{4}",
            @"%u[0-9a-f]{4}"
        };

        private static readonly string[] PathTraversalPatterns = new[]
        {
            @"\.\./",
            @"\.\.\\/",
            @"%2e%2e/",
            @"%2e%2e%5c",
            @"\.\.\\",
            @"/etc/passwd",
            @"/etc/shadow",
            @"c:\\windows",
            @"c:/windows",
            @"\0",
            @"%00",
            @"\.git",
            @"\.env",
            @"\.config",
            @"web\.config",
            @"appsettings\.json"
        };

        private static readonly string[] MaliciousUserAgents = new[]
        {
            "sqlmap",
            "nikto",
            "nmap",
            "masscan",
            "metasploit",
            "burp",
            "acunetix",
            "nessus",
            "openvas",
            "w3af",
            "havij",
            "pangolin",
            "headless",
            "phantomjs",
            "selenium",
            "puppeteer"
        };

        public (bool IsAttack, string AttackType, string Pattern) DetectSqlInjection(string input)
        {
            if (string.IsNullOrWhiteSpace(input))
                return (false, "", "");

            foreach (var pattern in SqlInjectionPatterns)
            {
                if (Regex.IsMatch(input, pattern, RegexOptions.IgnoreCase))
                {
                    return (true, "SQL_INJECTION", pattern);
                }
            }

            return (false, "", "");
        }

        public (bool IsAttack, string AttackType, string Pattern) DetectXss(string input)
        {
            if (string.IsNullOrWhiteSpace(input))
                return (false, "", "");

            foreach (var pattern in XssPatterns)
            {
                if (Regex.IsMatch(input, pattern, RegexOptions.IgnoreCase | RegexOptions.Singleline))
                {
                    return (true, "XSS", pattern);
                }
            }

            return (false, "", "");
        }

        public (bool IsAttack, string AttackType, string Pattern) DetectPathTraversal(string input)
        {
            if (string.IsNullOrWhiteSpace(input))
                return (false, "", "");

            foreach (var pattern in PathTraversalPatterns)
            {
                if (Regex.IsMatch(input, pattern, RegexOptions.IgnoreCase))
                {
                    return (true, "PATH_TRAVERSAL", pattern);
                }
            }

            return (false, "", "");
        }

        public bool IsMaliciousUserAgent(string userAgent)
        {
            if (string.IsNullOrWhiteSpace(userAgent))
                return false;

            return MaliciousUserAgents.Any(agent => 
                userAgent.Contains(agent, StringComparison.OrdinalIgnoreCase));
        }
    }
}