package pesco.example.withdraw_service.dtos;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data 
@Builder
public class DeductWalletRequestDTO {

    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Recipient username is required")
    @Size(min = 3, max = 50, message = "Recipient username must be between 3 and 50 characters")
    private String recipientUsername;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    private String note; 

    @NotNull(message = "Sender Id is required")
    private Long senderUserId;
    
    @NotNull(message = "Wallet Id is required")
    private Long walletId;
    
    @NotBlank(message = "Transfer Pin is required")
    @Size(min = 4, max = 6, message = "Transfer Pin should typically be 4-6 characters")
    private String password; 

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
    
    private String ipAddress;
    private String deviceId;
    private String userAgent;
    private String geoLocation;

    public DeductWalletRequestDTO() {
    }


    public DeductWalletRequestDTO(String username, String recipientUsername, BigDecimal amount, String currency, String note, Long senderUserId, Long walletId, String password, String idempotencyKey, String ipAddress, String deviceId, String userAgent, String geoLocation) {
        this.username = username;
        this.recipientUsername = recipientUsername;
        this.amount = amount;
        this.currency = currency;
        this.note = note;
        this.senderUserId = senderUserId;
        this.walletId = walletId;
        this.password = password;
        this.idempotencyKey = idempotencyKey;
        this.ipAddress = ipAddress;
        this.deviceId = deviceId;
        this.userAgent = userAgent;
        this.geoLocation = geoLocation;
    }
    

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRecipientUsername() {
        return this.recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
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

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getSenderUserId() {
        return this.senderUserId;
    }

    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdempotencyKey() {
        return this.idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getGeoLocation() {
        return this.geoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

}