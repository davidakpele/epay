package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.ISanctionListService;
import com.example.admin_api_service.enums.SanctionListStatus;
import com.example.admin_api_service.enums.SanctionListType;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.complianceAndRisk.SanctionList;
import com.example.admin_api_service.repository.SanctionListRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SanctionListServiceImpl implements ISanctionListService {

    private final SanctionListRepository sanctionListRepository;

    public SanctionListServiceImpl(SanctionListRepository sanctionListRepository) {
        this.sanctionListRepository = sanctionListRepository;
    }

    @Override
    public SanctionList addEntry(SanctionList entry) {
        entry.setStatus(SanctionListStatus.ACTIVE);
        entry.setLastSyncedAt(LocalDateTime.now());
        return sanctionListRepository.save(entry);
    }

    @Override
    public SanctionList updateEntry(String entryId, SanctionList updated) {
        SanctionList existing = getEntryById(entryId);
        existing.setName(updated.getName());
        existing.setAliasNames(updated.getAliasNames());
        existing.setNationality(updated.getNationality());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setNationalId(updated.getNationalId());
        existing.setPassportNumber(updated.getPassportNumber());
        existing.setAddress(updated.getAddress());
        existing.setReason(updated.getReason());
        existing.setRawData(updated.getRawData());
        existing.setLastSyncedAt(LocalDateTime.now());
        existing.setUpdatedOn(LocalDateTime.now());
        return sanctionListRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public SanctionList getEntryById(String entryId) {
        return sanctionListRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("SanctionList", "id", entryId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SanctionList> getAllEntries(Pageable pageable) {
        return sanctionListRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SanctionList> getEntriesByStatus(SanctionListStatus status, Pageable pageable) {
        return sanctionListRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SanctionList> getEntriesByType(SanctionListType listType, Pageable pageable) {
        return sanctionListRepository.findAllByListType(listType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SanctionList> getEntriesByAuthority(String issuingAuthority, Pageable pageable) {
        return sanctionListRepository.findAllByIssuingAuthority(issuingAuthority, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SanctionList> searchByName(String name, double minimumMatchScore) {
        // Fetch active entries where name contains any token of the search string
        // In production replace with a proper fuzzy search (Elasticsearch, PostgreSQL pg_trgm, etc.)
        List<SanctionList> candidates = sanctionListRepository
                .findAllByStatusAndNameContainingIgnoreCase(SanctionListStatus.ACTIVE, name);

        return candidates.stream()
                .filter(entry -> calculateMatchScore(name, entry.getName()) >= minimumMatchScore)
                .collect(Collectors.toList());
    }

    @Override
    public void delistEntry(String entryId) {
        SanctionList entry = getEntryById(entryId);
        entry.setStatus(SanctionListStatus.DELISTED);
        entry.setDelistedOn(LocalDateTime.now());
        entry.setUpdatedOn(LocalDateTime.now());
        sanctionListRepository.save(entry);
    }

    @Override
    public void bulkSyncFromProvider(List<SanctionList> entries, String issuingAuthority) {
        // Mark existing entries for this authority as UNDER_REVIEW before overwrite
        sanctionListRepository.updateStatusByIssuingAuthority(
                issuingAuthority, SanctionListStatus.UNDER_REVIEW);

        LocalDateTime syncTime = LocalDateTime.now();
        entries.forEach(entry -> {
            entry.setIssuingAuthority(issuingAuthority);
            entry.setStatus(SanctionListStatus.ACTIVE);
            entry.setLastSyncedAt(syncTime);
        });

        sanctionListRepository.saveAll(entries);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveEntries() {
        return sanctionListRepository.countByStatus(SanctionListStatus.ACTIVE);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private double calculateMatchScore(String query, String candidate) {
        // Simple token overlap score — replace with Jaro-Winkler or Levenshtein in production
        if (query == null || candidate == null) return 0.0;
        String[] queryTokens = query.toLowerCase().split("\\s+");
        String[] candidateTokens = candidate.toLowerCase().split("\\s+");
        long matches = 0;
        for (String qt : queryTokens) {
            for (String ct : candidateTokens) {
                if (ct.contains(qt) || qt.contains(ct)) {
                    matches++;
                    break;
                }
            }
        }
        return (double) matches / Math.max(queryTokens.length, candidateTokens.length);
    }
}
