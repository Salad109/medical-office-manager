package com.medicaloffice.medicalofficemanager.visits

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Entity
@Table(name = "visits")
class Visit(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:NotNull
    @Column(name = "appointment_id")
    var appointmentId: Long? = null,

    var notes: String? = null,

    @field:NotNull
    @Column(name = "completed_by_doctor_id")
    var completedByDoctorId: Long? = null,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null
)