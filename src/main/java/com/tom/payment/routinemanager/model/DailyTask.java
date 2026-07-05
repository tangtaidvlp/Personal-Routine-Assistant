package com.tom.payment.routinemanager.model;

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
public class DailyTask {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;
    private LocalTime startTime;
    private int durationMinutes;
    private boolean completed;

    @ManyToOne
    @JoinColumn(name = "daily_routine_id")
    private DailyRoutine dailyRoutine;
}
