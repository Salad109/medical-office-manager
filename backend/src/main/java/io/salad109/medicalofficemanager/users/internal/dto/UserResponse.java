package io.salad109.medicalofficemanager.users.internal.dto;

import io.salad109.medicalofficemanager.users.Role;

public record UserResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String phoneNumber,
        String pesel,
        Role role
) {
}
