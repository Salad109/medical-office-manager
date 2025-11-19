package io.salad109.medicalofficemanager.appointments.dto;

import io.salad109.medicalofficemanager.appointments.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        LocalDate date,
        LocalTime time,
        AppointmentStatus status
) {
}
