package com.medicaloffice.medicalofficemanager.appointments

import com.medicaloffice.medicalofficemanager.appointments.dto.AppointmentResponse
import com.medicaloffice.medicalofficemanager.appointments.dto.AppointmentWithDetailsResponse
import com.medicaloffice.medicalofficemanager.appointments.dto.BookAppointmentRequest
import com.medicaloffice.medicalofficemanager.exception.exceptions.InvalidRoleException
import com.medicaloffice.medicalofficemanager.exception.exceptions.InvalidTimeSlotException
import com.medicaloffice.medicalofficemanager.exception.exceptions.ResourceAlreadyExistsException
import com.medicaloffice.medicalofficemanager.exception.exceptions.ResourceNotFoundException
import com.medicaloffice.medicalofficemanager.users.Role
import com.medicaloffice.medicalofficemanager.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

@Service
class AppointmentService(
    private val appointmentRepository: AppointmentRepository,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(AppointmentService::class.java)

    companion object {
        private val OFFICE_START = LocalTime.of(9, 0)
        private val OFFICE_END = LocalTime.of(17, 0)
        private const val SLOT_DURATION_MINUTES = 30L
    }

    fun getAvailableSlots(date: LocalDate): List<String> {
        // Get existing appointments
        val existingAppointments: List<Appointment> = appointmentRepository.findByAppointmentDate(date)
        val bookedTimes = existingAppointments.map { it.appointmentTime }.toSet()

        // Generate all possible slots
        val allSlots = mutableListOf<LocalTime>()
        var currentTime = OFFICE_START
        while (currentTime.isBefore(OFFICE_END)) {
            allSlots.add(currentTime)
            currentTime = currentTime.plusMinutes(SLOT_DURATION_MINUTES)
        }

        // Subtract booked times from all slots
        return allSlots
            .filterNot { it in bookedTimes }
            .map { it.toString() }
            .also { log.debug("Available slots on {}: {}", date, it) }
    }

    fun getAppointmentsByDate(date: LocalDate): List<AppointmentResponse> {
        return appointmentRepository.findByAppointmentDate(date).map {
            AppointmentResponse(
                it.id,
                it.patientId,
                it.appointmentDate,
                it.appointmentTime,
                it.status
            )
        }.also { log.debug("Fetched {} appointments for date {}", it.size, date) }
    }

    fun getAppointmentsWithDetailsByDate(date: LocalDate): List<AppointmentWithDetailsResponse> {
        return appointmentRepository.findAppointmentsWithDetailsByDate(date)
            .also { log.debug("Fetched {} appointments with details for date {}", it.size, date) }
    }

    fun getAppointmentsByPatientId(patientId: Long): List<AppointmentWithDetailsResponse> {
        return appointmentRepository.findAppointmentsByPatientId(patientId)
            .also { log.debug("Fetched {} appointments for patient {}", it.size, patientId) }
    }

    @Transactional
    fun bookAppointment(request: BookAppointmentRequest): AppointmentResponse {
        userRepository.findById(request.patientId)
            .orElseThrow { ResourceNotFoundException("Patient with ID ${request.patientId} not found") }
            .takeIf { it.role == Role.PATIENT }
            ?: throw InvalidRoleException("User with ID ${request.patientId} is not a patient")


        if (request.date.isBefore(LocalDate.now()) && request.time.isBefore(LocalTime.now())) {
            throw InvalidTimeSlotException("Cannot book appointment in the past")
        }

        if (!request.isValidTimeSlot()) {
            throw InvalidTimeSlotException(
                "Invalid time slot. Must be ${SLOT_DURATION_MINUTES}-minute interval between $OFFICE_START and $OFFICE_END"
            )
        }

        // Check slot availability
        val existingAppointments: List<Appointment> = appointmentRepository.findByAppointmentDate(request.date)
        val bookedTimes = existingAppointments.map { it.appointmentTime }.toSet()
        if (request.time in bookedTimes) {
            throw ResourceAlreadyExistsException(
                "Time slot ${request.time} on ${request.date} is already booked"
            )
        }

        // Create appointment
        val appointment = Appointment(
            patientId = request.patientId,
            appointmentDate = request.date,
            appointmentTime = request.time,
            status = AppointmentStatus.SCHEDULED
        )

        val savedAppointment = appointmentRepository.save(appointment)
        log.info("Appointment booked: ID=${savedAppointment.id}, Patient=${request.patientId}, Date=${request.date}, Time=${request.time}")

        return AppointmentResponse(
            savedAppointment.id,
            savedAppointment.patientId,
            savedAppointment.appointmentDate,
            savedAppointment.appointmentTime,
            savedAppointment.status
        )
    }

    @Transactional
    fun markAsNoShow(appointmentId: Long): AppointmentResponse {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { ResourceNotFoundException("Appointment not found with ID: $appointmentId") }

        check(appointment.status != AppointmentStatus.COMPLETED) {
            "Cannot mark completed appointment as no-show"
        }

        appointment.status = AppointmentStatus.NO_SHOW
        val updatedAppointment = appointmentRepository.save(appointment)
        log.info("Appointment marked as NO_SHOW: ID=$appointmentId")

        return AppointmentResponse(
            updatedAppointment.id,
            updatedAppointment.patientId,
            updatedAppointment.appointmentDate,
            updatedAppointment.appointmentTime,
            updatedAppointment.status
        )
    }

    @Transactional
    fun cancelAppointment(appointmentId: Long, currentUserId: Long, currentUserRole: Role) {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { ResourceNotFoundException("Appointment not found with ID: $appointmentId") }

        // Check permissions
        when (currentUserRole) {
            Role.RECEPTIONIST -> {
                // Receptionists can cancel any appointment
            }

            Role.DOCTOR -> throw AccessDeniedException("Doctors cannot cancel appointments")

            Role.PATIENT -> {
                if (appointment.patientId != currentUserId) {
                    throw AccessDeniedException("Patients can only cancel their own appointments")
                }
            }
        }

        appointmentRepository.delete(appointment)
        log.info("Appointment cancelled: ID=$appointmentId by user $currentUserId")
    }

    private fun BookAppointmentRequest.isValidTimeSlot(): Boolean {
        val time = this.time
        if (time.isBefore(OFFICE_START) || !time.isBefore(OFFICE_END)) {
            return false
        }

        val minutesSinceStart = Duration.between(OFFICE_START, time).toMinutes()
        return minutesSinceStart % SLOT_DURATION_MINUTES == 0L
    }
}
