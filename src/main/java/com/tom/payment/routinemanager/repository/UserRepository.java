package com.tom.payment.routinemanager.repository;

import com.tom.payment.routinemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
