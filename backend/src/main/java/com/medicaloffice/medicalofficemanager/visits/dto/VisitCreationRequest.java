package com.medicaloffice.medicalofficemanager.visits.dto;

import jakarta.validation.constraints.NotNull;

public record VisitCreationRequest(
        @NotNull Long appointmentId,
        String notes
) {
}
