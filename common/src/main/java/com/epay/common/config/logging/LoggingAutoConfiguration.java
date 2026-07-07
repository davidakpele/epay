package com.epay.common.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.epay.common.config.security.WebSecurityContext;

@Configuration
@EnableConfigurationProperties(LoggingProperties.class)
@Slf4j
@ConditionalOnProperty(prefix = "epay.logging", name = "enabled", havingValue = "true")
public class LoggingAutoConfiguration {

    static final String HTTP_REQUEST_HEADER_FILTER_LOG_REG_BEAN_NAME = "HTTP_REQUEST_HEADER_FILTER_LOG_REG_BEAN_NAME";
    static final String SERVLET_ACCESS_LOG_REG_BEAN_NAME = "SERVLET_ACCESS_LOG_REG_BEAN_NAME";

    @Bean(name = HTTP_REQUEST_HEADER_FILTER_LOG_REG_BEAN_NAME)
    public FilterRegistrationBean<HttpRequestHeaderLogFilter> registerHttpRequestHeaderLogFilter() {
        log.info("Initializing http header log filter");
        FilterRegistrationBean<HttpRequestHeaderLogFilter> registration = new FilterRegistrationBean<>();
        HttpRequestHeaderLogFilter filter = new HttpRequestHeaderLogFilter();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean(name = SERVLET_ACCESS_LOG_REG_BEAN_NAME)
    public FilterRegistrationBean<ServletAccessLogFilter> registerServletAccessLogFilter(LoggingProperties loggingProperties,
                                                                                         @Autowired WebSecurityContext webSecurityContext) {
        log.info("Initializing servlet access log filter");
        FilterRegistrationBean<ServletAccessLogFilter> registration = new FilterRegistrationBean<>();
        ServletAccessLogFilter filter = new ServletAccessLogFilter(webSecurityContext, loggingProperties);
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setName("Access Log Filter");
        return registration;
    }
}
