package pesco.example.withdraw_service.serviceImp;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class HazelcastIdempotencyService {
    
    private final HazelcastInstance hazelcastInstance;
    private final ObjectMapper objectMapper;
    
    private static final String IDEMPOTENCY_MAP = "idempotency-store";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final long PROCESSING_TTL_MINUTES = 5; // 5 minutes
    private static final long COMPLETED_TTL_MINUTES = 24 * 60; // 24 hours
    
    public HazelcastIdempotencyService(HazelcastInstance hazelcastInstance, ObjectMapper objectMapper) {
        this.hazelcastInstance = hazelcastInstance;
        this.objectMapper = objectMapper;
    }
    
    @SuppressWarnings("unchecked")
    public IdempotencyResult checkAndSetProcessing(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return IdempotencyResult.invalid("Idempotency key is required");
        }
        
        IMap<String, String> idempotencyMap = hazelcastInstance.getMap(IDEMPOTENCY_MAP);
        
        try {
            // Try to set the key with "PROCESSING" status atomically
            String existingValue = idempotencyMap.putIfAbsent(
                idempotencyKey, 
                STATUS_PROCESSING, 
                PROCESSING_TTL_MINUTES, 
                TimeUnit.MINUTES
            );
            
            if (existingValue == null) {
                // Key was set successfully - this is a new request
                return IdempotencyResult.processing();
            }
            
            // Key already exists - check if it's processing or completed
            if (STATUS_PROCESSING.equals(existingValue)) {
                // Request is still being processed
                return IdempotencyResult.duplicate("Transaction is already being processed");
            }
            
            // Try to parse as completed response
            try {
                Map<String, Object> response = objectMapper.readValue(existingValue, Map.class);
                return IdempotencyResult.completed(response);
            } catch (JsonProcessingException e) {
                // If we can't parse it as a completed response, treat as processing
                return IdempotencyResult.duplicate("Transaction is being processed");
            }
            
        } catch (Exception e) {
            return IdempotencyResult.error("Error checking idempotency key: " + e.getMessage());
        }
    }
    
    public void markAsCompleted(String idempotencyKey, Map<String, Object> response) {
        if (idempotencyKey == null) return;
        
        IMap<String, String> idempotencyMap = hazelcastInstance.getMap(IDEMPOTENCY_MAP);
        
        try {
            // Add idempotency key to response for reference
            response.put("idempotencyKey", idempotencyKey);
            response.put("processedAt", System.currentTimeMillis());
            
            String responseJson = objectMapper.writeValueAsString(response);
            idempotencyMap.put(
                idempotencyKey, 
                responseJson, 
                COMPLETED_TTL_MINUTES, 
                TimeUnit.MINUTES
            );
            
        } catch (Exception e) {
            // Log error but don't throw - we don't want to fail the transaction
            System.err.println("Error marking idempotency key as completed: " + e.getMessage());
        }
    }
    
    public void clearKey(String idempotencyKey) {
        if (idempotencyKey != null) {
            IMap<String, String> idempotencyMap = hazelcastInstance.getMap(IDEMPOTENCY_MAP);
            idempotencyMap.remove(idempotencyKey);
        }
    }
    
    public static class IdempotencyResult {
        public enum Status { PROCESSING, DUPLICATE, COMPLETED, INVALID, ERROR }
        
        private final Status status;
        private final String message;
        private final Map<String, Object> response;
        
        private IdempotencyResult(Status status, String message, Map<String, Object> response) {
            this.status = status;
            this.message = message;
            this.response = response;
        }
        
        public static IdempotencyResult processing() {
            return new IdempotencyResult(Status.PROCESSING, "Processing new request", null);
        }
        
        public static IdempotencyResult duplicate(String message) {
            return new IdempotencyResult(Status.DUPLICATE, message, null);
        }
        
        public static IdempotencyResult completed(Map<String, Object> response) {
            return new IdempotencyResult(Status.COMPLETED, "Request already completed", response);
        }
        
        public static IdempotencyResult invalid(String message) {
            return new IdempotencyResult(Status.INVALID, message, null);
        }
        
        public static IdempotencyResult error(String message) {
            return new IdempotencyResult(Status.ERROR, message, null);
        }
        
        public Status getStatus() { return status; }
        public String getMessage() { return message; }
        public Map<String, Object> getResponse() { return response; }
        public boolean isProcessing() { return status == Status.PROCESSING; }
        public boolean isDuplicate() { return status == Status.DUPLICATE; }
        public boolean isCompleted() { return status == Status.COMPLETED; }
        public boolean isInvalid() { return status == Status.INVALID; }
        public boolean isError() { return status == Status.ERROR; }
    }
}