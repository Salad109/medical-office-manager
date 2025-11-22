package io.salad109.medicalofficemanager.users

data class UserAuthData(
    val id: Long,
    val username: String,
    val passwordHash: String,
    val role: Role
)