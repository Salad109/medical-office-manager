package com.medicaloffice.medicalofficemanager.visits

import com.medicaloffice.medicalofficemanager.appointments.Appointment
import com.medicaloffice.medicalofficemanager.appointments.AppointmentRepository
import com.medicaloffice.medicalofficemanager.appointments.AppointmentStatus
import com.medicaloffice.medicalofficemanager.exception.exceptions.ResourceAlreadyExistsException
import com.medicaloffice.medicalofficemanager.exception.exceptions.ResourceNotFoundException
import com.medicaloffice.medicalofficemanager.users.Role
import com.medicaloffice.medicalofficemanager.users.User
import com.medicaloffice.medicalofficemanager.visits.dto.VisitCreationRequest
import com.medicaloffice.medicalofficemanager.visits.dto.VisitResponse
import com.medicaloffice.medicalofficemanager.visits.dto.VisitUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class VisitServiceTest {

    @Mock
    private lateinit var visitRepository: VisitRepository

    @Mock
    private lateinit var appointmentRepository: AppointmentRepository

    @InjectMocks
    private lateinit var visitService: VisitService

    private lateinit var patientUser: User
    private lateinit var doctorUser: User
    private lateinit var testAppointment: Appointment

    @BeforeEach
    fun setUp() {
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

        // Create test appointment
        testAppointment = Appointment(
            id = 1L,
            patientId = patientUser.id!!,
            appointmentDate = LocalDate.now().plusDays(1),
            appointmentTime = LocalTime.of(9, 0),
            status = AppointmentStatus.SCHEDULED
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
                testAppointment.id!!,
                testAppointment.appointmentDate,
                testAppointment.appointmentTime,
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
            val visits = visitService.getVisitsByPatient(patientUser.id!!)

            // Then
            assertThat(visits).hasSize(1)
            assertThat(visits[0].id).isEqualTo(1L)
            assertThat(visits[0].notes).isEqualTo("Visit notes.")
        }
    }

    @Nested
    inner class VisitCompletionTests {

        @Test
        fun `should mark visit as completed`() {
            // Given
            val visitCreationRequest = VisitCreationRequest(
                testAppointment.id!!,
                "Test notes."
            )
            whenever(appointmentRepository.findById(testAppointment.id!!))
                .thenReturn(Optional.of(testAppointment))
            whenever(visitRepository.existsByAppointmentId(testAppointment.id!!))
                .thenReturn(false)
            whenever(appointmentRepository.save(any())).thenAnswer { invocation ->
                invocation.arguments[0]
            }
            whenever(visitRepository.save(any())).thenAnswer { invocation ->
                (invocation.arguments[0] as Visit).apply { id = 1L }
            }
            whenever(visitRepository.findVisitResponseById(any()))
                .thenReturn(
                    Optional.of(
                        VisitResponse(
                            1L,
                            "Test notes.",
                            LocalDateTime.now(),
                            testAppointment.id!!,
                            testAppointment.appointmentDate,
                            testAppointment.appointmentTime,
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

            // Then
            verify(appointmentRepository).save(
                check {
                    assertThat(it.status).isEqualTo(AppointmentStatus.COMPLETED)
                }
            )
            verify(visitRepository).save(
                check {
                    assertThat(it.appointmentId).isEqualTo(testAppointment.id)
                    assertThat(it.notes).isEqualTo("Test notes.")
                    assertThat(it.completedByDoctorId).isEqualTo(doctorUser.id)
                }
            )
            assertThat(visitResponse.appointmentId).isEqualTo(testAppointment.id)
            assertThat(visitResponse.notes).isEqualTo("Test notes.")
            assertThat(visitResponse.doctorId).isEqualTo(doctorUser.id)
        }

        @Test
        fun `should throw exception when appointment not found`() {
            // Given
            val visitCreationRequest = VisitCreationRequest(
                999L,
                "Test notes."
            )
            whenever(appointmentRepository.findById(999L))
                .thenReturn(Optional.empty())

            // Then
            assertThatThrownBy { visitService.markVisitAsCompleted(visitCreationRequest, doctorUser.id!!) }
                .isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("Appointment not found")
        }

        @Test
        fun `should throw exception when appointment status is already completed`() {
            // Given
            val visitCreationRequest = VisitCreationRequest(
                testAppointment.id!!,
                "Test notes."
            )
            testAppointment.status = AppointmentStatus.COMPLETED
            whenever(appointmentRepository.findById(testAppointment.id!!))
                .thenReturn(Optional.of(testAppointment))

            // Then
            assertThatThrownBy { visitService.markVisitAsCompleted(visitCreationRequest, doctorUser.id!!) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("Only scheduled or no-show appointments can be marked as completed")
        }

        @Test
        fun `should throw exception when visit already exists for appointment`() {
            // Given
            val visitCreationRequest = VisitCreationRequest(
                testAppointment.id!!,
                "Test notes."
            )
            whenever(appointmentRepository.findById(testAppointment.id!!))
                .thenReturn(Optional.of(testAppointment))
            whenever(visitRepository.existsByAppointmentId(testAppointment.id!!))
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
                appointmentId = testAppointment.id!!,
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
                            testAppointment.id!!,
                            testAppointment.appointmentDate,
                            testAppointment.appointmentTime,
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