package io.salad109.medicalofficemanager.visits

import java.time.LocalDateTime

data class VisitCompletedEvent(
    val appointmentId: Long,
    val visitId: Long,
    val completedAt: LocalDateTime
)