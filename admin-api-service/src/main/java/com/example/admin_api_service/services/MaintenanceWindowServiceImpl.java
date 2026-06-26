package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IMaintenanceWindowService;
import com.example.admin_api_service.enums.MaintenanceWindowStatus;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.systemAndConfiguration.MaintenanceWindow;
import com.example.admin_api_service.repository.MaintenanceWindowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MaintenanceWindowServiceImpl implements IMaintenanceWindowService {

    private final MaintenanceWindowRepository maintenanceWindowRepository;

    public MaintenanceWindowServiceImpl(MaintenanceWindowRepository maintenanceWindowRepository) {
        this.maintenanceWindowRepository = maintenanceWindowRepository;
    }

    @Override
    public MaintenanceWindow scheduleWindow(MaintenanceWindow window, String createdBy) {
        if (window.getScheduledStart().isAfter(window.getScheduledEnd())) {
            throw new BadRequestException("scheduledStart must be before scheduledEnd");
        }
        window.setCreatedBy(createdBy);
        window.setStatus(MaintenanceWindowStatus.SCHEDULED);
        return maintenanceWindowRepository.save(window);
    }

    @Override
    public MaintenanceWindow updateWindow(String windowId, MaintenanceWindow updated, String updatedBy) {
        MaintenanceWindow existing = getWindowById(windowId);
        if (existing.getStatus() == MaintenanceWindowStatus.IN_PROGRESS) {
            throw new ConflictException("Cannot update a maintenance window that is currently in progress");
        }
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setType(updated.getType());
        existing.setAffectedServices(updated.getAffectedServices());
        existing.setBlockedFeatures(updated.getBlockedFeatures());
        existing.setScheduledStart(updated.getScheduledStart());
        existing.setScheduledEnd(updated.getScheduledEnd());
        existing.setUserFacingMessage(updated.getUserFacingMessage());
        existing.setInternalNotes(updated.getInternalNotes());
        existing.setNotifyUsers(updated.isNotifyUsers());
        existing.setUpdatedOn(LocalDateTime.now());
        return maintenanceWindowRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceWindow getWindowById(String windowId) {
        return maintenanceWindowRepository.findById(windowId)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceWindow", "id", windowId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceWindow> getAllWindows(Pageable pageable) {
        return maintenanceWindowRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceWindow> getWindowsByStatus(MaintenanceWindowStatus status, Pageable pageable) {
        return maintenanceWindowRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceWindow> getUpcomingWindows() {
        return maintenanceWindowRepository.findAllByStatusAndScheduledStartAfterOrderByScheduledStartAsc(
                MaintenanceWindowStatus.SCHEDULED, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MaintenanceWindow> getActiveWindow() {
        return maintenanceWindowRepository.findFirstByStatus(MaintenanceWindowStatus.IN_PROGRESS);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSystemUnderMaintenance() {
        return maintenanceWindowRepository.existsByStatus(MaintenanceWindowStatus.IN_PROGRESS);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFeatureBlocked(String featureName) {
        return maintenanceWindowRepository
                .findFirstByStatus(MaintenanceWindowStatus.IN_PROGRESS)
                .map(w -> w.getBlockedFeatures() != null
                        && w.getBlockedFeatures().contains(featureName))
                .orElse(false);
    }

    @Override
    public MaintenanceWindow approveWindow(String windowId, String approvedBy) {
        MaintenanceWindow window = getWindowById(windowId);
        if (window.getStatus() != MaintenanceWindowStatus.SCHEDULED) {
            throw new ConflictException("Only SCHEDULED windows can be approved");
        }
        window.setApprovedBy(approvedBy);
        window.setApprovedAt(LocalDateTime.now());
        window.setUpdatedOn(LocalDateTime.now());
        return maintenanceWindowRepository.save(window);
    }

    @Override
    public MaintenanceWindow startWindow(String windowId) {
        MaintenanceWindow window = getWindowById(windowId);
        if (window.getStatus() != MaintenanceWindowStatus.SCHEDULED) {
            throw new ConflictException("Window must be SCHEDULED to start");
        }
        window.setStatus(MaintenanceWindowStatus.IN_PROGRESS);
        window.setActualStart(LocalDateTime.now());
        window.setUpdatedOn(LocalDateTime.now());
        return maintenanceWindowRepository.save(window);
    }

    @Override
    public MaintenanceWindow completeWindow(String windowId) {
        MaintenanceWindow window = getWindowById(windowId);
        if (window.getStatus() != MaintenanceWindowStatus.IN_PROGRESS) {
            throw new ConflictException("Window must be IN_PROGRESS to complete");
        }
        window.setStatus(MaintenanceWindowStatus.COMPLETED);
        window.setActualEnd(LocalDateTime.now());
        window.setUpdatedOn(LocalDateTime.now());
        return maintenanceWindowRepository.save(window);
    }

    @Override
    public MaintenanceWindow extendWindow(String windowId, LocalDateTime newEndTime, String updatedBy) {
        MaintenanceWindow window = getWindowById(windowId);
        if (window.getStatus() != MaintenanceWindowStatus.IN_PROGRESS) {
            throw new ConflictException("Only IN_PROGRESS windows can be extended");
        }
        window.setScheduledEnd(newEndTime);
        window.setStatus(MaintenanceWindowStatus.EXTENDED);
        window.setUpdatedOn(LocalDateTime.now());
        return maintenanceWindowRepository.save(window);
    }

    @Override
    public MaintenanceWindow cancelWindow(String windowId, String cancelledBy, String reason) {
        MaintenanceWindow window = getWindowById(windowId);
        if (window.getStatus() == MaintenanceWindowStatus.COMPLETED
                || window.getStatus() == MaintenanceWindowStatus.CANCELLED) {
            throw new ConflictException("Window is already in a terminal state: " + window.getStatus());
        }
        window.setStatus(MaintenanceWindowStatus.CANCELLED);
        window.setCancelledBy(cancelledBy);
        window.setCancelledAt(LocalDateTime.now());
        window.setCancellationReason(reason);
        window.setUpdatedOn(LocalDateTime.now());
        return maintenanceWindowRepository.save(window);
    }

    @Override
    public void markNotificationSent(String windowId) {
        MaintenanceWindow window = getWindowById(windowId);
        window.setNotificationSentAt(LocalDateTime.now());
        maintenanceWindowRepository.save(window);
    }

    @Override
    @Scheduled(fixedDelay = 60000) // every minute
    public void autoStartScheduledWindows() {
        List<MaintenanceWindow> due = maintenanceWindowRepository
                .findAllByStatusAndScheduledStartBeforeOrEqual(
                        MaintenanceWindowStatus.SCHEDULED, LocalDateTime.now());
        due.forEach(w -> {
            w.setStatus(MaintenanceWindowStatus.IN_PROGRESS);
            w.setActualStart(LocalDateTime.now());
            w.setUpdatedOn(LocalDateTime.now());
        });
        if (!due.isEmpty()) maintenanceWindowRepository.saveAll(due);
    }

    @Override
    @Scheduled(fixedDelay = 60000) // every minute
    public void autoCompleteExpiredWindows() {
        List<MaintenanceWindow> expired = maintenanceWindowRepository
                .findAllByStatusAndScheduledEndBefore(
                        MaintenanceWindowStatus.IN_PROGRESS, LocalDateTime.now());
        expired.forEach(w -> {
            w.setStatus(MaintenanceWindowStatus.COMPLETED);
            w.setActualEnd(LocalDateTime.now());
            w.setUpdatedOn(LocalDateTime.now());
        });
        if (!expired.isEmpty()) maintenanceWindowRepository.saveAll(expired);
    }
}
