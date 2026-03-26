package pesco.example.withdraw_service.serviceImp;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.handler.timeout.TimeoutException;
import pesco.example.withdraw_service.components.IpExtractor;

@Service
public class GeoLocationService {

    private static final Logger log = LoggerFactory.getLogger(GeoLocationService.class);

    private final WebClient ipApiClient;
    private final IpExtractor ipExtractor;
    private final Cache<String, String> geoCache;

    @Value("${geolocation.ip-api.timeout-seconds:3}")
    private int timeoutSeconds;

    public GeoLocationService(
            @Value("${geolocation.ip-api.base-url:http://ip-api.com}") String baseUrl,
            @Value("${geolocation.cache.max-size:5000}") int maxSize,
            @Value("${geolocation.cache.expire-hours:24}") int expireHours,
            IpExtractor ipExtractor) {

        this.ipExtractor = ipExtractor;
        this.ipApiClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        this.geoCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireHours, TimeUnit.HOURS)
                .recordStats()
                .build();
    }

    /**
     * Primary: use client-provided coords for reverse geocoding.
     * Fallback: resolve from IP address.
     */
    public String resolve(String clientGeoHeader, String ipAddress) {

        // ── 1. Client sent GPS coordinates ──────────────────────────
        if (clientGeoHeader != null && !clientGeoHeader.isBlank()) {
            log.debug("[GeoLocation] Using client-provided location: {}", clientGeoHeader);
            return sanitize(clientGeoHeader);
        }

        // ── 2. Internal/Docker IP — cannot resolve ───────────────────
        if (ipExtractor.isInternalIp(ipAddress)) {
            log.debug("[GeoLocation] Internal IP detected, skipping lookup: {}", ipAddress);
            return "Internal Network";
        }

        // ── 3. Check cache ───────────────────────────────────────────
        String cached = geoCache.getIfPresent(ipAddress);
        if (cached != null) {
            log.debug("[GeoLocation] Cache hit for IP: {}", ipAddress);
            return cached;
        }

        // ── 4. IP API lookup ─────────────────────────────────────────
        String result = lookupFromIp(ipAddress);
        geoCache.put(ipAddress, result);
        return result;
    }

    private String lookupFromIp(String ipAddress) {
        try {
            Map<?, ?> response = ipApiClient.get()
                    .uri("/json/{ip}?fields=status,message,country,regionName,city,lat,lon,isp",
                            ipAddress)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response == null) {
                log.warn("[GeoLocation] Null response for IP: {}", ipAddress);
                return "Unknown";
            }

            if (!"success".equals(response.get("status"))) {
                log.warn("[GeoLocation] API failure for IP {}: {}", ipAddress, response.get("message"));
                return "Unknown";
            }

            String city    = nullSafe(response.get("city"));
            String region  = nullSafe(response.get("regionName"));
            String country = nullSafe(response.get("country"));
            double lat     = toDouble(response.get("lat"));
            double lon     = toDouble(response.get("lon"));

            String location = String.format("%s, %s, %s (%.4f, %.4f)",
                    city, region, country, lat, lon);

            log.info("[GeoLocation] Resolved IP {} → {}", ipAddress, location);
            return location;

        } catch (TimeoutException e) {
            log.warn("[GeoLocation] Timeout resolving IP: {}", ipAddress);
            return "Unknown";
        } catch (Exception e) {
            log.error("[GeoLocation] Error resolving IP {}: {}", ipAddress, e.getMessage());
            return "Unknown";
        }
    }

    private String sanitize(String input) {
        // prevent injection — strip anything dangerous
        return input.replaceAll("[^a-zA-Z0-9(),.\\s\\-+]", "").trim();
    }

    private String nullSafe(Object val) {
        return val == null ? "" : val.toString();
    }

    private double toDouble(Object val) {
        if (val instanceof Number n) return n.doubleValue();
        return 0.0;
    }
}
