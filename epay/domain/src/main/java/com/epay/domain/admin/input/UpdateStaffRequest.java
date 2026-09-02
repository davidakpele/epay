package com.epay.domain.admin.input;

import com.epay.domain.auth.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateStaffRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Email(message = "Invalid email address")
    private String email;

    private String phone;

    private Role role;

    private Boolean enabled;
    private String department;
    private String jobTitle;
}
