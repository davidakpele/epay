package com.example.auth_user_service.responses;

public class CacheResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private boolean cached;
    private int count;

    // Constructors
    public CacheResponse() {
    }

    public CacheResponse(boolean success, String message, T data, boolean cached, int count) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.cached = cached;
        this.count = count;
    }

    // Getters and setters only
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}