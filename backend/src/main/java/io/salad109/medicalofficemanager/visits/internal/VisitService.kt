package io.salad109.medicalofficemanager.visits.internal

import io.salad109.medicalofficemanager.exception.ResourceAlreadyExistsException
import io.salad109.medicalofficemanager.exception.ResourceNotFoundException
import io.salad109.medicalofficemanager.visits.VisitCompletedEvent
import io.salad109.medicalofficemanager.visits.VisitResponse
import io.salad109.medicalofficemanager.visits.internal.dto.VisitCreationRequest
import io.salad109.medicalofficemanager.visits.internal.dto.VisitUpdateRequest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VisitService(
    private val visitRepository: VisitRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val pdfGenerator: VisitPdfGenerator
) {

    fun findVisitResponsesByPatient(patientId: Long): List<VisitResponse> {
        return visitRepository.findVisitResponsesByPatientId(patientId)
    }

    fun generatePatientVisitReport(patientId: Long): ByteArray {
        val visits = visitRepository.findVisitResponsesByPatientId(patientId)
        if (visits.isEmpty()) {
            throw ResourceNotFoundException("No visits found for patient with ID: $patientId")
        }
        return pdfGenerator.generatePatientVisitReport(visits)
    }

    @Transactional
    fun markVisitAsCompleted(request: VisitCreationRequest, doctorId: Long): VisitResponse {
        if (visitRepository.existsByAppointmentId(request.appointmentId)) {
            throw ResourceAlreadyExistsException("Visit already exists for appointment ${request.appointmentId}")
        }

        val visit = Visit(
            appointmentId = request.appointmentId,
            notes = request.notes,
            completedByDoctorId = doctorId,
        )
        val savedVisit = visitRepository.save(visit)
        applicationEventPublisher.publishEvent(
            VisitCompletedEvent(
                appointmentId = request.appointmentId,
                visitId = savedVisit.id!!,
                completedAt = savedVisit.completedAt!!
            )
        )

        return visitRepository.findVisitResponseById(savedVisit.id!!)
            .orElseThrow { ResourceNotFoundException("Visit not found with ID: ${savedVisit.id}") }
    }

    @Transactional
    fun updateVisitNotes(visitId: Long, request: VisitUpdateRequest): VisitResponse {
        val visit = visitRepository.findById(visitId)
            .orElseThrow { ResourceNotFoundException("Visit not found with ID: $visitId") }

        visit.notes = request.notes
        val updatedVisit = visitRepository.save(visit)

        return visitRepository.findVisitResponseById(updatedVisit.id!!)
            .orElseThrow { ResourceNotFoundException("Visit not found with ID: ${updatedVisit.id}") }
    }
}