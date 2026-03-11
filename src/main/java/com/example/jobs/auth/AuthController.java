package com.example.jobs.auth;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.jobs.auth.config.JwtProperties;
import com.example.jobs.auth.dtos.LoginRequest;
import com.example.jobs.auth.dtos.LoginResponse;
import com.example.jobs.auth.jwt.JwtUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

   String token = jwtUtils.generateToken(authentication);

        String cookieName = Optional.ofNullable(jwtProperties.getCookieName())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse("jwt");

        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        long expirySeconds = jwtProperties.getExpirySeconds();
        if (expirySeconds > 0) {
            cookie.setMaxAge((int) Math.min(expirySeconds, Integer.MAX_VALUE));
        }

        response.addCookie(cookie);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        String cookieName = Optional.ofNullable(jwtProperties.getCookieName())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse("jwt");

        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }
}