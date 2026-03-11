package com.example.jobs.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends HTTPException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}