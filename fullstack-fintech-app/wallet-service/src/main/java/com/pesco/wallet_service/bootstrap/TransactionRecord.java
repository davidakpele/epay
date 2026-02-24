package com.pesco.wallet_service.bootstrap;

import java.math.BigDecimal;
import lombok.Data;

@Data

public class TransactionRecord {
  private String id;
  private String type; // DEPOSIT, WITHDRAW, SWAP, etc.
  private BigDecimal amount;
  private String currency; // NGN, USD, etc.
  private String status; // PENDING, SUCCESS, FAILED
  private String timestamp;
  private String description;

  public TransactionRecord() {
  }

  public TransactionRecord(String id, String type, BigDecimal amount, String currency, String status, String timestamp, String description) {
    this.id = id;
    this.type = type;
    this.amount = amount;
    this.currency = currency;
    this.status = status;
    this.timestamp = timestamp;
    this.description = description;
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public BigDecimal getAmount() {
    return this.amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return this.currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getTimestamp() {
    return this.timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
