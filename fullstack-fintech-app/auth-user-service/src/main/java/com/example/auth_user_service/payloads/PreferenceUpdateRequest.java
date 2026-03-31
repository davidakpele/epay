package com.example.auth_user_service.payloads;

public class PreferenceUpdateRequest {
    private String language;
    private String timezone;

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}
