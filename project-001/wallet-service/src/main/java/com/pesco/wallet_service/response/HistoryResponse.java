package com.pesco.wallet_service.response;

import java.util.List;
import com.pesco.wallet_service.dtos.HistoryDTO;

public class HistoryResponse {
    private boolean success;
    private List<HistoryDTO> data;
    private String error;
    private int count;

    // Constructors
    public HistoryResponse() {}

    public HistoryResponse(boolean success, List<HistoryDTO> data) {
        this.success = success;
        this.data = data;
        this.count = data != null ? data.size() : 0;
    }

    public HistoryResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
        this.count = 0;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public List<HistoryDTO> getData() { return data; }
    public void setData(List<HistoryDTO> data) { 
        this.data = data; 
        this.count = data != null ? data.size() : 0;
    }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}