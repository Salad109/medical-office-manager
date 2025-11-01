package com.medicaloffice.medicalofficemanager.users.dto;

import com.medicaloffice.medicalofficemanager.users.Role;

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
