package pesco.example.withdraw_service.components;


import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class IpExtractor {

    private static final Logger log = LoggerFactory.getLogger(IpExtractor.class);

    private static final List<String> IP_HEADERS = List.of(
            "X-Forwarded-For",
            "X-Real-IP",
            "CF-Connecting-IP",      // Cloudflare
            "X-Original-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    );

    public String extract(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                String clientIp = ip.split(",")[0].trim();
                log.debug("[IpExtractor] Resolved IP from header {}: {}", header, clientIp);
                return clientIp;
            }
        }
        String remoteAddr = request.getRemoteAddr();
        log.debug("[IpExtractor] Falling back to remoteAddr: {}", remoteAddr);
        return remoteAddr;
    }

    public boolean isInternalIp(String ip) {
        return ip == null
                || ip.equals("127.0.0.1")
                || ip.equals("0:0:0:0:0:0:0:1")
                || ip.startsWith("10.")
                || ip.startsWith("172.")
                || ip.startsWith("192.168.");
    }
}