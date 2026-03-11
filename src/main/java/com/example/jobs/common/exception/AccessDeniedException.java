package com.example.jobs.common.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends HTTPException {
    public AccessDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}