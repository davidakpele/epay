package com.pesco.wallet_service.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating a supported currency.
 */
public class CurrencyRequest {

    @NotBlank(message = "Currency code is required")
    @Pattern(regexp = "^[A-Za-z]{2,10}$", message = "Currency code must be 2–10 letters")
    private String code;

    @NotBlank(message = "Symbol is required")
    @Size(max = 10, message = "Symbol must be at most 10 characters")
    private String symbol;

    @NotBlank(message = "Name is required")
    @Size(max = 60, message = "Name must be at most 60 characters")
    private String name;

    private Boolean enabled = true;

    public CurrencyRequest() {}

    public String  getCode()               { return code; }
    public void    setCode(String code)    { this.code = code; }

    public String  getSymbol()             { return symbol; }
    public void    setSymbol(String v)     { this.symbol = v; }

    public String  getName()               { return name; }
    public void    setName(String v)       { this.name = v; }

    public Boolean getEnabled()            { return enabled; }
    public void    setEnabled(Boolean v)   { this.enabled = v; }
}
