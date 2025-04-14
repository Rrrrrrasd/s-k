package com.contract.backend.common.exception;

public class RegistrationNotInProgressException extends RuntimeException {
    public RegistrationNotInProgressException(String message) {
        super(message);
    }
}
