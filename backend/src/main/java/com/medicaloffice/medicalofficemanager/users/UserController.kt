package com.medicaloffice.medicalofficemanager.users

import com.medicaloffice.medicalofficemanager.users.dto.UserCreationRequest
import com.medicaloffice.medicalofficemanager.users.dto.UserResponse
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
    @PreAuthorize("hasRole('RECEPTIONIST')")
    @GetMapping
    fun getAllUsers(pageable: Pageable): ResponseEntity<Page<UserResponse>> {
        val users = userService.getAllUsers(pageable)
        return ResponseEntity.ok(users)
    }

    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('DOCTOR') or (#id == authentication.principal.id)")
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
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
        @Valid @RequestBody user: UserCreationRequest
    ): ResponseEntity<UserResponse> {
        val updatedUser = userService.updateUser(id, user)
        return ResponseEntity.ok(updatedUser)
    }
}