package com.epay.domain.notification.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginAlertNotification {
    private String email;
    private String fullName;
    private String username;
    private String loginTime;
    private String ipAddress;
    private String deviceInfo;
    private String supportPhone;
    private String supportEmail;
}
