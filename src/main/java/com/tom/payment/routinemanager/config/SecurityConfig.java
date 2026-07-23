package com.tom.payment.routinemanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, Environment environment) throws Exception {
        boolean allowAll = Arrays.stream(environment.getActiveProfiles())
            .anyMatch(profile -> profile.equals("local") || profile.equals("fast"));

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().access((authentication, context) ->
                        new org.springframework.security.authorization.AuthorizationDecision(allowAll || authentication.get().isAuthenticated()))
            );
        
        return http.build();
    }
}
