package com.tom.payment.routinemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthTokens {
    private LoginResponse loginResponse;
    private String refreshToken;
}
