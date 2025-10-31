package com.medicaloffice.medicalofficemanager.appointments.dto;

import com.medicaloffice.medicalofficemanager.appointments.AppointmentStatus;

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
