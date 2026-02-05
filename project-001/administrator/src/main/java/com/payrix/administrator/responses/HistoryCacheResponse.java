package com.payrix.administrator.responses;

import java.util.List;
import com.payrix.administrator.dtos.HistoryData;

public class HistoryCacheResponse {
    private boolean cached;
    private int count;
    private List<HistoryData> data;
    private String source;

    // Getters and Setters
    public boolean isCached() { return cached; }
    public void setCached(boolean cached) { this.cached = cached; }
    
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    
    public List<HistoryData> getData() { return data; }
    public void setData(List<HistoryData> data) { this.data = data; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
