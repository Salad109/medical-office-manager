package com.medicaloffice.medicalofficemanager.appointments

import com.medicaloffice.medicalofficemanager.appointments.dto.AppointmentResponse
import com.medicaloffice.medicalofficemanager.appointments.dto.BookAppointmentRequest
import com.medicaloffice.medicalofficemanager.users.Role
import com.medicaloffice.medicalofficemanager.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import kotlin.jvm.optionals.getOrNull

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

    @Transactional
    fun bookAppointment(request: BookAppointmentRequest): AppointmentResponse {
        require((userRepository.findById(request.patientId).getOrNull()?.role == Role.PATIENT)) {
            "User with ID ${request.patientId} does not exist or is not a patient"
        }

        require(!request.date.isBefore(LocalDate.now())) {
            "Cannot book appointment in the past"
        }

        require(request.isValidTimeSlot()) {
            "Invalid time slot. Must be ${SLOT_DURATION_MINUTES}-minute interval between $OFFICE_START and $OFFICE_END"
        }

        // Check slot availability
        val existingAppointments: List<Appointment> = appointmentRepository.findByAppointmentDate(request.date)
        val bookedTimes = existingAppointments.map { it.appointmentTime }.toSet()
        require(request.time !in bookedTimes) {
            "Time slot ${request.time} on ${request.date} is already booked"
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
    fun cancelAppointment(appointmentId: Long, currentUserId: Long, currentUserRole: Role) {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { IllegalArgumentException("Appointment not found with ID: $appointmentId") }

        // Check permissions
        when (currentUserRole) {
            Role.PATIENT -> {
                require(appointment.patientId == currentUserId) {
                    "Patients can only cancel their own appointments"
                }
            }

            Role.RECEPTIONIST -> {
                // Receptionists can cancel any appointment
            }

            Role.DOCTOR -> throw AccessDeniedException("Doctors cannot cancel appointments")
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
