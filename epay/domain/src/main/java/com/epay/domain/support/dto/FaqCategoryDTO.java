package com.epay.domain.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Groups FAQs by category — mirrors the structure the frontend
 * already expects (category name + list of question/answer items).
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FaqCategoryDTO {
    private String category;
    private List<FaqDTO> items;
}
