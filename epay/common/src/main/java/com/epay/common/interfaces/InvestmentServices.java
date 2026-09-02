package com.epay.common.interfaces;

import java.math.BigDecimal;
import java.util.List;
import com.epay.domain.investment.dto.InvestmentReturnResponse;
import com.epay.domain.investment.entity.Investment;
import com.epay.domain.investment.enums.InvestmentDuration;
import com.epay.domain.investment.input.CreateInvestmentRequest;

public interface InvestmentServices {

    InvestmentReturnResponse calculateReturns(
            BigDecimal principal,
            InvestmentDuration duration,
            String currencyCode
    );

    Investment createInvestment(CreateInvestmentRequest request);

    Investment getById(Long id, Long userId);

    List<Investment> getByUserId(Long userId);

    void processMaturedInvestments();
}