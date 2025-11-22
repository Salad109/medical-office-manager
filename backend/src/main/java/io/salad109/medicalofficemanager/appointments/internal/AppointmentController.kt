package io.salad109.medicalofficemanager.appointments.internal

import io.salad109.medicalofficemanager.appointments.internal.dto.AppointmentResponse
import io.salad109.medicalofficemanager.appointments.internal.dto.AppointmentWithDetailsResponse
import io.salad109.medicalofficemanager.appointments.internal.dto.BookAppointmentRequest
import io.salad109.medicalofficemanager.users.Role
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/appointments")
class AppointmentController(
    private val appointmentService: AppointmentService
) {

    @GetMapping("/available")
    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('PATIENT')")
    fun getAvailableSlots(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<List<String>> {
        val availableSlots = appointmentService.getAvailableSlots(date)
        return ResponseEntity.ok(availableSlots)
    }

    @GetMapping("/existing")
    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('DOCTOR')")
    fun getAppointmentsByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<List<AppointmentWithDetailsResponse>> {
        val appointments = appointmentService.getAppointmentsWithDetailsByDate(date)
        return ResponseEntity.ok(appointments)
    }

    @GetMapping("/patient/{id}")
    @PreAuthorize("hasRole('RECEPTIONIST') or (#id == authentication.principal.userId)")
    fun getAppointmentsByPatient(@PathVariable id: Long): ResponseEntity<List<AppointmentWithDetailsResponse>> {
        val appointments = appointmentService.getAppointmentsByPatientId(id)
        return ResponseEntity.ok(appointments)
    }

    @PostMapping
    @PreAuthorize("hasRole('RECEPTIONIST') or (hasRole('PATIENT') and #request.patientId == authentication.principal.userId)")
    fun bookAppointment(@Valid @RequestBody request: BookAppointmentRequest): ResponseEntity<AppointmentResponse> {
        val response = appointmentService.bookAppointment(request = request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/{id}/mark-no-show")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    fun markAppointmentAsNoShow(@PathVariable id: Long): ResponseEntity<AppointmentResponse> {
        val appointment = appointmentService.markAsNoShow(id)
        return ResponseEntity.ok(appointment)
    }

    @DeleteMapping("/{id}")
    fun cancelAppointment(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Unit> {
        val principal = authentication.principal
        val userId = principal.javaClass.getMethod("getUserId").invoke(principal) as Long
        val role = principal.javaClass.getMethod("getRole").invoke(principal) as Role

        appointmentService.cancelAppointment(
            appointmentId = id,
            currentUserId = userId,
            currentUserRole = role
        )
        return ResponseEntity.noContent().build()
    }
}
