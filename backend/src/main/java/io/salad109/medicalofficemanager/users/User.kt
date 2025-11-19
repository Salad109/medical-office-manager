package io.salad109.medicalofficemanager.users

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:NotNull
    var username: String,

    @field:NotNull
    @Column(name = "password_hash")
    var passwordHash: String,

    @field:NotNull
    @Column(name = "first_name")
    var firstName: String,

    @field:NotNull
    @Column(name = "last_name")
    var lastName: String,

    @field:NotNull
    @Column(name = "phone_number")
    var phoneNumber: String,

    var pesel: String? = null,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    var role: Role
)
