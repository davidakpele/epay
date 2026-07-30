package com.epay.domain.investment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InvestmentReturnResponse {

    private BigDecimal principal;
    private String duration;
    private Integer durationDays;
    private BigDecimal annualRate;
    private BigDecimal expectedProfit;
    private BigDecimal totalPayout;
    private String currencyCode;
    private LocalDate maturityDate;

    public InvestmentReturnResponse() {
    }

    public InvestmentReturnResponse(BigDecimal principal, String duration, Integer durationDays, BigDecimal annualRate, BigDecimal expectedProfit, BigDecimal totalPayout, String currencyCode, LocalDate maturityDate) {
        this.principal = principal;
        this.duration = duration;
        this.durationDays = durationDays;
        this.annualRate = annualRate;
        this.expectedProfit = expectedProfit;
        this.totalPayout = totalPayout;
        this.currencyCode = currencyCode;
        this.maturityDate = maturityDate;
    }

    public BigDecimal getPrincipal() {
        return this.principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public String getDuration() {
        return this.duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getDurationDays() {
        return this.durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public BigDecimal getAnnualRate() {
        return this.annualRate;
    }

    public void setAnnualRate(BigDecimal annualRate) {
        this.annualRate = annualRate;
    }

    public BigDecimal getExpectedProfit() {
        return this.expectedProfit;
    }

    public void setExpectedProfit(BigDecimal expectedProfit) {
        this.expectedProfit = expectedProfit;
    }

    public BigDecimal getTotalPayout() {
        return this.totalPayout;
    }

    public void setTotalPayout(BigDecimal totalPayout) {
        this.totalPayout = totalPayout;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public LocalDate getMaturityDate() {
        return this.maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

}