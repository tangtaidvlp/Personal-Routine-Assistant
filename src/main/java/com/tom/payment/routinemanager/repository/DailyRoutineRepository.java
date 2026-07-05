package com.tom.payment.routinemanager.repository;

import com.tom.payment.routinemanager.model.DailyRoutine;
import com.tom.payment.routinemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DailyRoutineRepository extends JpaRepository<DailyRoutine, UUID> {
    Optional<DailyRoutine> findByUserAndDate(User user, LocalDate date);
}
