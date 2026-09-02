package com.epay.domain.auth.input;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceUpdateRequest {
    @Pattern(regexp = "^[a-zA-Z]{2,3}(-[a-zA-Z]{2,4})?$", message = "Invalid language code")
    private String language;

    @Size(max = 50, message = "Invalid timezone")
    private String timezone;

    private Integer sessionTimeoutMinutes;
}
