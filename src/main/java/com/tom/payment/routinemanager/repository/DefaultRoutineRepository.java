package com.tom.payment.routinemanager.repository;

import com.tom.payment.routinemanager.model.DailyRoutine;
import com.tom.payment.routinemanager.model.DefaultRoutine;
import com.tom.payment.routinemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface DefaultRoutineRepository extends JpaRepository<DefaultRoutine, UUID> {
    Optional<DefaultRoutine> findByUser(User user);

    Optional<DefaultRoutine> findByUserAndType(User user, String type);
}
