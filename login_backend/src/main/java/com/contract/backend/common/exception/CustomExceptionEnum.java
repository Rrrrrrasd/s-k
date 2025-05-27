package com.contract.backend.common.exception;

import org.springframework.http.HttpStatus;

public enum CustomExceptionEnum {
    EMAIL_ALREADY_EXISTS("이미 존재하는 이메일입니다.", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_FOUND("이메일이 존재하지 않습니다.", HttpStatus.UNAUTHORIZED),
    PASSWORD_MISMATCH("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    WEBAUTHN_REGISTRATION_FAILED("WebAuthn 등록에 실패했습니다.", HttpStatus.BAD_REQUEST),
    WEBAUTHN_AUTHENTICATION_FAILED("WebAuthn 인증에 실패했습니다.", HttpStatus.BAD_REQUEST),
    CREDENTIAL_ALREADY_EXISTS("이미 등록된 WebAuthn 자격 정보입니다.", HttpStatus.CONFLICT),
    CREDENTIAL_NOT_FOUND("등록된 인증 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED("요청 권한이 없습니다.", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus status;

    CustomExceptionEnum(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
