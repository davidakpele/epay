package com.epay.domain.bank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class PaystackDtos {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PayStackBankList {
        private int id;
        private String name;
        private String slug;
        private String code;
        private String longcode;
        private String gateway;

        @JsonProperty("pay_with_bank")
        private boolean payWithBank;

        @JsonProperty("supports_transfer")
        private boolean supportsTransfer;

        private boolean active;
        private String country;
        private String currency;
        private String type;

        @JsonProperty("is_deleted")
        private boolean isDeleted;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaystackBankResponse {
        private boolean status;
        private String message;
        private List<PayStackBankList> data = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaystackAccountData {
        @JsonProperty("account_number")
        private String accountNumber;

        @JsonProperty("account_name")
        private String accountName;

        @JsonProperty("bank_id")
        private int bankId;
        
        @JsonProperty("bank_code")
        private String bankCode;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaystackAccountResponse {
        private boolean status;
        private String message;
        private PaystackAccountData data;
    }
}