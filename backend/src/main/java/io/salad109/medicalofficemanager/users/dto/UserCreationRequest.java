package io.salad109.medicalofficemanager.users.dto;

import io.salad109.medicalofficemanager.users.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreationRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username must not exceed 50 characters")
        String username,

        @NotBlank(message = "Password is required")
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

        @Size(min = 11, max = 11, message = "PESEL must be exactly 11 characters")
        String pesel,

        @NotNull(message = "Role is required")
        Role role
) {
}