package com.epay.domain.history.dto;

import com.epay.domain.history.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateTransition {

    private TransactionStatus from;
    private TransactionStatus to;
}
