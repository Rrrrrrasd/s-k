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
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND), // 기존 USER_NOT_FOUND를 일반화하거나 CONTRACT_NOT_FOUND 추가
    CONTRACT_NOT_FOUND("계약을 찾을 수 없습니다.", HttpStatus.NOT_FOUND), // 추가
    CONTRACT_NOT_MODIFIABLE("계약을 수정할 수 없는 상태입니다.", HttpStatus.BAD_REQUEST),
    VERSION_NOT_FOUND("계약 버전을 찾을 수 없습니다.", HttpStatus.NOT_FOUND), // 추가 (필요에 따라)
    CANNOT_SIGN_CONTRACT("계약에 서명할 수 없는 상태입니다.", HttpStatus.BAD_REQUEST), // 추가
    VERSION_NOT_PENDING_SIGNATURE("서명 대기 중인 버전이 아닙니다.", HttpStatus.BAD_REQUEST), // 추가
    ALREADY_SIGNED("이미 해당 버전에 서명했습니다.", HttpStatus.CONFLICT),// 추가; // 추가
    PARTICIPANT_ALREADY_EXISTS("이미 계약에 참여하고 있는 사용자입니다.", HttpStatus.CONFLICT), // 필요시 추가
    CANNOT_ADD_PARTICIPANT("계약에 참여자를 추가할 수 없는 상태입니다.", HttpStatus.BAD_REQUEST),
    CANNOT_ADD_CREATOR_AS_DIFFERENT_ROLE("자기 자신을 추가할 수 없습니다.", HttpStatus.BAD_REQUEST);


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