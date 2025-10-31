package com.medicaloffice.medicalofficemanager.auth.dto;

import com.medicaloffice.medicalofficemanager.users.Role;

public record AuthResponse(
        String token,
        Role role
) {
}