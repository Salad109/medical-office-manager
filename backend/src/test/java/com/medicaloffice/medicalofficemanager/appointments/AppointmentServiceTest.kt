package com.medicaloffice.medicalofficemanager.appointments

import com.medicaloffice.medicalofficemanager.appointments.dto.BookAppointmentRequest
import com.medicaloffice.medicalofficemanager.exception.exceptions.*
import com.medicaloffice.medicalofficemanager.users.Role
import com.medicaloffice.medicalofficemanager.users.User
import com.medicaloffice.medicalofficemanager.users.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.access.AccessDeniedException
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class AppointmentServiceTest {

    @Mock
    private lateinit var appointmentRepository: AppointmentRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var appointmentService: AppointmentService

    private lateinit var patientUser: User
    private lateinit var doctorUser: User
    private lateinit var receptionistUser: User
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

        receptionistUser = User(
            id = 3L,
            username = "receptionist1",
            passwordHash = "R3C3PT10N1ST",
            firstName = "Bob",
            lastName = "Bobert",
            phoneNumber = "555555555",
            role = Role.RECEPTIONIST
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
    inner class GetAvailableSlotsTests {

        @Test
        fun `should return all slots when no appointments exist`() {
            // Given
            val testDate = LocalDate.now().plusDays(1)
            whenever(appointmentRepository.findByAppointmentDate(testDate)).thenReturn(emptyList())

            // When
            val availableSlots = appointmentService.getAvailableSlots(testDate)

            // Then
            assertThat(availableSlots)
                .isNotEmpty()
                .contains("09:00", "09:30", "10:00", "16:00", "16:30")
                .doesNotContain("17:00")
        }

        @Test
        fun `should handle booked slots`() {
            // Given
            val testDate = LocalDate.now().plusDays(1)
            val bookedAppointments = listOf(
                Appointment(1L, 1L, testDate, LocalTime.of(9, 0), AppointmentStatus.SCHEDULED),
                Appointment(2L, 2L, testDate, LocalTime.of(10, 0), AppointmentStatus.SCHEDULED),
                Appointment(3L, 3L, testDate, LocalTime.of(14, 0), AppointmentStatus.SCHEDULED)
            )
            whenever(appointmentRepository.findByAppointmentDate(testDate)).thenReturn(bookedAppointments)

            // When
            val availableSlots = appointmentService.getAvailableSlots(testDate)

            // Then
            assertThat(availableSlots)
                .doesNotContain("09:00", "10:00", "14:00")
                .contains("09:30", "10:30", "14:30")
        }
    }

    @Nested
    inner class GetAppointmentsByDateTests {

        @Test
        fun `should return empty list when no appointments exist`() {
            // Given
            val testDate = LocalDate.now().plusDays(1)
            whenever(appointmentRepository.findByAppointmentDate(testDate)).thenReturn(emptyList())

            // When
            val appointments = appointmentService.getAppointmentsByDate(testDate)

            // Then
            assertThat(appointments).isEmpty()
        }

        @Test
        fun `should return appointments for given date`() {
            // Given
            val testDate = LocalDate.now().plusDays(1)
            whenever(appointmentRepository.findByAppointmentDate(testDate)).thenReturn(listOf(testAppointment))

            // When
            val appointments = appointmentService.getAppointmentsByDate(testDate)

            // Then
            assertThat(appointments).hasSize(1)
            assertThat(appointments[0].id).isEqualTo(testAppointment.id)
            assertThat(appointments[0].patientId).isEqualTo(testAppointment.patientId)

        }

        @Test
        fun `should return multiple appointments`() {
            // Given
            val testDate = LocalDate.now().plusDays(1)
            val appointment1 = Appointment(1L, 1L, testDate, LocalTime.of(9, 0), AppointmentStatus.SCHEDULED)
            val appointment2 = Appointment(2L, 2L, testDate, LocalTime.of(10, 0), AppointmentStatus.SCHEDULED)
            val mockAppointments = listOf(appointment1, appointment2)
            whenever(appointmentRepository.findByAppointmentDate(testDate)).thenReturn(mockAppointments)

            // When
            val appointments = appointmentService.getAppointmentsByDate(testDate)

            // Then
            assertThat(appointments).hasSize(2)
        }
    }

    @Nested
    inner class BookAppointmentTests {

        @Test
        fun `should book appointment successfully`() {
            // Given
            val futureDate = LocalDate.now().plusDays(1)
            val validTime = LocalTime.of(9, 0)
            val request = BookAppointmentRequest(patientUser.id, futureDate, validTime)

            whenever(userRepository.findById(patientUser.id!!)).thenReturn(Optional.of(patientUser))
            whenever(appointmentRepository.findByAppointmentDate(futureDate)).thenReturn(emptyList())
            whenever(appointmentRepository.save(any(Appointment::class.java))).thenAnswer { invocation ->
                (invocation.arguments[0] as Appointment).apply { id = 1L }
            }

            // When
            val response = appointmentService.bookAppointment(request)

            // Then
            assertThat(response).isNotNull
            assertThat(response.id).isEqualTo(1L)
            assertThat(response.patientId).isEqualTo(patientUser.id)
            assertThat(response.date).isEqualTo(futureDate)
            assertThat(response.time).isEqualTo(validTime)
            assertThat(response.status).isEqualTo(AppointmentStatus.SCHEDULED)
        }

        @Test
        fun `should throw exception when user is not a patient`() {
            // Given
            val futureDate = LocalDate.now().plusDays(1)
            val request = BookAppointmentRequest(doctorUser.id, futureDate, LocalTime.of(9, 0))

            whenever(userRepository.findById(doctorUser.id!!)).thenReturn(Optional.of(doctorUser))

            // Then
            assertThatThrownBy { appointmentService.bookAppointment(request) }
                .isInstanceOf(InvalidRoleException::class.java)
                .hasMessageContaining("is not a patient")
            verify(appointmentRepository, never()).save(any()) // make sure save is never called
        }

        @Test
        fun `should throw exception when user does not exist`() {
            // Given
            val futureDate = LocalDate.now().plusDays(1)
            val request = BookAppointmentRequest(999L, futureDate, LocalTime.of(9, 0))

            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            // Then
            assertThatThrownBy { appointmentService.bookAppointment(request) }
                .isInstanceOf(UserNotFoundException::class.java)
                .hasMessageContaining("Patient with ID 999 not found")
            verify(appointmentRepository, never()).save(any())
        }

        @Test
        fun `should throw exception when booking in the past`() {
            // Given
            val pastDate = LocalDate.now().minusDays(1)
            val request = BookAppointmentRequest(patientUser.id, pastDate, LocalTime.of(9, 0))

            whenever(userRepository.findById(patientUser.id!!)).thenReturn(Optional.of(patientUser))

            // Then
            assertThatThrownBy { appointmentService.bookAppointment(request) }
                .isInstanceOf(InvalidTimeSlotException::class.java)
                .hasMessageContaining("Cannot book appointment in the past")
            verify(appointmentRepository, never()).save(any())
        }

        @ParameterizedTest
        @CsvSource(
            "1,0", // before office hours
            "17,0", // after office hours
            "11,13" // invalid interval
        )
        fun `should throw exception for invalid times`(hour: String, minute: String) {
            // Given
            val futureDate = LocalDate.now().plusDays(1)
            val time = LocalTime.of(hour.toInt(), minute.toInt())
            val request = BookAppointmentRequest(patientUser.id, futureDate, time)

            whenever(userRepository.findById(patientUser.id!!)).thenReturn(Optional.of(patientUser))

            // Then
            assertThatThrownBy { appointmentService.bookAppointment(request) }
                .isInstanceOf(InvalidTimeSlotException::class.java)
                .hasMessageContaining("Invalid time slot")
            verify(appointmentRepository, never()).save(any())
        }

        @Test
        fun `should throw exception when slot is already booked`() {
            // Given
            val futureDate = LocalDate.now().plusDays(1)
            val time = LocalTime.of(9, 0)
            val request = BookAppointmentRequest(patientUser.id, futureDate, time)
            val existingAppointment = Appointment(2L, 2L, futureDate, time, AppointmentStatus.SCHEDULED)

            whenever(userRepository.findById(patientUser.id!!)).thenReturn(Optional.of(patientUser))
            whenever(appointmentRepository.findByAppointmentDate(futureDate)).thenReturn(listOf(existingAppointment))

            // Then
            assertThatThrownBy { appointmentService.bookAppointment(request) }
                .isInstanceOf(TimeSlotAlreadyBookedException::class.java)
                .hasMessageContaining("already booked")
            verify(appointmentRepository, never()).save(any())
        }
    }

    @Nested
    inner class CancelAppointmentTests {

        @Test
        fun `should allow patient to cancel own appointment`() {
            // Given
            whenever(appointmentRepository.findById(testAppointment.id!!)).thenReturn(Optional.of(testAppointment))

            // When
            appointmentService.cancelAppointment(testAppointment.id!!, patientUser.id!!, Role.PATIENT)

            // Then
            verify(appointmentRepository).delete(testAppointment)
        }

        @Test
        fun `should throw exception when patient cancels another appointment`() {
            // Given
            val differentPatientId = 999L
            whenever(appointmentRepository.findById(testAppointment.id!!)).thenReturn(Optional.of(testAppointment))

            // When / Then
            assertThatThrownBy {
                appointmentService.cancelAppointment(testAppointment.id!!, differentPatientId, Role.PATIENT)
            }
                .isInstanceOf(AccessDeniedException::class.java)
                .hasMessageContaining("Patients can only cancel their own appointments")
            verify(appointmentRepository, never()).delete(any())
        }

        @Test
        fun `should allow receptionist to cancel any appointment`() {
            // Given
            whenever(appointmentRepository.findById(testAppointment.id!!)).thenReturn(Optional.of(testAppointment))

            // When
            appointmentService.cancelAppointment(testAppointment.id!!, receptionistUser.id!!, Role.RECEPTIONIST)

            // Then
            verify(appointmentRepository).delete(testAppointment)
        }

        @Test
        fun `should throw exception when doctor cancels appointment`() {
            // Given
            whenever(appointmentRepository.findById(testAppointment.id!!)).thenReturn(Optional.of(testAppointment))

            // Then
            assertThatThrownBy {
                appointmentService.cancelAppointment(testAppointment.id!!, doctorUser.id!!, Role.DOCTOR)
            }
                .isInstanceOf(AccessDeniedException::class.java)
                .hasMessageContaining("Doctors cannot cancel appointments")
            verify(appointmentRepository, never()).delete(any())
        }

        @Test
        fun `should throw exception when appointment does not exist`() {
            // Given
            val nonExistentId = 999L
            whenever(appointmentRepository.findById(nonExistentId)).thenReturn(Optional.empty())

            // Then
            assertThatThrownBy {
                appointmentService.cancelAppointment(nonExistentId, patientUser.id!!, Role.PATIENT)
            }
                .isInstanceOf(AppointmentNotFoundException::class.java)
                .hasMessageContaining("Appointment not found")
            verify(appointmentRepository, never()).delete(any())
        }
    }
}