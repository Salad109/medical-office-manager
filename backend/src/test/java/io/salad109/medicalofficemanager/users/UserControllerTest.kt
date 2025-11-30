package io.salad109.medicalofficemanager.users

import io.salad109.medicalofficemanager.BaseControllerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.http.HttpStatus

class UserControllerTest : BaseControllerTest() {

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