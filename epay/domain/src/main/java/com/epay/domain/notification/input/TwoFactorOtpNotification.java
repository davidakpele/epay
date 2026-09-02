
package com.epay.domain.notification.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorOtpNotification {
    private String email;
    private String otp;
    private String resetPasswordUrl;
    private String config2faUrl;
    private String config2faRecoveryUrl;
}
