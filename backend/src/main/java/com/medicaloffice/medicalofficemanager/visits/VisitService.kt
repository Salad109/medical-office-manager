package com.medicaloffice.medicalofficemanager.visits

import com.medicaloffice.medicalofficemanager.appointments.AppointmentRepository
import com.medicaloffice.medicalofficemanager.appointments.AppointmentStatus
import com.medicaloffice.medicalofficemanager.users.Role
import com.medicaloffice.medicalofficemanager.users.UserRepository
import com.medicaloffice.medicalofficemanager.visits.dto.VisitCreationRequest
import com.medicaloffice.medicalofficemanager.visits.dto.VisitResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class VisitService(
    private val visitRepository: VisitRepository,
    private val appointmentRepository: AppointmentRepository,
    private val userRepository: UserRepository
) {

    fun getVisitsByPatient(patientId: Long): List<VisitResponse> {
        return visitRepository.findVisitResponseByPatientId(patientId)
    }

    @Transactional
    fun markVisitAsCompleted(request: VisitCreationRequest, doctorId: Long): VisitResponse {
        val appointment = appointmentRepository.findById(request.appointmentId)
            .orElseThrow { IllegalArgumentException("Appointment not found with id: ${request.appointmentId}") }

        require(appointment.status == AppointmentStatus.SCHEDULED || appointment.status == AppointmentStatus.NO_SHOW) {
            "Only scheduled appointments can be marked as completed."
        }

        require(!visitRepository.existsByAppointmentId(request.appointmentId)) {
            "Visit already exists for appointment ${request.appointmentId}"
        }

        require(userRepository.findById(doctorId).getOrNull()?.role == Role.DOCTOR) {
            "Only doctors can mark visits as completed."
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
}