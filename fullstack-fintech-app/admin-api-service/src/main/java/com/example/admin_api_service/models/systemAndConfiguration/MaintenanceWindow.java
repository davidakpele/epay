package com.example.admin_api_service.models.systemAndConfiguration;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.MaintenanceWindowStatus;
import com.example.admin_api_service.enums.MaintenanceWindowType;

@Entity
@Table(name = "maintenance_windows")
public class MaintenanceWindow {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private MaintenanceWindowType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private MaintenanceWindowStatus status = MaintenanceWindowStatus.SCHEDULED;

    // Services affected stored as JSON array e.g. ["payment-service", "kyc-service"]
    @Column(name = "affected_services", columnDefinition = "json")
    private String affectedServices;

    // Features/channels blocked during maintenance stored as JSON array
    @Column(name = "blocked_features", columnDefinition = "json")
    private String blockedFeatures;

    @Column(name = "scheduled_start", nullable = false)
    private LocalDateTime scheduledStart;

    @Column(name = "scheduled_end", nullable = false)
    private LocalDateTime scheduledEnd;

    @Column(name = "actual_start")
    private LocalDateTime actualStart;

    @Column(name = "actual_end")
    private LocalDateTime actualEnd;

    // Message shown to users during the maintenance window
    @Column(name = "user_facing_message", length = 1000)
    private String userFacingMessage;

    // Internal notes for the engineering/ops team
    @Column(name = "internal_notes", length = 1000)
    private String internalNotes;

    // Whether in-flight transactions should be allowed to complete before blocking
    @Column(name = "drain_in_flight", nullable = false)
    private boolean drainInFlight = true;

    // Whether users should be notified before the window begins
    @Column(name = "notify_users", nullable = false)
    private boolean notifyUsers = true;

    @Column(name = "notification_sent_at")
    private LocalDateTime notificationSentAt;

    @Column(name = "created_by", length = 36, nullable = false)
    private String createdBy;

    @Column(name = "approved_by", length = 36)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "cancelled_by", length = 36)
    private String cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public MaintenanceWindow() {
    }

    public MaintenanceWindow(String id, String title, String description, MaintenanceWindowType type, MaintenanceWindowStatus status, String affectedServices, String blockedFeatures, LocalDateTime scheduledStart, LocalDateTime scheduledEnd, LocalDateTime actualStart, LocalDateTime actualEnd, String userFacingMessage, String internalNotes, boolean drainInFlight, boolean notifyUsers, LocalDateTime notificationSentAt, String createdBy, String approvedBy, LocalDateTime approvedAt, String cancelledBy, LocalDateTime cancelledAt, String cancellationReason, LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.affectedServices = affectedServices;
        this.blockedFeatures = blockedFeatures;
        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
        this.actualStart = actualStart;
        this.actualEnd = actualEnd;
        this.userFacingMessage = userFacingMessage;
        this.internalNotes = internalNotes;
        this.drainInFlight = drainInFlight;
        this.notifyUsers = notifyUsers;
        this.notificationSentAt = notificationSentAt;
        this.createdBy = createdBy;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.cancelledBy = cancelledBy;
        this.cancelledAt = cancelledAt;
        this.cancellationReason = cancellationReason;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MaintenanceWindowType getType() { return type; }
    public void setType(MaintenanceWindowType type) { this.type = type; }

    public MaintenanceWindowStatus getStatus() { return status; }
    public void setStatus(MaintenanceWindowStatus status) { this.status = status; }

    public String getAffectedServices() { return affectedServices; }
    public void setAffectedServices(String affectedServices) { this.affectedServices = affectedServices; }

    public String getBlockedFeatures() { return blockedFeatures; }
    public void setBlockedFeatures(String blockedFeatures) { this.blockedFeatures = blockedFeatures; }

    public LocalDateTime getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(LocalDateTime scheduledStart) { this.scheduledStart = scheduledStart; }

    public LocalDateTime getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(LocalDateTime scheduledEnd) { this.scheduledEnd = scheduledEnd; }

    public LocalDateTime getActualStart() { return actualStart; }
    public void setActualStart(LocalDateTime actualStart) { this.actualStart = actualStart; }

    public LocalDateTime getActualEnd() { return actualEnd; }
    public void setActualEnd(LocalDateTime actualEnd) { this.actualEnd = actualEnd; }

    public String getUserFacingMessage() { return userFacingMessage; }
    public void setUserFacingMessage(String userFacingMessage) { this.userFacingMessage = userFacingMessage; }

    public String getInternalNotes() { return internalNotes; }
    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }

    public boolean isDrainInFlight() { return drainInFlight; }
    public void setDrainInFlight(boolean drainInFlight) { this.drainInFlight = drainInFlight; }

    public boolean isNotifyUsers() { return notifyUsers; }
    public void setNotifyUsers(boolean notifyUsers) { this.notifyUsers = notifyUsers; }

    public LocalDateTime getNotificationSentAt() { return notificationSentAt; }
    public void setNotificationSentAt(LocalDateTime notificationSentAt) { this.notificationSentAt = notificationSentAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }
}