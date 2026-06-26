package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IBulkOperationJobService;
import com.example.admin_api_service.Interfaces.IBulkOperationRecordService;
import com.example.admin_api_service.enums.BulkRecordStatus;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.accessAndApprovals.BulkOperationRecord;
import com.example.admin_api_service.repository.BulkOperationRecordRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class BulkOperationRecordServiceImpl implements IBulkOperationRecordService {

    private final BulkOperationRecordRepository recordRepository;
    private final IBulkOperationJobService jobService;

    public BulkOperationRecordServiceImpl(BulkOperationRecordRepository recordRepository,
                                          IBulkOperationJobService jobService) {
        this.recordRepository = recordRepository;
        this.jobService = jobService;
    }

    @Override
    public BulkOperationRecord createRecord(String jobId, int rowIndex, String targetId, String inputPayload) {
        jobService.getJobById(jobId); // validate job exists
        BulkOperationRecord record = new BulkOperationRecord();
        record.setJobId(jobId);
        record.setRowIndex(rowIndex);
        record.setTargetId(targetId);
        record.setInputPayload(inputPayload);
        record.setStatus(BulkRecordStatus.PENDING);
        return recordRepository.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    public BulkOperationRecord getRecordById(String recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("BulkOperationRecord", "id", recordId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BulkOperationRecord> getRecordsByJob(String jobId, Pageable pageable) {
        return recordRepository.findAllByJobId(jobId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BulkOperationRecord> getRecordsByJobAndStatus(String jobId, BulkRecordStatus status, Pageable pageable) {
        return recordRepository.findAllByJobIdAndStatus(jobId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BulkOperationRecord> getFailedRecordsForJob(String jobId) {
        return recordRepository.findAllByJobIdAndStatus(jobId, BulkRecordStatus.FAILED);
    }

    @Override
    public BulkOperationRecord markSuccess(String recordId, String resultPayload, String historyReferenceId) {
        BulkOperationRecord record = getRecordById(recordId);
        record.setStatus(BulkRecordStatus.SUCCESS);
        record.setResultPayload(resultPayload);
        record.setHistoryReferenceId(historyReferenceId);
        record.setProcessedAt(LocalDateTime.now());
        record.setUpdatedOn(LocalDateTime.now());

        BulkOperationRecord saved = recordRepository.save(record);
        jobService.updateProgress(record.getJobId(), true);
        return saved;
    }

    @Override
    public BulkOperationRecord markFailed(String recordId, String errorCode, String errorMessage) {
        BulkOperationRecord record = getRecordById(recordId);
        record.setStatus(BulkRecordStatus.FAILED);
        record.setErrorCode(errorCode);
        record.setErrorMessage(errorMessage);
        record.setProcessedAt(LocalDateTime.now());
        record.setUpdatedOn(LocalDateTime.now());

        BulkOperationRecord saved = recordRepository.save(record);
        jobService.updateProgress(record.getJobId(), false);
        return saved;
    }

    @Override
    public BulkOperationRecord markSkipped(String recordId, String reason) {
        BulkOperationRecord record = getRecordById(recordId);
        record.setStatus(BulkRecordStatus.SKIPPED);
        record.setErrorMessage(reason);
        record.setProcessedAt(LocalDateTime.now());
        record.setUpdatedOn(LocalDateTime.now());
        return recordRepository.save(record);
    }

    @Override
    public BulkOperationRecord retryRecord(String recordId) {
        BulkOperationRecord record = getRecordById(recordId);
        if (record.getStatus() != BulkRecordStatus.FAILED) {
            throw new BadRequestException("Only failed records can be retried");
        }
        record.setStatus(BulkRecordStatus.RETRYING);
        record.setRetryCount(record.getRetryCount() + 1);
        record.setErrorCode(null);
        record.setErrorMessage(null);
        record.setUpdatedOn(LocalDateTime.now());
        return recordRepository.save(record);
    }

    @Override
    public void createRecordsBatch(String jobId, List<String> targetIds, List<String> inputPayloads) {
        if (targetIds.size() != inputPayloads.size()) {
            throw new BadRequestException("targetIds and inputPayloads must have the same size");
        }
        jobService.getJobById(jobId); // validate

        List<BulkOperationRecord> records = new ArrayList<>();
        for (int i = 0; i < targetIds.size(); i++) {
            BulkOperationRecord record = new BulkOperationRecord();
            record.setJobId(jobId);
            record.setRowIndex(i + 1);
            record.setTargetId(targetIds.get(i));
            record.setInputPayload(inputPayloads.get(i));
            record.setStatus(BulkRecordStatus.PENDING);
            records.add(record);
        }
        recordRepository.saveAll(records);
    }
}
