package com.example.admin_api_service.Interfaces;

import com.example.admin_api_service.models.accessAndSecurity.AdminIpWhitelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAdminIpWhitelistService {
    AdminIpWhitelist addToWhitelist(AdminIpWhitelist entry, String createdBy);
 
    AdminIpWhitelist getWhitelistEntryById(String id);
 
    Page<AdminIpWhitelist> getAllWhitelistEntries(Pageable pageable);
 
    List<AdminIpWhitelist> getWhitelistEntriesForAdmin(String adminUserId);
 
    List<AdminIpWhitelist> getGlobalWhitelistEntries();
 
    void revokeWhitelistEntry(String id, String revokedBy, String revokedReason);
 
    boolean isIpAllowed(String ipAddress, String adminUserId);
 
    boolean isGlobalIpAllowed(String ipAddress);
 
    void deleteExpiredEntries();
}
