package com.epay.domain.notification.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotUsernameNotification {
    private String email;
    private String username;
    private String fullName;
}
