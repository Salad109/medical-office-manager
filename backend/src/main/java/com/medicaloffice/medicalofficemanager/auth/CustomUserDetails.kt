package com.medicaloffice.medicalofficemanager.auth

import com.medicaloffice.medicalofficemanager.users.Role
import com.medicaloffice.medicalofficemanager.users.User
import org.springframework.security.core.CredentialsContainer
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(user: User) : UserDetails, CredentialsContainer {
    val userId: Long = user.id!!
    val role: Role = user.role
    private val usernameField: String = user.username
    private var passwordField: String? = user.passwordHash

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_$role"))
    }

    override fun getUsername(): String {
        return usernameField
    }

    override fun getPassword(): String? {
        return passwordField
    }

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    override fun eraseCredentials() {
        passwordField = null
    }
}
