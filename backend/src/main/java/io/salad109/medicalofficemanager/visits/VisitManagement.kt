package io.salad109.medicalofficemanager.visits

import io.salad109.medicalofficemanager.visits.dto.VisitResponse

interface VisitManagement {
    fun findVisitResponsesByPatient(patientId: Long): List<VisitResponse>
}