package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IAdminIpWhitelistService;
import com.example.admin_api_service.enums.IpWhitelistScope;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.accessAndSecurity.AdminIpWhitelist;
import com.example.admin_api_service.repository.AdminIpWhitelistRepository;

import org.apache.commons.net.util.SubnetUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AdminIpWhitelistServiceImpl implements IAdminIpWhitelistService {

    private final AdminIpWhitelistRepository ipWhitelistRepository;

    public AdminIpWhitelistServiceImpl(AdminIpWhitelistRepository ipWhitelistRepository) {
        this.ipWhitelistRepository = ipWhitelistRepository;
    }

    @Override
    public AdminIpWhitelist addToWhitelist(AdminIpWhitelist entry, String createdBy) {
        boolean duplicate = ipWhitelistRepository
                .existsByAdminUserIdAndIpAddressOrCidrAndIsActiveTrue(
                        entry.getAdminUserId(), entry.getIpAddressOrCidr());
        if (duplicate) {
            throw new ConflictException("This IP/CIDR is already whitelisted for this admin");
        }
        entry.setCreatedBy(createdBy);
        entry.setActive(true);
        return ipWhitelistRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminIpWhitelist getWhitelistEntryById(String id) {
        return ipWhitelistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdminIpWhitelist", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminIpWhitelist> getAllWhitelistEntries(Pageable pageable) {
        return ipWhitelistRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminIpWhitelist> getWhitelistEntriesForAdmin(String adminUserId) {
        return ipWhitelistRepository.findAllByAdminUserIdAndIsActiveTrue(adminUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminIpWhitelist> getGlobalWhitelistEntries() {
        return ipWhitelistRepository.findAllByScopeAndIsActiveTrue(IpWhitelistScope.GLOBAL);
    }

    @Override
    public void revokeWhitelistEntry(String id, String revokedBy, String revokedReason) {
        AdminIpWhitelist entry = getWhitelistEntryById(id);
        entry.setActive(false);
        entry.setRevokedBy(revokedBy);
        entry.setRevokedAt(LocalDateTime.now());
        entry.setRevokedReason(revokedReason);
        ipWhitelistRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isIpAllowed(String ipAddress, String adminUserId) {
        // Check global rules first
        if (isGlobalIpAllowed(ipAddress)) return true;

        // Check individual admin rules
        List<AdminIpWhitelist> entries = ipWhitelistRepository
                .findAllByAdminUserIdAndIsActiveTrue(adminUserId);

        return entries.stream()
                .filter(e -> e.getExpiresAt() == null || e.getExpiresAt().isAfter(LocalDateTime.now()))
                .anyMatch(e -> matchesIpOrCidr(ipAddress, e.getIpAddressOrCidr()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isGlobalIpAllowed(String ipAddress) {
        List<AdminIpWhitelist> globalEntries = ipWhitelistRepository
                .findAllByScopeAndIsActiveTrue(IpWhitelistScope.GLOBAL);

        return globalEntries.stream()
                .filter(e -> e.getExpiresAt() == null || e.getExpiresAt().isAfter(LocalDateTime.now()))
                .anyMatch(e -> matchesIpOrCidr(ipAddress, e.getIpAddressOrCidr()));
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // runs every hour
    public void deleteExpiredEntries() {
        List<AdminIpWhitelist> expired = ipWhitelistRepository
                .findAllByIsActiveTrueAndExpiresAtBefore(LocalDateTime.now());
        expired.forEach(entry -> {
            entry.setActive(false);
            entry.setRevokedAt(LocalDateTime.now());
            entry.setRevokedReason("Auto-expired");
        });
        if (!expired.isEmpty()) {
            ipWhitelistRepository.saveAll(expired);
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private boolean matchesIpOrCidr(String ipAddress, String ipOrCidr) {
        if (!ipOrCidr.contains("/")) {
            return ipAddress.equals(ipOrCidr);
        }
        try {
            SubnetUtils subnet = new SubnetUtils(ipOrCidr);
            subnet.setInclusiveHostCount(true);
            return subnet.getInfo().isInRange(ipAddress);
        } catch (Exception e) {
            return false;
        }
    }
}