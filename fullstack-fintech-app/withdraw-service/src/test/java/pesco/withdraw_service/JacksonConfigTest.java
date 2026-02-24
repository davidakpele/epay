package pesco.withdraw_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import pesco.example.withdraw_service.dtos.HistoryDTO;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class JacksonConfigTest {
    
    private ObjectMapper objectMapper = createObjectMapper();
    
    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }
    
    @Test
    void testOffsetDateTimeParsingWithNanoseconds() throws JsonProcessingException {
        String json = "{\n" +
                "    \"id\": \"9f39a8b1-b984-4855-b200-a60e2396bdf5\",\n" +
                "    \"walletId\": 1,\n" +
                "    \"userId\": 1001,\n" +
                "    \"sessionId\": \"ZIpqRdOdqbQsVPoaAjTfGLKIRa5B8hGQ\",\n" +
                "    \"amount\": 200,\n" +
                "    \"type\": \"CREDITED\",\n" +
                "    \"description\": \"KIMMY PETER/Transfer from DAVID AKPELE\",\n" +
                "    \"message\": \"Transfer €200.00 to david\",\n" +
                "    \"currencyType\": \"EUR\",\n" +
                "    \"status\": \"SUCCESS\",\n" +
                "    \"ipAddress\": \"172.18.0.1\",\n" +
                "    \"timestamp\": \"2025-10-29T07:39:53.987403224Z\"\n" +
                "}";
        
        HistoryDTO dto = objectMapper.readValue(json, HistoryDTO.class);
        
        assertNotNull(dto);
        assertNotNull(dto.getTimestamp());
        assertEquals("9f39a8b1-b984-4855-b200-a60e2396bdf5", dto.getId());
        assertEquals(1001L, dto.getUserId());
        
        System.out.println("Successfully parsed timestamp: " + dto.getTimestamp());
        System.out.println("TEST PASSED! The timestamp with nanoseconds was parsed correctly.");
    }
    
    @Test
    void testOffsetDateTimeSerialization() throws JsonProcessingException {
        HistoryDTO dto = new HistoryDTO();
        dto.setId("test-id");
        dto.setUserId(1001L);
        dto.setWalletId(1L);
        dto.setAmount(new BigDecimal("200.00"));
        dto.setTimestamp(java.time.OffsetDateTime.now());
        
        String json = objectMapper.writeValueAsString(dto);
        assertNotNull(json);
        assertTrue(json.contains("timestamp"));
        
        System.out.println("Serialized JSON: " + json);
        System.out.println("TEST PASSED! Object was serialized to JSON correctly.");
    }
    
    @Test
    void testSpecificProblematicTimestamp() throws JsonProcessingException {
        // Test the exact timestamp that was causing the issue
        String problematicJson = "{\n" +
                "    \"id\": \"test\",\n" +
                "    \"timestamp\": \"2025-10-29T07:39:53.987403224Z\"\n" +
                "}";
        
        HistoryDTO dto = objectMapper.readValue(problematicJson, HistoryDTO.class);
        
        assertNotNull(dto);
        assertNotNull(dto.getTimestamp());
        System.out.println("PROBLEMATIC TIMESTAMP PARSED SUCCESSFULLY: " + dto.getTimestamp());
    }
}