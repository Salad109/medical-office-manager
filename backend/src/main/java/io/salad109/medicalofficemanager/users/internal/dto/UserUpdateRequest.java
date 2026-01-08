package io.salad109.medicalofficemanager.users.internal.dto;

import io.salad109.medicalofficemanager.users.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username must not exceed 50 characters")
        String username,

        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        @NotBlank(message = "Phone number is required")
        @Size(max = 15, message = "Phone number must not exceed 15 characters")
        String phoneNumber,

        String pesel,

        @NotNull(message = "Role is required")
        Role role
) {
}
