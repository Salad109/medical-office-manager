package io.salad109.medicalofficemanager.users.dto;

import io.salad109.medicalofficemanager.visits.dto.VisitResponse;

import java.util.List;

public record UserResponseWithVisits(
        UserResponse patient,
        List<VisitResponse> visits
) {
}
