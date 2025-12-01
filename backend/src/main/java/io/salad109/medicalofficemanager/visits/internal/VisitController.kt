package io.salad109.medicalofficemanager.visits.internal

import io.salad109.medicalofficemanager.users.CustomUserDetails
import io.salad109.medicalofficemanager.visits.VisitResponse
import io.salad109.medicalofficemanager.visits.internal.dto.VisitCreationRequest
import io.salad109.medicalofficemanager.visits.internal.dto.VisitUpdateRequest
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
        val visits = visitService.findVisitResponsesByPatient(id)
        return ResponseEntity.ok(visits)
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping
    fun markVisitAsCompleted(
        @Valid @RequestBody request: VisitCreationRequest,
        @AuthenticationPrincipal principal: CustomUserDetails
    ): ResponseEntity<VisitResponse> {
        val visit = visitService.markVisitAsCompleted(request, principal.userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(visit)
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/{id}")
    fun updateVisit(
        @PathVariable id: Long,
        @Valid @RequestBody request: VisitUpdateRequest
    ): ResponseEntity<VisitResponse> {
        val updatedVisit = visitService.updateVisitNotes(id, request)
        return ResponseEntity.ok(updatedVisit)
    }
}