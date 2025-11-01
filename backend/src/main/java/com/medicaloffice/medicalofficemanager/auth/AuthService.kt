package com.medicaloffice.medicalofficemanager.auth

import com.medicaloffice.medicalofficemanager.auth.dto.AuthResponse
import com.medicaloffice.medicalofficemanager.auth.dto.LoginRequest
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
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
}
