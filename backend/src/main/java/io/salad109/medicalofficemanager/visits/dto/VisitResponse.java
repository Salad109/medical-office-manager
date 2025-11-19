package io.salad109.medicalofficemanager.visits.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record VisitResponse(
        Long id,
        String notes,
        LocalDateTime completedAt,
        Long appointmentId,
        LocalDate appointmentDate,
        LocalTime appointmentTime,
        Long doctorId,
        String doctorFirstName,
        String doctorLastName,
        Long patientId,
        String patientFirstName,
        String patientLastName
) {
}
