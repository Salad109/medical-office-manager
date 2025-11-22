package io.salad109.medicalofficemanager.auth.internal

import io.salad109.medicalofficemanager.auth.CustomUserDetails
import io.salad109.medicalofficemanager.users.UserAuthentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userAuthentication: UserAuthentication) : UserDetailsService {
    override fun loadUserByUsername(username: String): CustomUserDetails {
        val user = userAuthentication.findUserForAuthentication(username)
            ?: throw UsernameNotFoundException("User not found: $username")
        return CustomUserDetails(user)
    }
}