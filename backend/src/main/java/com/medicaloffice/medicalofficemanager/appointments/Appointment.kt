package com.medicaloffice.medicalofficemanager.appointments

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "appointments")
class Appointment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:NotNull
    @Column(name = "patient_id")
    var patientId: Long? = null,

    @field:NotNull
    @Column(name = "appointment_date")
    var appointmentDate: LocalDate? = null,

    @field:NotNull
    @Column(name = "appointment_time")
    var appointmentTime: LocalTime? = null,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    var status: AppointmentStatus? = null
)