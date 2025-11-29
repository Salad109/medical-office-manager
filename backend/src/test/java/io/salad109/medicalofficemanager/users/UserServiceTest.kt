package io.salad109.medicalofficemanager.users

import io.salad109.medicalofficemanager.exception.InvalidRoleException
import io.salad109.medicalofficemanager.exception.ResourceAlreadyExistsException
import io.salad109.medicalofficemanager.exception.ResourceNotFoundException
import io.salad109.medicalofficemanager.users.internal.User
import io.salad109.medicalofficemanager.users.internal.UserRepository
import io.salad109.medicalofficemanager.users.internal.UserService
import io.salad109.medicalofficemanager.users.internal.dto.UserCreationRequest
import io.salad109.medicalofficemanager.users.internal.dto.UserUpdateRequest
import io.salad109.medicalofficemanager.visits.VisitManagement
import jakarta.validation.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var visitManagement: VisitManagement

    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()

    private lateinit var userService: UserService

    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        // Manually instantiate UserService with real PasswordEncoder
        userService = UserService(userRepository, passwordEncoder, visitManagement)

        user = User(
            id = 1L,
            username = "joeMama",
            passwordHash = "hashedPassword",
            firstName = "Joe",
            lastName = "Mama",
            phoneNumber = "123456789",
            role = Role.PATIENT,
            pesel = "12345678901"
        )
    }

    @Nested
    inner class GetUserTests {

        @Test
        fun `should get all users`() {
            // Given
            whenever(userRepository.findAll(any(Pageable::class.java))).thenReturn(PageImpl(listOf(user)))

            // When
            val users = userService.getAllUsers(Pageable.unpaged())

            // Then
            assertThat(users).hasSize(1)
            assertThat(users.content[0].username).isEqualTo("joeMama")
        }

        @Test
        fun `should get user by ID`() {
            // Given
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            // When
            val userResponse = userService.getUserById(1L)

            // Then
            assertThat(userResponse.username).isEqualTo("joeMama")
        }

        @Test
        fun `should throw exception when user not found by ID`() {
            // Given
            whenever(userRepository.findById(2L)).thenReturn(Optional.empty())

            // Then
            assertThatThrownBy {
                userService.getUserById(2L)
            }.isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("User with ID 2 not found")
        }

        @Test
        fun `should search users by query`() {
            // Given
            whenever(userRepository.searchByName("*Joe*", Pageable.unpaged())).thenReturn(PageImpl(listOf(user)))

            // When
            val users = userService.searchUsers("Joe", Pageable.unpaged())

            // Then
            assertThat(users).hasSize(1)
            assertThat(users.content[0].firstName).isEqualTo("Joe")
        }

        @Test
        fun `should return empty page when search query is too short`() {
            // When
            val users = userService.searchUsers("J", Pageable.unpaged())

            // Then
            assertThat(users).isEmpty()
        }

        @Test
        fun `should get patient with visits`() {
            // Given
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(visitManagement.findVisitResponsesByPatient(1L)).thenReturn(emptyList())

            // When
            val patientWithVisits = userService.getPatientWithVisits(1L)

            // Then
            assertThat(patientWithVisits.patient.username).isEqualTo("joeMama")
            assertThat(patientWithVisits.visits).isEmpty()
        }

        @Test
        fun `should throw exception when getting patient with visits for non-existent user`() {
            // Given
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            // Then
            assertThatThrownBy {
                userService.getPatientWithVisits(999L)
            }.isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("User with ID 999 not found")
        }
    }

    @Nested
    inner class CreateUserTests {

        @Test
        fun `should create user successfully`() {
            // Given
            val request = UserCreationRequest(
                "newUser",
                "password123",
                "New",
                "User",
                "987654321",
                "10987654321",
                Role.PATIENT
            )
            whenever(userRepository.existsByUsername("newUser")).thenReturn(false)
            whenever(userRepository.existsByPhoneNumber("987654321")).thenReturn(false)
            whenever(userRepository.save(any(User::class.java))).thenAnswer { invocation ->
                (invocation.arguments[0] as User).apply {
                    id = 2L
                }
            }

            // When
            val createdUser = userService.createUser(request)

            // Then
            assertThat(createdUser.id).isEqualTo(2L)
            assertThat(createdUser.username).isEqualTo("newUser")
        }

        @Test
        fun `should throw exception when creating user with existing username`() {
            // Given
            val request = UserCreationRequest(
                "joeMama",
                "password123",
                "Joe",
                "Mama",
                "987654321",
                "10987654321",
                Role.PATIENT
            )
            whenever(userRepository.existsByUsername("joeMama")).thenReturn(true)

            // Then
            assertThatThrownBy {
                userService.createUser(request)
            }.isInstanceOf(ResourceAlreadyExistsException::class.java)
                .hasMessageContaining("Username already exists")
        }

        @Test
        fun `should throw exception when creating user with existing phone number`() {
            // Given
            val request = UserCreationRequest(
                "newUser",
                "password123",
                "New",
                "User",
                "123456789",
                "10987654321",
                Role.PATIENT
            )
            whenever(userRepository.existsByUsername("newUser")).thenReturn(false)
            whenever(userRepository.existsByPhoneNumber("123456789")).thenReturn(true)

            // Then
            assertThatThrownBy {
                userService.createUser(request)
            }.isInstanceOf(ResourceAlreadyExistsException::class.java)
                .hasMessageContaining("Phone number already exists")
        }

        @Test
        fun `should throw exception when creating patient without PESEL`() {
            // Given
            val request = UserCreationRequest(
                "newUser",
                "password123",
                "New",
                "User",
                "987654321",
                null,
                Role.PATIENT
            )
            whenever(userRepository.existsByUsername("newUser")).thenReturn(false)
            whenever(userRepository.existsByPhoneNumber("987654321")).thenReturn(false)

            // Then
            assertThatThrownBy {
                userService.createUser(request)
            }.isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("PESEL is required for patients")
        }
    }

    @Nested
    inner class UpdateUserTests {

        @Test
        fun `should update user successfully`() {
            // Given
            val request = UserUpdateRequest(
                "updatedUser",
                "newPassword123",
                "Updated",
                "User",
                "111222333",
                "10987654321",
                Role.PATIENT
            )
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(userRepository.save(any(User::class.java))).thenAnswer { invocation ->
                invocation.arguments[0]
            }

            // When
            val updatedUser = userService.updateUser(1L, request)

            // Then
            verify(userRepository).save(check {
                assertThat(it.passwordHash).isNotEqualTo("hashedPassword")
            })
            assertThat(updatedUser.username).isEqualTo("updatedUser")
        }

        @Test
        fun `should not change password if not provided during update`() {
            // Given
            val request = UserUpdateRequest(
                "updatedUser",
                null,
                "Updated",
                "User",
                "111222333",
                "10987654321",
                Role.PATIENT
            )
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(userRepository.save(any(User::class.java))).thenAnswer { invocation ->
                invocation.arguments[0]
            }

            // When
            val updatedUser = userService.updateUser(1L, request)

            // Then
            verify(userRepository).save(check {
                assertThat(it.passwordHash).isEqualTo("hashedPassword")
            })
            assertThat(updatedUser.username).isEqualTo("updatedUser")
        }

        @Test
        fun `should throw exception when updating non-existent user`() {
            // Given
            val request = UserUpdateRequest(
                "updatedUser",
                "newPassword123",
                "Updated",
                "User",
                "111222333",
                "10987654321",
                Role.PATIENT
            )
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            // Then
            assertThatThrownBy {
                userService.updateUser(999L, request)
            }.isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("User with ID 999 not found")
        }

        @Test
        fun `should throw exception when updating patient without PESEL`() {
            // Given
            val request = UserUpdateRequest(
                "updatedUser",
                "newPassword123",
                "Updated",
                "User",
                "111222333",
                null,
                Role.PATIENT
            )
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            // Then
            assertThatThrownBy {
                userService.updateUser(1L, request)
            }.isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("PESEL is required for patients")
        }
    }

    @Nested
    inner class PatientValidationTests {

        @Test
        fun `should validate patient successfully`() {
            // Given
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            // Then
            assertDoesNotThrow { userService.validatePatient(1L) }
        }

        @Test
        fun `should throw exception when validating non-existent patient`() {
            // Given
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            // Then
            assertThatThrownBy {
                userService.validatePatient(999L)
            }.isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("Patient with ID 999 not found")
        }

        @Test
        fun `should throw exception when user is not a patient`() {
            // Given
            val doctorUser = user.apply { role = Role.DOCTOR }
            whenever(userRepository.findById(2L)).thenReturn(Optional.of(doctorUser))

            // Then
            assertThatThrownBy {
                userService.validatePatient(2L)
            }.isInstanceOf(InvalidRoleException::class.java)
                .hasMessageContaining("User with ID 2 is not a patient")
        }
    }

    @Nested
    inner class FindUserForAuthenticationTests {

        @Test
        fun `should find user by username for authentication`() {
            // Given
            whenever(userRepository.findByUsername("joeMama")).thenReturn(Optional.of(user))

            // When
            val foundUser = userService.findUserForAuthentication("joeMama")

            // Then
            assertThat(foundUser?.username).isEqualTo("joeMama")
        }

        @Test
        fun `should return null when user not found by username for authentication`() {
            // Given
            whenever(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty())

            // When
            val foundUser = userService.findUserForAuthentication("unknownUser")

            // Then
            assertThat(foundUser).isNull()
        }
    }
}