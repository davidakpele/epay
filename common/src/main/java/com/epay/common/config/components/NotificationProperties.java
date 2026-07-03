package com.epay.common.config.components;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds notification support-contact values from application.yaml.
 *
 * <pre>
 * notification:
 *   support:
 *     phone: "070034335489"
 *     email: "support@epay.com"
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "notification.support")
public class NotificationProperties {

    private String phone = "070034335489";
    private String email = "support@epay.com";

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
 