package com.epay.domain.developer.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAppRequest {

    @NotBlank(message = "App name is required")
    @Size(max = 100)
    private String appName;

    @Size(max = 500)
    private String description;

    @Pattern(regexp = "https?://.+", message = "Website URL must start with http:// or https://")
    private String websiteUrl;
}
