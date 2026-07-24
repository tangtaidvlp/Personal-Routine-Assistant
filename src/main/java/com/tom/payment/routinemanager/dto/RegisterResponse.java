package com.tom.payment.routinemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RegisterResponse {
    private UUID userId;
    private String email;
    private String userName;
    private String message;
}
