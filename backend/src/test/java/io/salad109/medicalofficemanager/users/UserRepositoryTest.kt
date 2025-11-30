package io.salad109.medicalofficemanager.users

import io.salad109.medicalofficemanager.TestContainersConfig
import io.salad109.medicalofficemanager.audit.internal.AuditLogRepository
import io.salad109.medicalofficemanager.users.internal.User
import io.salad109.medicalofficemanager.users.internal.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest

@DataJpaTest
@Import(TestContainersConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var auditLogRepository: AuditLogRepository

    @BeforeEach
    fun setUp() {
        auditLogRepository.deleteAll()
        auditLogRepository.flush()
        userRepository.deleteAll()
        userRepository.flush()
    }

    @Test
    fun `should find users by role`() {
        // Given
        val patient = User(
            username = "patient1",
            passwordHash = "hash",
            firstName = "Patient",
            lastName = "One",
            phoneNumber = "111111111",
            role = Role.PATIENT,
            pesel = "11111111111"
        )
        val doctor = User(
            username = "doctor1",
            passwordHash = "hash",
            firstName = "Doctor",
            lastName = "One",
            phoneNumber = "222222222",
            role = Role.DOCTOR,
            pesel = null
        )
        userRepository.save(patient)
        userRepository.save(doctor)

        // When
        val patients = userRepository.findByRole(Role.PATIENT, PageRequest.of(0, 10))
        val doctors = userRepository.findByRole(Role.DOCTOR, PageRequest.of(0, 10))

        // Then
        assertThat(patients.content).hasSize(1)
        assertThat(patients.content[0].username).isEqualTo("patient1")
        assertThat(doctors.content).hasSize(1)
        assertThat(doctors.content[0].username).isEqualTo("doctor1")
    }
}