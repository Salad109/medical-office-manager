package io.salad109.medicalofficemanager.appointments.internal;

import io.salad109.medicalofficemanager.appointments.internal.dto.AppointmentWithDetailsResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByAppointmentDate(LocalDate appointmentDate);

    @Query("SELECT new io.salad109.medicalofficemanager.appointments.internal.dto.AppointmentWithDetailsResponse(" +
            "a.id, a.patientId, p.firstName, p.lastName, p.phoneNumber, a.appointmentDate, a.appointmentTime, a.status) " +
            "FROM Appointment a " +
            "JOIN User p ON a.patientId = p.id " +
            "WHERE a.appointmentDate = :date " +
            "ORDER BY a.appointmentTime ASC")
    List<AppointmentWithDetailsResponse> findAppointmentsWithDetailsByDate(@Param("date") LocalDate date);

    @Query("SELECT new io.salad109.medicalofficemanager.appointments.internal.dto.AppointmentWithDetailsResponse(" +
            "a.id, a.patientId, p.firstName, p.lastName, p.phoneNumber, a.appointmentDate, a.appointmentTime, a.status) " +
            "FROM Appointment a " +
            "JOIN User p ON a.patientId = p.id " +
            "WHERE a.patientId = :patientId " +
            "ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<AppointmentWithDetailsResponse> findAppointmentsByPatientId(@Param("patientId") Long patientId);
}
