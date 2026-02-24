package com.pesco.wallet_service.encryptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EncryptedSignature {
    private String iv;
    private String encryptedData;
}
