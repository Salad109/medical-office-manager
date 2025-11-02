package com.medicaloffice.medicalofficemanager.visits

import com.medicaloffice.medicalofficemanager.auth.CustomUserDetails
import com.medicaloffice.medicalofficemanager.visits.dto.VisitCreationRequest
import com.medicaloffice.medicalofficemanager.visits.dto.VisitResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
class VisitController(
    private val visitService: VisitService
) {

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/api/visits/patient/{id}")
    fun getVisitsByPatient(@PathVariable id: Long): ResponseEntity<List<VisitResponse>> {
        val visits = visitService.getVisitsByPatient(id)
        return ResponseEntity.ok(visits)
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/api/visits")
    fun markVisitAsCompleted(
        @Valid @RequestBody request: VisitCreationRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<VisitResponse> {
        val visit = visitService.markVisitAsCompleted(request, userDetails.userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(visit)
    }
}