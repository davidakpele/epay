package com.example.admin_api_service.dto;

import com.example.admin_api_service.responses.TransactionHistory;
import com.example.admin_api_service.responses.UserAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedTransactionDTO {
    private TransactionHistory history;
    private UserAccount initiator;      
    private UserAccount counterparty;

}
