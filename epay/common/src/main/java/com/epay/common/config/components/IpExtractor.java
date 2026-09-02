package com.epay.common.config.components;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.epay.common.config.object_mapper.RequestMetadata;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class IpExtractor {

    private static final Logger log = LoggerFactory.getLogger(IpExtractor.class);

    private static final List<String> IP_HEADERS = List.of(
            "X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP",    
            "X-Original-Forwarded-For", "Proxy-Client-IP",
            "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED"
    );

    public RequestMetadata extractMetadata(HttpServletRequest request) {
        return new RequestMetadata(extract(request), extractDevice(request));
    }

    public String extract(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip) && isValid(ip)){
                String clientIp = ip.split(",")[0].trim();
                log.debug("[IpExtractor] Resolved IP from header {}: {}", header, clientIp);
                return clientIp;
            }
        }
        String remoteAddr = request.getRemoteAddr();
        log.debug("[IpExtractor] Falling back to remoteAddr: {}", remoteAddr);
        return remoteAddr;
    }

    public String extractDevice(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null || ua.isBlank()) return "Unknown";
        String lower = ua.toLowerCase();
        if (lower.contains("mobile") || lower.contains("android") || lower.contains("iphone")) return "Mobile";
        if (lower.contains("tablet") || lower.contains("ipad")) return "Tablet";
        return "Desktop";
    }

    public boolean isInternalIp(String ip) {
        return ip == null
                || ip.equals("127.0.0.1")
                || ip.equals("0:0:0:0:0:0:0:1")
                || ip.startsWith("10.")
                || ip.startsWith("172.")
                || ip.startsWith("192.168.");
    }

    private boolean isValid(String value) {
        return value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value);
    }
}