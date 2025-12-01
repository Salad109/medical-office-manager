package io.salad109.medicalofficemanager.visits

import io.salad109.medicalofficemanager.BaseControllerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class VisitControllerTest : BaseControllerTest() {

    @Nested
    inner class ReportGenerationTests {

        @Test
        fun `should generate visit report for patient with visits`() {
            // todo
        }

        @Test
        fun `should not generate visit report when patient has no visits`() {
            // Given
            val patientId = patient2.id
            val token = loginAndGetToken("doctor1", "doctor-pass")

            // Then
            assertThat(
                mockMvcTester
                    .get()
                    .uri("/api/visits/patient/$patientId/report")
                    .header("Authorization", "Bearer $token")
            )
                .hasStatus(HttpStatus.BAD_REQUEST)
        }

        @Test
        fun `should not generate visit report for non-patient users`() {
            // Given
            val nonPatientId = receptionist.id
            val token = loginAndGetToken("doctor1", "doctor-pass")

            // Then
            assertThat(
                mockMvcTester
                    .get()
                    .uri("/api/visits/patient/$nonPatientId/report")
                    .header("Authorization", "Bearer $token")
            )
                .hasStatus(HttpStatus.BAD_REQUEST)
        }
    }
}