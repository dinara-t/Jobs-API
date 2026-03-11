package com.example.jobs.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends HTTPException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}