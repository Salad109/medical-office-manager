package io.salad109.medicalofficemanager.visits

import io.salad109.medicalofficemanager.exception.ResourceAlreadyExistsException
import io.salad109.medicalofficemanager.exception.ResourceNotFoundException
import io.salad109.medicalofficemanager.users.Role
import io.salad109.medicalofficemanager.users.internal.User
import io.salad109.medicalofficemanager.visits.internal.Visit
import io.salad109.medicalofficemanager.visits.internal.VisitPdfGenerator
import io.salad109.medicalofficemanager.visits.internal.VisitRepository
import io.salad109.medicalofficemanager.visits.internal.VisitService
import io.salad109.medicalofficemanager.visits.internal.dto.VisitCreationRequest
import io.salad109.medicalofficemanager.visits.internal.dto.VisitUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class VisitServiceTest {

    @Mock
    private lateinit var visitRepository: VisitRepository

    @Mock
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    private val pdfGenerator: VisitPdfGenerator = VisitPdfGenerator()

    private lateinit var visitService: VisitService

    private lateinit var patientUser: User
    private lateinit var doctorUser: User
    private val testAppointmentId = 1L
    private val testAppointmentDate = LocalDate.now().plusDays(1)
    private val testAppointmentTime = LocalTime.of(9, 0)

    @BeforeEach
    fun setUp() {
        visitService = VisitService(visitRepository, applicationEventPublisher, pdfGenerator)

        // Create test users
        patientUser = User(
            id = 1L,
            username = "patient1",
            passwordHash = "P4T13NT",
            firstName = "Joe",
            lastName = "Mama",
            phoneNumber = "123456789",
            pesel = "12345678901",
            role = Role.PATIENT
        )

        doctorUser = User(
            id = 2L,
            username = "doctor1",
            passwordHash = "D0CT0R",
            firstName = "Jane",
            lastName = "Jana",
            phoneNumber = "987654321",
            role = Role.DOCTOR
        )
    }

    @Nested
    inner class GetVisitsByPatientTests {

        @Test
        fun `should return visits for given patient`() {
            // Given
            val visitResponse = VisitResponse(
                1L,
                "Visit notes.",
                LocalDateTime.now(),
                testAppointmentId,
                testAppointmentDate,
                testAppointmentTime,
                doctorUser.id!!,
                doctorUser.firstName,
                doctorUser.lastName,
                patientUser.id!!,
                patientUser.firstName,
                patientUser.lastName
            )
            whenever(visitRepository.findVisitResponsesByPatientId(patientUser.id!!))
                .thenReturn(listOf(visitResponse))

            // When
            val visits = visitService.findVisitResponsesByPatient(patientUser.id!!)

            // Then
            assertThat(visits).hasSize(1)
            assertThat(visits[0].id).isEqualTo(1L)
            assertThat(visits[0].notes).isEqualTo("Visit notes.")
        }
    }

    @Nested
    inner class VisitCompletionTests {

        @Test
        fun `should create visit and publish event`() {
            // Given
            val visitCreationRequest = VisitCreationRequest(
                testAppointmentId,
                "Test notes."
            )
            val now = LocalDateTime.now()
            whenever(visitRepository.existsByAppointmentId(testAppointmentId))
                .thenReturn(false)
            whenever(visitRepository.save(any())).thenAnswer { invocation ->
                (invocation.arguments[0] as Visit).apply {
                    id = 1L
                    completedAt = now
                }
            }
            whenever(visitRepository.findVisitResponseById(1L))
                .thenReturn(
                    Optional.of(
                        VisitResponse(
                            1L,
                            "Test notes.",
                            now,
                            testAppointmentId,
                            testAppointmentDate,
                            testAppointmentTime,
                            doctorUser.id!!,
                            doctorUser.firstName,
                            doctorUser.lastName,
                            patientUser.id!!,
                            patientUser.firstName,
                            patientUser.lastName
                        )
                    )
                )

            // When
            val visitResponse = visitService.markVisitAsCompleted(visitCreationRequest, doctorUser.id!!)

            // Then - verify visit was saved
            verify(visitRepository).save(
                check {
                    assertThat(it.appointmentId).isEqualTo(testAppointmentId)
                    assertThat(it.notes).isEqualTo("Test notes.")
                    assertThat(it.completedByDoctorId).isEqualTo(doctorUser.id)
                }
            )

            verify(applicationEventPublisher).publishEvent(
                check<VisitCompletedEvent> {
                    assertThat(it.appointmentId).isEqualTo(testAppointmentId)
                    assertThat(it.visitId).isEqualTo(1L)
                    assertThat(it.completedAt).isNotNull()
                }
            )

            assertThat(visitResponse.appointmentId).isEqualTo(testAppointmentId)
            assertThat(visitResponse.notes).isEqualTo("Test notes.")
            assertThat(visitResponse.doctorId).isEqualTo(doctorUser.id)
        }

        @Test
        fun `should throw exception when visit already exists for appointment`() {
            // Given
            val visitCreationRequest = VisitCreationRequest(
                testAppointmentId,
                "Test notes."
            )
            whenever(visitRepository.existsByAppointmentId(testAppointmentId))
                .thenReturn(true)

            // Then
            assertThatThrownBy { visitService.markVisitAsCompleted(visitCreationRequest, doctorUser.id!!) }
                .isInstanceOf(ResourceAlreadyExistsException::class.java)
                .hasMessageContaining("Visit already exists")
        }
    }

    @Nested
    inner class UpdateVisitNotesTests {

        @Test
        fun `should update visit notes`() {
            // Given
            val existingVisit = Visit(
                id = 1L,
                appointmentId = testAppointmentId,
                notes = "Old notes.",
                completedByDoctorId = doctorUser.id!!
            )
            val visitUpdateRequest = VisitUpdateRequest(
                "Updated notes."
            )
            whenever(visitRepository.findById(existingVisit.id!!))
                .thenReturn(Optional.of(existingVisit))
            whenever(visitRepository.save(any())).thenAnswer { invocation ->
                invocation.arguments[0]
            }
            whenever(visitRepository.findVisitResponseById(existingVisit.id!!))
                .thenReturn(
                    Optional.of(
                        VisitResponse(
                            existingVisit.id!!,
                            "Updated notes.",
                            LocalDateTime.now(),
                            testAppointmentId,
                            testAppointmentDate,
                            testAppointmentTime,
                            doctorUser.id!!,
                            doctorUser.firstName,
                            doctorUser.lastName,
                            patientUser.id!!,
                            patientUser.firstName,
                            patientUser.lastName
                        )
                    )
                )

            // When
            val updatedVisitResponse = visitService.updateVisitNotes(existingVisit.id!!, visitUpdateRequest)

            // Then
            verify(visitRepository).save(
                check {
                    assertThat(it.notes).isEqualTo("Updated notes.")
                }
            )
            assertThat(updatedVisitResponse.notes).isEqualTo("Updated notes.")
        }

        @Test
        fun `should throw exception when visit to update not found`() {
            // Given
            val visitUpdateRequest = VisitUpdateRequest(
                "Updated notes."
            )
            whenever(visitRepository.findById(999L))
                .thenReturn(Optional.empty())

            // Then
            assertThatThrownBy { visitService.updateVisitNotes(999L, visitUpdateRequest) }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("Visit not found")
        }
    }
}