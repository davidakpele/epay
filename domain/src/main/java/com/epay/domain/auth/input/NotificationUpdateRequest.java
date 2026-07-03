package com.epay.domain.auth.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationUpdateRequest {

    private Boolean emailNotifications;
    private Boolean smsNotifications;
    private Boolean transactionAlerts;
    private Boolean loginAlerts;
    private Boolean marketingEmails;
    private Boolean pushNotifications;
}
