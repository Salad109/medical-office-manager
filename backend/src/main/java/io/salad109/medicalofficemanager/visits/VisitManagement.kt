package io.salad109.medicalofficemanager.visits

interface VisitManagement {
    fun findVisitResponsesByPatient(patientId: Long): List<VisitResponse>
}