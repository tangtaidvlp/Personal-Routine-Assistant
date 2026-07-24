package com.tom.payment.routinemanager.service;

import com.tom.payment.routinemanager.dto.*;
import com.tom.payment.routinemanager.model.RefreshToken;
import com.tom.payment.routinemanager.model.User;
import com.tom.payment.routinemanager.repository.RefreshTokenRepository;
import com.tom.payment.routinemanager.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Key jwtSecretKey;

    @Value("${jwt.access.expiration:900}") // 15 minutes in seconds
    private int accessTokenExpiration;

    @Value("${jwt.refresh.expiration:604800}") // 7 days in seconds
    private int refreshTokenExpiration;

    public AuthenticationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        // Generate a secure key for HS256
        this.jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                "User registered successfully."
        );
    }

    @Transactional
    public AuthTokens login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate access token
        String accessToken = generateAccessToken(user);

        // Generate and save refresh token
        String refreshTokenValue = generateRefreshToken(user);
        saveRefreshToken(user.getId(), refreshTokenValue);

        LoginResponse loginResponse = new LoginResponse(
                accessToken,
                accessTokenExpiration,
                new LoginResponse.UserInfo(user.getId().toString(), user.getEmail())
        );

        return new AuthTokens(loginResponse, refreshTokenValue);
    }

    public RefreshResponse refresh(String refreshTokenValue) {
        // Find refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByTokenVal(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Check if token is revoked
        if (refreshToken.getIsRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        // Check if token is expired
        if (refreshToken.getExpiryDate().isBefore(LocalTime.now())) {
            throw new RuntimeException("Refresh token has expired");
        }

        // Find user
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate new access token
        String accessToken = generateAccessToken(user);

        return new RefreshResponse(accessToken, accessTokenExpiration);
    }

    @Transactional
    public void logout(UUID userId) {
        // Revoke all refresh tokens for the user
        refreshTokenRepository.deleteByUserId(userId);
    }

    private String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000L))
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration * 1000L))
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private void saveRefreshToken(UUID userId, String tokenValue) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setTokenVal(tokenValue);
        refreshToken.setExpiryDate(LocalTime.now().plusSeconds(refreshTokenExpiration));
        refreshToken.setIsRevoked(false);
        refreshTokenRepository.save(refreshToken);
    }

}
