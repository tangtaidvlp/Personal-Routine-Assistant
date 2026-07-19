package com.tom.payment.routinemanager.repository;

import com.tom.payment.routinemanager.model.RoutineTaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoutineTaskTemplateRepository extends JpaRepository<RoutineTaskTemplate, UUID> {
}
