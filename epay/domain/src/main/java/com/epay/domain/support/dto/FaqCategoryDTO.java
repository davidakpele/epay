package com.epay.domain.support.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FaqCategoryDTO {
    private String category;
    private List<FaqDTO> items;
}
