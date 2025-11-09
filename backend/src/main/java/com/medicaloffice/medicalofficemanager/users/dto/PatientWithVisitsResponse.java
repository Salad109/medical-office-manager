package com.medicaloffice.medicalofficemanager.users.dto;

import com.medicaloffice.medicalofficemanager.visits.dto.VisitResponse;

import java.util.List;

public record PatientWithVisitsResponse(
        UserResponse patient,
        List<VisitResponse> visits
) {
}
