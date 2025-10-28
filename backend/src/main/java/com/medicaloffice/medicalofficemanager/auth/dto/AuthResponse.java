package com.medicaloffice.medicalofficemanager.auth.dto;

public record AuthResponse(
        String token,
        String role
) {
}