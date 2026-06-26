package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.example.admin_api_service.enums.MaintenanceWindowStatus;
import com.example.admin_api_service.models.systemAndConfiguration.MaintenanceWindow;

public interface IMaintenanceWindowService {
    MaintenanceWindow scheduleWindow(MaintenanceWindow window, String createdBy);
 
    MaintenanceWindow updateWindow(String windowId, MaintenanceWindow updated, String updatedBy);
 
    MaintenanceWindow getWindowById(String windowId);
 
    Page<MaintenanceWindow> getAllWindows(Pageable pageable);
 
    Page<MaintenanceWindow> getWindowsByStatus(MaintenanceWindowStatus status, Pageable pageable);
 
    List<MaintenanceWindow> getUpcomingWindows();
 
    // Returns the active maintenance window right now, if any
    Optional<MaintenanceWindow> getActiveWindow();
 
    boolean isSystemUnderMaintenance();
 
    boolean isFeatureBlocked(String featureName);
 
    MaintenanceWindow approveWindow(String windowId, String approvedBy);
 
    MaintenanceWindow startWindow(String windowId);
 
    MaintenanceWindow completeWindow(String windowId);
 
    MaintenanceWindow extendWindow(String windowId, LocalDateTime newEndTime, String updatedBy);
 
    MaintenanceWindow cancelWindow(String windowId, String cancelledBy, String reason);
 
    void markNotificationSent(String windowId);
 
    void autoStartScheduledWindows();
 
    void autoCompleteExpiredWindows();
}
