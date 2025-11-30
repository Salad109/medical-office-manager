package io.salad109.medicalofficemanager.users

import com.jayway.jsonpath.JsonPath
import io.salad109.medicalofficemanager.TestContainersConfig
import io.salad109.medicalofficemanager.audit.internal.AuditLogRepository
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
import org.springframework.jdbc.core.JdbcTemplate
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
    private lateinit var auditLogRepository: AuditLogRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var mockMvcTester: MockMvcTester

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var patient1: User
    private lateinit var patient2: User
    private lateinit var doctor: User
    private lateinit var receptionist: User

    @BeforeEach
    fun setUp() {
        jdbcTemplate.execute("SET @current_user_id = NULL") // Clear the MySQL session variable for audit triggers

        auditLogRepository.deleteAll()
        auditLogRepository.flush()
        userRepository.deleteAll()
        userRepository.flush()

        patient1 = userRepository.save(
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
        patient2 = userRepository.save(
            User(
                username = "patient2",
                passwordHash = passwordEncoder.encode("patient2-pass"),
                firstName = "Jane",
                lastName = "Mama",
                phoneNumber = "555666777",
                role = Role.PATIENT,
                pesel = "10987654321"
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

    fun loginAndGetToken(username: String, password: String): String {
        val loginRequest = """
            {
                "username": "%s",
                "password": "%s"
            }
            """.format(username, password).trimIndent()

        val response = mockMvcTester
            .post()
            .uri("/api/auth/login")
            .contentType("application/json")
            .content(loginRequest)
            .exchange()

        assertThat(response).hasStatus(HttpStatus.OK)

        val responseBody = response.mvcResult.response.contentAsString

        return JsonPath.read(responseBody, "$.token")
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

    @Nested
    inner class GetUserByIdTests {
        @Test
        fun `should get user by id when requested by self`() {
            // Given
            val token = loginAndGetToken("patient1", "patient-pass")

            // Then
            assertThat(
                mockMvcTester
                    .get()
                    .uri("/api/users/${patient1.id}")
                    .header("Authorization", "Bearer $token")
            )
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("username")
                .isEqualTo("patient1")
        }

        @Test
        fun `should get user by id when requested by doctor`() {
            // Given
            val token = loginAndGetToken("doctor1", "doctor-pass")

            // Then
            assertThat(
                mockMvcTester
                    .get()
                    .uri("/api/users/${patient1.id}")
                    .header("Authorization", "Bearer $token")
            )
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("username")
                .isEqualTo("patient1")
        }

        @Test
        fun `should throw when patient requests another patient by id`() {
            // Given
            val token = loginAndGetToken("patient2", "patient2-pass")

            // Then
            assertThat(
                mockMvcTester
                    .get()
                    .uri("/api/users/${patient1.id}")
                    .header("Authorization", "Bearer $token")
            )
                .hasStatus(HttpStatus.FORBIDDEN)
        }

        @Test
        fun `should throw when requesting non-existing user by id`() {
            // Given
            val token = loginAndGetToken("doctor1", "doctor-pass")

            // Then
            assertThat(
                mockMvcTester
                    .get()
                    .uri("/api/users/9999")
                    .header("Authorization", "Bearer $token")
            )
                .hasStatus(HttpStatus.NOT_FOUND)
        }
    }
}