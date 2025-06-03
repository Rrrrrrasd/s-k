package com.contract.backend.common.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
    private final HttpStatus status;

    public CustomException(CustomExceptionEnum e) {
        super(e.getMessage());
        this.status = e.getStatus();
    }

    public HttpStatus getStatus() {
        return status;
    }
}
