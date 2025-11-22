package io.salad109.medicalofficemanager.users.internal

import io.salad109.medicalofficemanager.users.internal.dto.UserCreationRequest
import io.salad109.medicalofficemanager.users.internal.dto.UserResponse
import io.salad109.medicalofficemanager.users.internal.dto.UserResponseWithVisits
import io.salad109.medicalofficemanager.users.internal.dto.UserUpdateRequest
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('DOCTOR')")
    @GetMapping
    fun getAllUsers(pageable: Pageable): ResponseEntity<Page<UserResponse>> {
        val users = userService.getAllUsers(pageable)
        return ResponseEntity.ok(users)
    }

    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('DOCTOR') or (#id == authentication.principal.userId)")
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/{id}/with-visits")
    fun getPatientWithVisits(@PathVariable id: Long): ResponseEntity<UserResponseWithVisits> {
        val patientWithVisits = userService.getPatientWithVisits(id)
        return ResponseEntity.ok(patientWithVisits)
    }

    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('DOCTOR')")
    @GetMapping("/search")
    fun searchUsersByLastName(@RequestParam q: String, pageable: Pageable): ResponseEntity<Page<UserResponse>> {
        val users = userService.searchUsers(q, pageable)
        return ResponseEntity.ok(users)
    }

    @PreAuthorize("hasRole('RECEPTIONIST')")
    @PostMapping
    fun createUser(@Valid @RequestBody request: UserCreationRequest): ResponseEntity<UserResponse> {
        val createdUser = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @PreAuthorize("hasRole('RECEPTIONIST')")
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UserUpdateRequest
    ): ResponseEntity<UserResponse> {
        val updatedUser = userService.updateUser(id, request)
        return ResponseEntity.ok(updatedUser)
    }
}