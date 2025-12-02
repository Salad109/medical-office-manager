package io.salad109.medicalofficemanager

import com.jayway.jsonpath.JsonPath
import io.salad109.medicalofficemanager.appointments.internal.Appointment
import io.salad109.medicalofficemanager.appointments.internal.AppointmentRepository
import io.salad109.medicalofficemanager.appointments.internal.AppointmentStatus
import io.salad109.medicalofficemanager.users.Role
import io.salad109.medicalofficemanager.users.internal.User
import io.salad109.medicalofficemanager.users.internal.UserRepository
import io.salad109.medicalofficemanager.visits.internal.Visit
import io.salad109.medicalofficemanager.visits.internal.VisitRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.assertj.MockMvcTester
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
open class BaseControllerTest {

    @Autowired
    protected lateinit var userRepository: UserRepository

    @Autowired
    protected lateinit var appointmentRepository: AppointmentRepository

    @Autowired
    protected lateinit var visitRepository: VisitRepository

    @Autowired
    protected lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    protected lateinit var mockMvcTester: MockMvcTester

    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate

    protected lateinit var patient1: User
    protected lateinit var patient2: User
    protected lateinit var doctor: User
    protected lateinit var receptionist: User

    @BeforeEach
    fun setUp() {
        jdbcTemplate.execute("SET @current_user_id = NULL") // Clear the MySQL session variable for audit triggers
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0")
        jdbcTemplate.execute("TRUNCATE TABLE audit_log")
        jdbcTemplate.execute("TRUNCATE TABLE visits")
        jdbcTemplate.execute("TRUNCATE TABLE appointments")
        jdbcTemplate.execute("TRUNCATE TABLE users")
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1")

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
        val appointment = appointmentRepository.save(
            Appointment(
                patientId = patient1.id!!,
                appointmentDate = LocalDate.of(2025, 5, 25),
                appointmentTime = LocalTime.of(10, 0),
                status = AppointmentStatus.COMPLETED
            )
        )
        visitRepository.save(
            Visit(
                appointmentId = appointment.id!!,
                notes = "Test notes here.",
                completedByDoctorId = doctor.id!!,
                completedAt = LocalDateTime.of(2025, 5, 25, 10, 7)
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
}