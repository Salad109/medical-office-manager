package com.medicaloffice.medicalofficemanager.visits

import com.medicaloffice.medicalofficemanager.auth.CustomUserDetails
import com.medicaloffice.medicalofficemanager.visits.dto.VisitCreationRequest
import com.medicaloffice.medicalofficemanager.visits.dto.VisitResponse
import com.medicaloffice.medicalofficemanager.visits.dto.VisitUpdateRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/visits")
class VisitController(
    private val visitService: VisitService
) {

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/patient/{id}")
    fun getVisitsByPatient(@PathVariable id: Long): ResponseEntity<List<VisitResponse>> {
        val visits = visitService.getVisitsByPatient(id)
        return ResponseEntity.ok(visits)
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping
    fun markVisitAsCompleted(
        @Valid @RequestBody request: VisitCreationRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<VisitResponse> {
        val visit = visitService.markVisitAsCompleted(request, userDetails.userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(visit)
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/{id}")
    fun updateVisit(
        @PathVariable id: Long,
        @Valid @RequestBody request: VisitUpdateRequest
    ): ResponseEntity<VisitResponse> {
        val updatedVisit = visitService.updateVisit(id, request)
        return ResponseEntity.ok(updatedVisit)
    }
}