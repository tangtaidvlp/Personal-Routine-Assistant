package com.tom.payment.routinemanager.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_val", nullable = false, unique = true, length = 512)
    private String tokenVal;

    @Column(name = "expiry_date", nullable = false)
    private LocalTime expiryDate;

    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false;
}
