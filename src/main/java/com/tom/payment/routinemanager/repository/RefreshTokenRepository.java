package com.tom.payment.routinemanager.repository;

import com.tom.payment.routinemanager.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenVal(String tokenVal);
    void deleteByUserId(UUID userId);
}
