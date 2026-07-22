package com.tom.payment.routinemanager.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String username;
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DefaultRoutine> defaultRoutines;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Get weekend default routine for the user
    public Optional<DefaultRoutine> getWeekendDefaultRoutine() {
        return getDefaultRoutines().stream()
                .filter(routine -> routine.getRoutineType() == DefaultRoutine.RoutineTypeEnum.WEEKEND)
                .findFirst();
    }

}
