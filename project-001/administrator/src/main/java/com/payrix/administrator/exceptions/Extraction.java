package com.payrix.administrator.exceptions;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class Extraction {

    public String extractDetailsFromError(String errorMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(errorMessage);
            return rootNode.path("message").asText("No message provided");
        } catch (JsonProcessingException e) {
            return errorMessage != null && !errorMessage.isEmpty()
                    ? errorMessage
                    : "No details available";
        }
    }
}
