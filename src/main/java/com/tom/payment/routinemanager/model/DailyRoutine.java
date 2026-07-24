package com.tom.payment.routinemanager.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DailyRoutine {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalTime date;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonManagedReference
    @OneToMany(mappedBy = "dailyRoutine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyTask> tasks = new ArrayList<>();
}
