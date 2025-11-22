package io.salad109.medicalofficemanager.visits.internal.dto;

import jakarta.validation.constraints.NotNull;

public record VisitCreationRequest(
        @NotNull Long appointmentId,
        String notes
) {
}
