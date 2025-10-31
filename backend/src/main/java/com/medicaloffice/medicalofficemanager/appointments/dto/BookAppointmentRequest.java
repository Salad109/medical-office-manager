package com.medicaloffice.medicalofficemanager.appointments.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookAppointmentRequest(
        @NotNull(message = "Patient ID is required")
        Long patientId,

        @NotNull(message = "Appointment date is required")
        LocalDate date,

        @NotNull(message = "Appointment time is required")
        LocalTime time
) {
}
