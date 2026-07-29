package com.epay.domain.bank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

public class PaystackDtos {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PayStackBankList {
        private String name;
        private String code;
        private String longcode;
        private String currency;
        private String type;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaystackBankResponse {
        private boolean status;
        private String message;
        private List<PayStackBankList> data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaystackAccountData {
        private String accountNumber;
        private String accountName;
        private Long bankId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaystackAccountResponse {
        private boolean status;
        private String message;
        private PaystackAccountData data;
    }
}