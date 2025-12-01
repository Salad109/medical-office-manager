package io.salad109.medicalofficemanager.appointments

import io.salad109.medicalofficemanager.BaseControllerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.LocalTime

class AppointmentControllerTest : BaseControllerTest() {

    @Nested
    inner class BookAppointmentTests {

        @Test
        fun `patient should book appointment for themselves`() {
            // Given
            val token = loginAndGetToken("patient1", "patient-pass")
            val bookAppointmentRequest = """
                {
                    "patientId": %d,
                    "date": "%s",
                    "time": "%s"
                }
                """.format(
                patient1.id,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0)
            )
                .trimIndent()

            // Then
            assertThat(
                mockMvcTester
                    .post()
                    .uri("/api/appointments")
                    .header("Authorization", "Bearer $token")
                    .contentType("application/json")
                    .content(bookAppointmentRequest)
            )
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .extractingPath("patientId")
                .isEqualTo(patient1.id!!.toInt())
        }

        @Test
        fun `receptionist should book appointment for any patient`() {
            // Given
            val token = loginAndGetToken("receptionist1", "receptionist-pass")
            val bookAppointmentRequest = """
                {
                    "patientId": %d,
                    "date": "%s",
                    "time": "%s"
                }
                """.format(
                patient2.id,
                LocalDate.now().plusDays(2),
                LocalTime.of(11, 0)
            )
                .trimIndent()

            // Then
            assertThat(
                mockMvcTester
                    .post()
                    .uri("/api/appointments")
                    .header("Authorization", "Bearer $token")
                    .contentType("application/json")
                    .content(bookAppointmentRequest)
            )
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .extractingPath("patientId")
                .isEqualTo(patient2.id!!.toInt())
        }

        @Test
        fun `should throw exception when patient tries to book appointment for another patient`() {
            // Given
            val token = loginAndGetToken("patient1", "patient-pass")
            val bookAppointmentRequest = """
                {
                    "patientId": %d,
                    "date": "%s",
                    "time": "%s"
                    }
                """.format(
                patient2.id,
                LocalDate.now().plusDays(3),
                LocalTime.of(12, 0)
            )
                .trimIndent()

            // Then
            assertThat(
                mockMvcTester
                    .post()
                    .uri("/api/appointments")
                    .header("Authorization", "Bearer $token")
                    .contentType("application/json")
                    .content(bookAppointmentRequest)
            )
                .hasStatus(HttpStatus.FORBIDDEN)
        }

        @Test
        fun `should throw exception when doctor tries to book appointment`() {
            // Given
            val token = loginAndGetToken("doctor1", "doctor-pass")
            val bookAppointmentRequest = """
                {
                    "patientId": %d,
                    "date": "%s",
                    "time": "%s"
                    }
                """.format(
                patient1.id,
                LocalDate.now().plusDays(4),
                LocalTime.of(13, 0)
            )
                .trimIndent()

            // Then
            assertThat(
                mockMvcTester
                    .post()
                    .uri("/api/appointments")
                    .header("Authorization", "Bearer $token")
                    .contentType("application/json")
                    .content(bookAppointmentRequest)
            )
                .hasStatus(HttpStatus.FORBIDDEN)
        }

        @Test
        fun `should throw exception when booking two appointments at the same time`() {
            // Given
            val token = loginAndGetToken("patient1", "patient-pass")
            val appointmentDate = LocalDate.now().plusDays(5)
            val appointmentTime = LocalTime.of(14, 0)
            val bookAppointmentRequest = """
                {
                    "patientId": %d,
                    "date": "%s",
                    "time": "%s"
                    }""".format(
                patient1.id,
                appointmentDate,
                appointmentTime
            )
                .trimIndent()

            // When
            assertThat(
                mockMvcTester
                    .post()
                    .uri("/api/appointments")
                    .header("Authorization", "Bearer $token")
                    .contentType("application/json")
                    .content(bookAppointmentRequest)
            )
                .hasStatus(HttpStatus.CREATED)

            // Then
            assertThat(
                mockMvcTester
                    .post()
                    .uri("/api/appointments")
                    .header("Authorization", "Bearer $token")
                    .contentType("application/json")
                    .content(bookAppointmentRequest)
            )
                .hasStatus(HttpStatus.CONFLICT)
        }

        @Test
        fun `should throw exception when booking appointment in the past`() {
            // Given
            val token = loginAndGetToken("patient1", "patient-pass")
            val bookAppointmentRequest = """
                {
                    "patientId": %d,
                    "date": "%s",
                    "time": "%s"
                    }""".format(
                patient1.id,
                LocalDate.now().minusDays(1),
                LocalTime.of(9, 0)
            )
                .trimIndent()

            // Then
            assertThat(
                mockMvcTester
                    .post()
                    .uri("/api/appointments")
                    .header("Authorization", "Bearer $token")
                    .contentType("application/json")
                    .content(bookAppointmentRequest)
            )
                .hasStatus(HttpStatus.BAD_REQUEST)
        }
    }
}