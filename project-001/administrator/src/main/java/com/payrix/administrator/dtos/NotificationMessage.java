package com.payrix.administrator.dtos;

import java.time.LocalDateTime;

public class NotificationMessage {
    private String type;
    private String content;
    private String sender;
    private LocalDateTime timestamp;

    public NotificationMessage() {
    }

    public NotificationMessage(String type, String content, String sender, LocalDateTime timestamp) {
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
   
}
