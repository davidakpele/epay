package com.pesco.wallet_service.bootstrap;

import java.util.Map;
import com.pesco.wallet_service.dtos.MessageDTO;
import com.pesco.wallet_service.dtos.WalletSection;
import com.pesco.wallet_service.encryptions.EncryptedSignature;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class UserSessionData {
    private DataSection data;
    private HistorySection history;
    private WalletSection wallet;
    private Map<String, MessageDTO> messages;
    private EncryptedSignature encrypted_signature;


    public UserSessionData() {
    }

    public UserSessionData(DataSection data, HistorySection history, WalletSection wallet, Map<String,MessageDTO> messages, EncryptedSignature encrypted_signature) {
        this.data = data;
        this.history = history;
        this.wallet = wallet;
        this.messages = messages;
        this.encrypted_signature = encrypted_signature;
    }


    public DataSection getData() {
        return this.data;
    }

    public void setData(DataSection data) {
        this.data = data;
    }

    public HistorySection getHistory() {
        return this.history;
    }

    public void setHistory(HistorySection history) {
        this.history = history;
    }

    public WalletSection getWallet() {
        return this.wallet;
    }

    public void setWallet(WalletSection wallet) {
        this.wallet = wallet;
    }

    public Map<String,MessageDTO> getMessages() {
        return this.messages;
    }

    public void setMessages(Map<String,MessageDTO> messages) {
        this.messages = messages;
    }

    public EncryptedSignature getEncrypted_signature() {
        return this.encrypted_signature;
    }

    public void setEncrypted_signature(EncryptedSignature encrypted_signature) {
        this.encrypted_signature = encrypted_signature;
    }

}
