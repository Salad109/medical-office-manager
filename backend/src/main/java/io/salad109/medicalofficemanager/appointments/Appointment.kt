package io.salad109.medicalofficemanager.appointments

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
    var patientId: Long,

    @field:NotNull
    @Column(name = "appointment_date")
    var appointmentDate: LocalDate,

    @field:NotNull
    @Column(name = "appointment_time")
    var appointmentTime: LocalTime,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    var status: AppointmentStatus
)