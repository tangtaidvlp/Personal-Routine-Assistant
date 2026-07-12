package com.tom.payment.routinemanager.api;

import com.tom.payment.routinemanager.dto.*;
import com.tom.payment.routinemanager.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final int REFRESH_TOKEN_MAX_AGE = 604800; // 7 days in seconds

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        RegisterResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthTokens authTokens = authenticationService.login(request);

        // Set refresh token as HttpOnly cookie
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE, authTokens.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // Enable in production with HTTPS
        refreshTokenCookie.setPath("/api/v1/auth/refresh");
        refreshTokenCookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
        refreshTokenCookie.setAttribute("SameSite", "Strict");

        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(authTokens.getLoginResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(HttpServletRequest request) {
        // Extract refresh token from cookie
        String refreshToken = Arrays.stream(request.getCookies() != null ? request.getCookies() : new Cookie[0])
                .filter(cookie -> REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        
        RefreshResponse refreshResponse = authenticationService.refresh(refreshToken);
        return ResponseEntity.ok(refreshResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        
        // Clear the refresh token cookie
        Cookie clearCookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        clearCookie.setHttpOnly(true);
        clearCookie.setSecure(true);
        clearCookie.setPath("/api/v1/auth/refresh");
        clearCookie.setMaxAge(0);
        clearCookie.setAttribute("SameSite", "Strict");
        
        response.addCookie(clearCookie);
        
        // Optionally revoke tokens in database
        // You'd need to extract user ID from the current session/token
        // authenticationService.logout(userId);
        
        return ResponseEntity.ok().build();
    }

}
