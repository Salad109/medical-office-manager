package io.salad109.medicalofficemanager.appointments.dto;

import io.salad109.medicalofficemanager.appointments.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentWithDetailsResponse(
        Long id,
        Long patientId,
        String patientFirstName,
        String patientLastName,
        String patientPhoneNumber,
        LocalDate date,
        LocalTime time,
        AppointmentStatus status
) {
}
