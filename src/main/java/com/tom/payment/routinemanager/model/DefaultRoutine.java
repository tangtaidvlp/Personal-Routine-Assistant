package com.tom.payment.routinemanager.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DefaultRoutine {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "defaultRoutine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutineTaskTemplate> tasks = new ArrayList<>();
}
