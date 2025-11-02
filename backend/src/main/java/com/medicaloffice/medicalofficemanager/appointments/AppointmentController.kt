package com.medicaloffice.medicalofficemanager.appointments

import com.medicaloffice.medicalofficemanager.appointments.dto.AppointmentResponse
import com.medicaloffice.medicalofficemanager.appointments.dto.BookAppointmentRequest
import com.medicaloffice.medicalofficemanager.auth.CustomUserDetails
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/appointments")
class AppointmentController(
    private val appointmentService: AppointmentService
) {

    @GetMapping("/available")
    fun getAvailableSlots(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<List<String>> {
        val availableSlots = appointmentService.getAvailableSlots(date)
        return ResponseEntity.ok(availableSlots)
    }

    @PreAuthorize("hasRole('RECEPTIONIST') or (hasRole('PATIENT') and #request.patientId == authentication.principal.userId)")
    @PostMapping
    fun bookAppointment(@Valid @RequestBody request: BookAppointmentRequest): ResponseEntity<AppointmentResponse> {
        val response = appointmentService.bookAppointment(request = request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/{id}")
    fun cancelAppointment(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Unit> {
        appointmentService.cancelAppointment(
            appointmentId = id,
            currentUserId = userDetails.userId,
            currentUserRole = userDetails.role
        )
        return ResponseEntity.noContent().build()
    }
}
