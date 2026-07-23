package com.epay.domain.history.converter;

import com.epay.domain.history.dto.StatusTimeline;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts StatusTimeline ↔ JSON String for JPA persistence.
 * Lives in domain so @EntityScan picks it up alongside entities.
 * autoApply=true — applied to all StatusTimeline fields automatically.
 */
@Converter(autoApply = true)
public class StatusTimelineConverter implements AttributeConverter<StatusTimeline, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public String convertToDatabaseColumn(StatusTimeline timeline) {
        if (timeline == null) return "{}";
        try {
            return MAPPER.writeValueAsString(timeline);
        } catch (Exception e) {
            return "{}";
        }
    }

    @Override
    public StatusTimeline convertToEntityAttribute(String json) {
        if (json == null || json.isBlank() || json.equals("{}")) {
            return new StatusTimeline();
        }
        try {
            return MAPPER.readValue(json, StatusTimeline.class);
        } catch (Exception e) {
            return new StatusTimeline();
        }
    }
}
