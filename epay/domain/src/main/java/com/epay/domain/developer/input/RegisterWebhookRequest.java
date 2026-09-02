package com.epay.domain.developer.input;

import com.epay.domain.developer.enums.ApiMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterWebhookRequest {

    @NotNull(message = "Mode is required")
    private ApiMode mode;

    @NotBlank(message = "URL is required")
    @Pattern(regexp = "https://.+", message = "Webhook URL must use HTTPS")
    @Size(max = 500)
    private String url;

    private String subscribedEvents;
}
