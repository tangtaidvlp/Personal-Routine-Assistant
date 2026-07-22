package com.tom.payment.routinemanager.model;

import java.time.LocalTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public LocalTime getEndTime() {
        return startTime.plusMinutes(durationMinutes);
    }
}
