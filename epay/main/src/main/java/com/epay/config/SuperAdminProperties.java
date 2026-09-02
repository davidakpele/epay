package com.epay.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Binds super-admin bootstrap credentials from:
 *
 *   epay.super-admin.*  in application.yaml
 *
 * All values fall back to environment variables so they can be
 * overridden in Docker / Kubernetes without touching the yaml file.
 *
 * Example yaml:
 *   epay:
 *     super-admin:
 *       username:   ${SUPER_ADMIN_USERNAME:superadmin}
 *       email:      ${SUPER_ADMIN_EMAIL:superadmin@epay.com}
 *       password:   ${SUPER_ADMIN_PASSWORD:ChangeMe@2026!}
 *       first-name: ${SUPER_ADMIN_FIRST_NAME:Super}
 *       last-name:  ${SUPER_ADMIN_LAST_NAME:Admin}
 *       phone:      ${SUPER_ADMIN_PHONE:}
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "epay.super-admin")
public class SuperAdminProperties {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Super-admin password must be at least 8 characters")
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    /** Optional — can be left blank */
    private String phone = "";
}
