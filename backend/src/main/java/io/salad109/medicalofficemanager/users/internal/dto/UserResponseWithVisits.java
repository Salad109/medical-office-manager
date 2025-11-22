package io.salad109.medicalofficemanager.users.internal.dto;

import io.salad109.medicalofficemanager.visits.VisitResponse;

import java.util.List;

public record UserResponseWithVisits(
        UserResponse patient,
        List<VisitResponse> visits
) {
}
