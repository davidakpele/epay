package com.pesco.wallet_service.encryptions.battery;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.springframework.stereotype.Service;

import com.pesco.wallet_service.encryptions.EncryptedSignature;

import jakarta.annotation.PostConstruct;

@Service
public class SEC46 {

    private static final String AES = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12; 
    private static final int AES_KEY_SIZE = 256;

    private SecretKey secretKey;

    @PostConstruct
    public void init() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
        keyGenerator.init(AES_KEY_SIZE);
        secretKey = keyGenerator.generateKey();
    }

    public EncryptedSignature data_encryption(Long userId) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] encryptedBytes = cipher.doFinal(userId.toString().getBytes());

            return EncryptedSignature.builder()
                    .iv(Base64.getEncoder().encodeToString(iv))
                    .encryptedData(Base64.getEncoder().encodeToString(encryptedBytes))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }
}