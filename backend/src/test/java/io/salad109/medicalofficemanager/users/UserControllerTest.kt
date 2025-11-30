package io.salad109.medicalofficemanager.users

import io.salad109.medicalofficemanager.TestContainersConfig
import io.salad109.medicalofficemanager.users.internal.User
import io.salad109.medicalofficemanager.users.internal.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.assertj.MockMvcTester

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserControllerTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var mockMvcTester: MockMvcTester

    private lateinit var patient: User
    private lateinit var doctor: User
    private lateinit var receptionist: User

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()

        patient = userRepository.save(
            User(
                username = "patient1",
                passwordHash = passwordEncoder.encode("patient-pass"),
                firstName = "Joe",
                lastName = "Mama",
                phoneNumber = "123456789",
                role = Role.PATIENT,
                pesel = "12345678901"
            )
        )
        doctor = userRepository.save(
            User(
                username = "doctor1",
                passwordHash = passwordEncoder.encode("doctor-pass"),
                firstName = "Marie",
                lastName = "Curie",
                phoneNumber = "987654321",
                role = Role.DOCTOR,
                pesel = null
            )
        )
        receptionist = userRepository.save(
            User(
                username = "receptionist1",
                passwordHash = passwordEncoder.encode("receptionist-pass"),
                firstName = "Pablo",
                lastName = "Escobar",
                phoneNumber = "123123123",
                role = Role.RECEPTIONIST,
                pesel = null
            )
        )
    }

    @Nested
    inner class LoginTests {

        @ParameterizedTest
        @CsvSource(
            "patient1, patient-pass, PATIENT",
            "doctor1, doctor-pass, DOCTOR",
            "receptionist1, receptionist-pass, RECEPTIONIST"
        )
        fun `should login users successfully`(username: String, password: String, expectedRole: String) {
            // Given
            val loginRequest = """
            {
                "username": "%s",
                "password": "%s"
            }
            """.format(username, password).trimIndent()

            // Then
            assertThat(
                mockMvcTester
                    .post()
                    .uri("/api/auth/login")
                    .contentType("application/json")
                    .content(loginRequest)
            )
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("role")
                .isEqualTo(expectedRole)
        }

        @Test
        fun `should fail login with incorrect password`() {
            // Given
            val loginRequest = """
            {
                "username": "patient1",
                "password": "wrong-pass"
            }
            """.trimIndent()

            // Then
            assertThat(
                mockMvcTester
                    .post()
                    .uri("/api/auth/login")
                    .contentType("application/json")
                    .content(loginRequest)
            )
                .hasStatus(HttpStatus.UNAUTHORIZED)
        }
    }
}