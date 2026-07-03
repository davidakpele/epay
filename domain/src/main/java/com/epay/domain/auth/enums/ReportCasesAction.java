package com.epay.domain.auth.enums;

public enum ReportCasesAction {
    DELETE_MY_ACCOUNT("Delete my account associated with the report"),
    SUSPEND_MY_ACCOUNT("Suspend my account associated with the report"),
    MARK_AS_RESOLVED("Mark the report case as resolved");
    private final String description;
    ReportCasesAction(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

}
