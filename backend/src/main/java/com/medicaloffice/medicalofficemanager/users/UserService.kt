package com.medicaloffice.medicalofficemanager.users

import com.medicaloffice.medicalofficemanager.users.dto.UserCreationRequest
import com.medicaloffice.medicalofficemanager.users.dto.UserResponse
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
        val user = userRepository.findById(id).orElseThrow { NoSuchElementException("User with id $id not found") }
        return user.toResponse()
    }

    fun searchUsers(query: String, pageable: Pageable): Page<UserResponse> {
        if (query.length < 3)
            return Page.empty(pageable)

        val searchQuery = "*$query*"
        return userRepository.searchByName(searchQuery, pageable).map { it.toResponse() }
    }

    fun createUser(request: UserCreationRequest): UserResponse {
        require(!userRepository.existsByUsername(request.username())) {
            "Username already exists"
        }

        require(!userRepository.existsByPhoneNumber(request.phoneNumber())) {
            "Phone number already exists"
        }

        require(request.role() != Role.PATIENT || !request.pesel().isNullOrBlank()) {
            "PESEL is required for patients"
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

    fun updateUser(id: Long, user: UserCreationRequest): UserResponse {
        val existingUser =
            userRepository.findById(id).orElseThrow { NoSuchElementException("User with id $id not found") }

        existingUser.firstName = user.firstName()
        existingUser.lastName = user.lastName()
        existingUser.phoneNumber = user.phoneNumber()
        existingUser.passwordHash = passwordEncoder.encode(user.password())
        if (user.role() == Role.PATIENT) {
            require(!user.pesel().isNullOrBlank()) {
                "PESEL is required for patients"
            }
            existingUser.pesel = user.pesel()
        } else {
            existingUser.pesel = null
        }
        existingUser.role = user.role()

        val updatedUser = userRepository.save(existingUser)

        log.info("User with id {} updated successfully", id)
        return updatedUser.toResponse()
    }

    fun User.toResponse() = UserResponse(
        this.id, this.username, this.firstName, this.lastName, this.phoneNumber, this.pesel, this.role
    )
}