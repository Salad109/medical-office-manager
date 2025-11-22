package io.salad109.medicalofficemanager.users

interface UserAuthentication {
    fun findUserForAuthentication(username: String): UserAuthData?
}