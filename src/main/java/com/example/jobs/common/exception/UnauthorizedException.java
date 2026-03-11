package com.example.jobs.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends HTTPException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}