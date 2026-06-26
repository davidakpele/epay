package com.pesco.wallet_service.bootstrap;

import com.pesco.wallet_service.dtos.UserDTO;
import lombok.Data;

@Data
public class DataSection {
    private String session_date;
    private String sessionId;
    private UserDTO userDetails;
    public DataSection() {
    }

    public DataSection(String session_date, String sessionId, UserDTO userDetails) {
        this.session_date = session_date;
        this.sessionId = sessionId;
        this.userDetails = userDetails;
    }

    public String getSession_date() {
        return this.session_date;
    }

    public void setSession_date(String session_date) {
        this.session_date = session_date;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public UserDTO getUserDetails() {
        return this.userDetails;
    }

    public void setUserDetails(UserDTO userDetails) {
        this.userDetails = userDetails;
    }

}
