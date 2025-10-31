package com.medicaloffice.medicalofficemanager.users

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @NotNull
    var username: String? = null

    @NotNull
    @Column(name = "password_hash")
    var passwordHash: String? = null

    @NotNull
    @Column(name = "first_name")
    var firstName: String? = null

    @NotNull
    @Column(name = "last_name")
    var lastName: String? = null

    @NotNull
    @Column(name = "phone_number")
    var phoneNumber: String? = null

    var pesel: String? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    var role: Role? = null
}
