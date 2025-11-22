package io.salad109.medicalofficemanager.users

import io.salad109.medicalofficemanager.exception.exceptions.InvalidRoleException
import io.salad109.medicalofficemanager.exception.exceptions.ResourceAlreadyExistsException
import io.salad109.medicalofficemanager.exception.exceptions.ResourceNotFoundException
import io.salad109.medicalofficemanager.users.dto.UserCreationRequest
import io.salad109.medicalofficemanager.users.dto.UserResponse
import io.salad109.medicalofficemanager.users.dto.UserResponseWithVisits
import io.salad109.medicalofficemanager.users.dto.UserUpdateRequest
import io.salad109.medicalofficemanager.visits.VisitRepository
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val visitRepository: VisitRepository
) : UserManagement {
    private val log = LoggerFactory.getLogger(UserService::class.java)

    fun getAllUsers(pageable: Pageable): Page<UserResponse> {
        return userRepository.findAll(pageable).map { user ->
            user.toResponse()
        }.also { log.debug("Fetched ${it.totalElements} users") }
    }

    fun getUserById(id: Long): UserResponse {
        val user = userRepository.findById(id).orElseThrow { ResourceNotFoundException("User with ID $id not found") }
        log.debug("Fetched user with ID {}", id)
        return user.toResponse()
    }

    fun searchUsers(query: String, pageable: Pageable): Page<UserResponse> {
        if (query.length < 3)
            return Page.empty(pageable)

        val searchQuery = "*${query}*"
        return userRepository.searchByName(searchQuery, pageable).map { it.toResponse() }
            .also { log.debug("Searched users with query '{}', found {} results", query, it.totalElements) }
    }

    fun getPatientWithVisits(patientId: Long): UserResponseWithVisits {
        val user = userRepository.findById(patientId)
            .orElseThrow { ResourceNotFoundException("User with ID $patientId not found") }

        val visits = visitRepository.findVisitResponsesByPatientId(patientId)

        log.debug("Fetched patient with ID {} and {} visits", patientId, visits.size)
        return UserResponseWithVisits(user.toResponse(), visits)
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

    /**
     * Removes passwordHash from User when converting to UserResponse.
     */
    fun User.toResponse() = UserResponse(
        this.id, this.username, this.firstName, this.lastName, this.phoneNumber, this.pesel, this.role
    )

    override fun validatePatient(patientId: Long) {
        userRepository.findById(patientId)
            .orElseThrow { ResourceNotFoundException("Patient with ID $patientId not found") }
            .takeIf { it.role == Role.PATIENT }
            ?: throw InvalidRoleException("User with ID $patientId is not a patient")
    }
}