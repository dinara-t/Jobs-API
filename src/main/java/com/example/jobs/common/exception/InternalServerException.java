package com.example.jobs.common.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends HTTPException {
    public InternalServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}