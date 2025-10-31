package com.medicaloffice.medicalofficemanager.auth

import com.medicaloffice.medicalofficemanager.auth.dto.AuthResponse
import com.medicaloffice.medicalofficemanager.auth.dto.LoginRequest
import com.medicaloffice.medicalofficemanager.users.Role
import com.medicaloffice.medicalofficemanager.users.User
import com.medicaloffice.medicalofficemanager.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    private val log = LoggerFactory.getLogger(AuthService::class.java)

    fun login(request: LoginRequest): AuthResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
            )
        )

        val userDetails = authentication.principal as UserDetails
        val token = jwtService.generateToken(userDetails)

        val role = (userDetails as? CustomUserDetails)?.role
            ?: throw IllegalStateException("Expected CustomUserDetails but got ${userDetails::class.simpleName}")

        log.info("User '{}' logged in successfully", request.username())
        return AuthResponse(token, role)
    }

    fun register(request: RegisterRequest): RegisterResponse {
        require(!userRepository.existsByUsername(request.username())) {
            "Username already exists"
        }

        require(!userRepository.existsByPhoneNumber(request.phoneNumber())) {
            "Phone number already exists"
        }

        require(request.role() != Role.PATIENT || !request.pesel().isNullOrBlank()) {
            "PESEL is required for patients"
        }

        val user = User().apply {
            username = request.username()
            passwordHash = passwordEncoder.encode(request.password())
            firstName = request.firstName()
            lastName = request.lastName()
            phoneNumber = request.phoneNumber()
            pesel = request.pesel()
            role = request.role()
        }

        userRepository.save(user)

        log.info("User '{}' registered successfully with role {}", request.username(), request.role())
        return RegisterResponse("User registered successfully", request.username())
    }
}
