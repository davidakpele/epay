package com.epay.domain.history.converter;

import com.epay.domain.history.dto.StatusTimeline;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts StatusTimeline ↔ JSON String for JPA persistence.
 * autoApply=true — applied to all StatusTimeline fields automatically.
 */
@Converter(autoApply = true)
public class StatusTimelineConverter implements AttributeConverter<StatusTimeline, String> {

    private static final Logger log = LoggerFactory.getLogger(StatusTimelineConverter.class);

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Override
    public String convertToDatabaseColumn(StatusTimeline timeline) {
        if (timeline == null) return "{}";
        try {
            String json = MAPPER.writeValueAsString(timeline);
            log.debug("[StatusTimelineConverter] Serialised {} chars", json.length());
            return json;
        } catch (Exception e) {
            log.error("[StatusTimelineConverter] Serialisation FAILED: {}", e.getMessage(), e);
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
            log.error("[StatusTimelineConverter] Deserialisation FAILED for json='{}': {}",
                    json.length() > 200 ? json.substring(0, 200) + "…" : json,
                    e.getMessage(), e);
            return new StatusTimeline();
        }
    }
}
