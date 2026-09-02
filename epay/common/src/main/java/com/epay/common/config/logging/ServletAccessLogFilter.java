package com.epay.common.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;
import com.epay.common.config.security.WebSecurityContext;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
public class ServletAccessLogFilter extends OncePerRequestFilter {
    private static final String SEPARATOR = " | ";

    private final WebSecurityContext webSecurityContext;
    private final LoggingProperties loggingProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final AtomicLong counter = new AtomicLong();


    public ServletAccessLogFilter(WebSecurityContext webSecurityContext, LoggingProperties loggingProperties) {
        this.webSecurityContext = webSecurityContext;
        this.loggingProperties = loggingProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        if (shouldBeLogged(loggingProperties.getExcludedPaths(), httpServletRequest.getServletPath())) {
            doFilterWithLogging(httpServletRequest, httpServletResponse, filterChain);
        } else {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    private void doFilterWithLogging(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        StopWatch stopWatch = new StopWatch();
        String userFormatted = "user: " + getUserId();
        String pathFormatted = "path: " + httpServletRequest.getMethod() + " " + httpServletRequest.getServletPath();
        String paramsFormatted = "params: " + formatParams(httpServletRequest.getParameterMap());

        StringBuilder userPathInfo = new StringBuilder(userFormatted);
        userPathInfo.append(SEPARATOR).append(pathFormatted);

        String uniqueString = "filterId=" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        log.info(String.join(SEPARATOR, "--Start--", userPathInfo.toString(), paramsFormatted, uniqueString));
        stopWatch.start();
        try {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (Exception e) {
            stopWatch.stop();
            long elapsed = stopWatch.getTotalTimeMillis();
            String timeFormatted = String.format("elapsed: %.3fs", elapsed / 1000.0);
            String responseCodeInfo = String.format("status: %d", HttpStatus.INTERNAL_SERVER_ERROR.value());
            log.info(String.join(SEPARATOR, "---End---", userPathInfo.toString(), timeFormatted, responseCodeInfo, uniqueString));
            throw e;
        }
        long elapsed = stopWatch.getTotalTimeMillis();
        String timeFormatted = String.format("elapsed: %.3fs", elapsed / 1000.0);
        String responseCodeInfo = String.format("status: %d", httpServletResponse.getStatus());
        log.info(String.join(SEPARATOR, "---End---", userPathInfo.toString(), timeFormatted, responseCodeInfo, uniqueString));

    }

    private boolean shouldBeLogged(List<String> excludedPaths, String path) {
        return excludedPaths.stream().noneMatch(excludedPath -> antPathMatcher.match(excludedPath, path));
    }

    private String getUserId() {
        return webSecurityContext.getUsername().isEmpty() ? String.valueOf(webSecurityContext.getUserId()) : "[UNKNOWN USER]";
    }

    private String formatParams(Map<String, String[]> params) {
        if ((params == null)) return "";
        List<String> formattedParams = params.entrySet().stream()
                .map(e -> e.getKey() + "=[" + String.join(", ", e.getValue()) + "]")
                .collect(Collectors.toList());

        return String.join(", ", formattedParams);
    }
}
