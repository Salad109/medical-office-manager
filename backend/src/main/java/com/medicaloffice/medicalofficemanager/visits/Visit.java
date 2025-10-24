package com.medicaloffice.medicalofficemanager.visits;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "visits")
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "appointment_id")
    private Long appointmentId;

    private String notes;

    @NotNull
    @Column(name = "completed_by_doctor_id")
    private Long completedByDoctorId;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
