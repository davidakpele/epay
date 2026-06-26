package com.example.admin_api_service.Interfaces;

import com.example.admin_api_service.enums.SanctionListStatus;
import com.example.admin_api_service.enums.SanctionListType;
import com.example.admin_api_service.models.complianceAndRisk.SanctionList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ISanctionListService {
    SanctionList addEntry(SanctionList entry);
 
    SanctionList updateEntry(String entryId, SanctionList updated);
 
    SanctionList getEntryById(String entryId);
 
    Page<SanctionList> getAllEntries(Pageable pageable);
 
    Page<SanctionList> getEntriesByStatus(SanctionListStatus status, Pageable pageable);
 
    Page<SanctionList> getEntriesByType(SanctionListType listType, Pageable pageable);
 
    Page<SanctionList> getEntriesByAuthority(String issuingAuthority, Pageable pageable);
 
    // Fuzzy name search — returns candidates above the match threshold
    List<SanctionList> searchByName(String name, double minimumMatchScore);
 
    void delistEntry(String entryId);
 
    void bulkSyncFromProvider(List<SanctionList> entries, String issuingAuthority);
 
    long countActiveEntries();
}
