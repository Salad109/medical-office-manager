package com.medicaloffice.medicalofficemanager.users

import com.medicaloffice.medicalofficemanager.exception.exceptions.ResourceAlreadyExistsException
import com.medicaloffice.medicalofficemanager.exception.exceptions.ResourceNotFoundException
import com.medicaloffice.medicalofficemanager.users.dto.UserCreationRequest
import com.medicaloffice.medicalofficemanager.users.dto.UserUpdateRequest
import com.medicaloffice.medicalofficemanager.users.dto.UserResponse
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder
) {
    private val log = LoggerFactory.getLogger(UserService::class.java)

    fun getAllUsers(pageable: Pageable): Page<UserResponse> {
        return userRepository.findAll(pageable).map { user ->
            user.toResponse()
        }
    }

    fun getUserById(id: Long): UserResponse {
        val user = userRepository.findById(id).orElseThrow { ResourceNotFoundException("User with id $id not found") }
        return user.toResponse()
    }

    fun searchUsers(query: String, pageable: Pageable): Page<UserResponse> {
        if (query.length < 3)
            return Page.empty(pageable)

        val searchQuery = "*$query*"
        return userRepository.searchByName(searchQuery, pageable).map { it.toResponse() }
    }

    fun createUser(request: UserCreationRequest): UserResponse {
        if (userRepository.existsByUsername(request.username())) {
            throw ResourceAlreadyExistsException("Username already exists")
        }

        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw ResourceAlreadyExistsException("Phone number already exists")
        }

        if (request.role() == Role.PATIENT && request.pesel().isNullOrBlank()) {
            throw ValidationException("PESEL is required for patients")
        }

        val user = User(
            username = request.username(),
            passwordHash = passwordEncoder.encode(request.password()),
            firstName = request.firstName(),
            lastName = request.lastName(),
            phoneNumber = request.phoneNumber(),
            pesel = request.pesel(),
            role = request.role()
        )

        val savedUser = userRepository.save(user)

        log.info("User '{}' registered successfully with role {}", request.username(), request.role())
        return savedUser.toResponse()
    }

    fun updateUser(id: Long, request: UserUpdateRequest): UserResponse {
        val existingUser =
            userRepository.findById(id).orElseThrow { ResourceNotFoundException("User with ID $id not found") }

        existingUser.username = request.username()
        existingUser.firstName = request.firstName()
        existingUser.lastName = request.lastName()
        existingUser.phoneNumber = request.phoneNumber()

        // Only update password if a new one is provided
        if (!request.password().isNullOrBlank()) {
            existingUser.passwordHash = passwordEncoder.encode(request.password())
        }

        if (request.role() == Role.PATIENT) {
            if (request.pesel().isNullOrBlank()) {
                throw ValidationException("PESEL is required for patients")
            }
            existingUser.pesel = request.pesel()
        } else {
            existingUser.pesel = null
        }
        existingUser.role = request.role()

        val updatedUser = userRepository.save(existingUser)

        log.info("User with id {} updated successfully", id)
        return updatedUser.toResponse()
    }

    fun User.toResponse() = UserResponse(
        this.id, this.username, this.firstName, this.lastName, this.phoneNumber, this.pesel, this.role
    )
}