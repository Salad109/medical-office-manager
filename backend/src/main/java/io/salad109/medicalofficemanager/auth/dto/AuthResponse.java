package io.salad109.medicalofficemanager.auth.dto;

import io.salad109.medicalofficemanager.users.Role;

public record AuthResponse(
        String token,
        Role role
) {
}