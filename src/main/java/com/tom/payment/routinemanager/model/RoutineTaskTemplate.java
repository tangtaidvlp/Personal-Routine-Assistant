package com.tom.payment.routinemanager.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RoutineTaskTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;
    private LocalTime startTime;
    private int durationMinutes;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "default_routine_id")
    private DefaultRoutine defaultRoutine;
}
