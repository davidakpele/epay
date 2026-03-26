package com.example.admin_api_service.payloads;

import java.time.LocalDate;
import com.example.admin_api_service.enums.ComplianceReportFormat;
import com.example.admin_api_service.enums.ComplianceReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateComplianceReportPayload {
    @NotNull(message = "Report type is required")
    private ComplianceReportType reportType;
 
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;
 
    @NotNull(message = "Period start is required")
    private LocalDate periodStart;
 
    @NotNull(message = "Period end is required")
    private LocalDate periodEnd;
 
    @Size(max = 100)
    private String regulatoryBody;
 
    @NotNull(message = "Format is required")
    private ComplianceReportFormat format;
 
    private LocalDate dueDate;
 
    public ComplianceReportType getReportType() { return reportType; }
    public void setReportType(ComplianceReportType reportType) { this.reportType = reportType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public String getRegulatoryBody() { return regulatoryBody; }
    public void setRegulatoryBody(String regulatoryBody) { this.regulatoryBody = regulatoryBody; }
    public ComplianceReportFormat getFormat() { return format; }
    public void setFormat(ComplianceReportFormat format) { this.format = format; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
