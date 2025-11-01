package com.medicaloffice.medicalofficemanager.users

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:NotNull
    var username: String? = null,

    @field:NotNull
    @Column(name = "password_hash")
    var passwordHash: String? = null,

    @field:NotNull
    @Column(name = "first_name")
    var firstName: String? = null,

    @field:NotNull
    @Column(name = "last_name")
    var lastName: String? = null,

    @field:NotNull
    @Column(name = "phone_number")
    var phoneNumber: String? = null,

    var pesel: String? = null,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    var role: Role? = null
)
