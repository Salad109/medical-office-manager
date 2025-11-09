package com.medicaloffice.medicalofficemanager.visits

import com.medicaloffice.medicalofficemanager.appointments.AppointmentRepository
import com.medicaloffice.medicalofficemanager.appointments.AppointmentStatus
import com.medicaloffice.medicalofficemanager.visits.dto.VisitCreationRequest
import com.medicaloffice.medicalofficemanager.visits.dto.VisitResponse
import com.medicaloffice.medicalofficemanager.visits.dto.VisitUpdateRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VisitService(
    private val visitRepository: VisitRepository,
    private val appointmentRepository: AppointmentRepository,
) {

    fun getVisitsByPatient(patientId: Long): List<VisitResponse> {
        return visitRepository.findVisitResponseByPatientId(patientId)
    }

    @Transactional
    fun markVisitAsCompleted(request: VisitCreationRequest, doctorId: Long): VisitResponse {
        val appointment = appointmentRepository.findById(request.appointmentId)
            .orElseThrow { IllegalArgumentException("Appointment not found with id: ${request.appointmentId}") }

        require(appointment.status == AppointmentStatus.SCHEDULED || appointment.status == AppointmentStatus.NO_SHOW) {
            "Only scheduled or no-show appointments can be marked as completed."
        }

        require(!visitRepository.existsByAppointmentId(request.appointmentId)) {
            "Visit already exists for appointment ${request.appointmentId}"
        }

        val visit = Visit(
            appointmentId = request.appointmentId,
            notes = request.notes,
            completedByDoctorId = doctorId,
        )
        appointment.status = AppointmentStatus.COMPLETED
        appointmentRepository.save(appointment)
        val savedVisit = visitRepository.save(visit)

        return visitRepository.findVisitResponseById(savedVisit.id!!)
            .orElseThrow { IllegalArgumentException("Visit not found with id: ${savedVisit.id}") }
    }

    @Transactional
    fun updateVisitNotes(visitId: Long, request: VisitUpdateRequest): VisitResponse {
        val visit = visitRepository.findById(visitId)
            .orElseThrow { IllegalArgumentException("Visit not found with id: $visitId") }

        visit.notes = request.notes
        val updatedVisit = visitRepository.save(visit)

        return visitRepository.findVisitResponseById(updatedVisit.id!!)
            .orElseThrow { IllegalArgumentException("Visit not found with id: ${updatedVisit.id}") }
    }
}