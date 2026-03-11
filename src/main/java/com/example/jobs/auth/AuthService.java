package com.example.jobs.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.jobs.auth.jwt.JwtUtils;
import com.example.jobs.common.exception.UnauthorizedException;

import io.jsonwebtoken.lang.Strings;

@Service
public class AuthService {

    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    private final String authUsername;
    private final String authPassword;

    public AuthService(
            JwtUtils jwtUtils,
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.username}") String authUsername,
            @Value("${app.auth.password}") String authPassword
    ) {
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.authUsername = authUsername;
        this.authPassword = authPassword;
    }

    public String login(String username, String password) {
        if (!Strings.hasText(username) || !Strings.hasText(password)) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (!username.equals(authUsername) || !passwordMatches(password)) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return jwtUtils.generateTokenFromSubject(username);
    }

    private boolean passwordMatches(String rawPassword) {
        if (Strings.hasText(authPassword) && authPassword.startsWith("$2a$") || authPassword.startsWith("$2b$") || authPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, authPassword);
        }
        return rawPassword.equals(authPassword);
    }
}