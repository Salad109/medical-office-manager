package io.salad109.medicalofficemanager.visits;

import io.salad109.medicalofficemanager.visits.dto.VisitResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    @Query("SELECT new io.salad109.medicalofficemanager.visits.dto.VisitResponse(" +
            "v.id, v.notes, v.completedAt, " +
            "a.id, a.appointmentDate, a.appointmentTime, " +
            "d.id, d.firstName, d.lastName, " +
            "p.id, p.firstName, p.lastName) " +
            "FROM Visit v " +
            "JOIN Appointment a ON v.appointmentId = a.id " +
            "JOIN User d ON v.completedByDoctorId = d.id " +
            "JOIN User p ON a.patientId = p.id " +
            "WHERE a.patientId = :patientId " +
            "ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    List<VisitResponse> findVisitResponsesByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT new io.salad109.medicalofficemanager.visits.dto.VisitResponse(" +
            "v.id, v.notes, v.completedAt, " +
            "a.id, a.appointmentDate, a.appointmentTime, " +
            "d.id, d.firstName, d.lastName, " +
            "p.id, p.firstName, p.lastName) " +
            "FROM Visit v " +
            "JOIN Appointment a ON v.appointmentId = a.id " +
            "JOIN User d ON v.completedByDoctorId = d.id " +
            "JOIN User p ON a.patientId = p.id " +
            "WHERE v.id = :visitId")
    Optional<VisitResponse> findVisitResponseById(@Param("visitId") Long visitId);

    boolean existsByAppointmentId(Long appointmentId);
}
