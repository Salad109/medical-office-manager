package io.salad109.medicalofficemanager.visits

import io.salad109.medicalofficemanager.exception.exceptions.ResourceAlreadyExistsException
import io.salad109.medicalofficemanager.exception.exceptions.ResourceNotFoundException
import io.salad109.medicalofficemanager.visits.dto.VisitCreationRequest
import io.salad109.medicalofficemanager.visits.dto.VisitResponse
import io.salad109.medicalofficemanager.visits.dto.VisitUpdateRequest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VisitService(
    private val visitRepository: VisitRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) : VisitManagement {

    override fun findVisitResponsesByPatient(patientId: Long): List<VisitResponse> {
        return visitRepository.findVisitResponsesByPatientId(patientId)
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