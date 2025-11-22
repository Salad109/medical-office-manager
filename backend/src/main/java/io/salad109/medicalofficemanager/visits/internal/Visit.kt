package io.salad109.medicalofficemanager.visits.internal

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
    var appointmentId: Long,

    var notes: String? = null,

    @field:NotNull
    @Column(name = "completed_by_doctor_id")
    var completedByDoctorId: Long,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null
)