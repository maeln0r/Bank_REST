package com.example.bankcards.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DomainValidationException extends RuntimeException {
    private final String code;
    private final String field;
    private final Object[] args;
    private final HttpStatus status;

    public DomainValidationException(String code) {
        this(code, null, null, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public DomainValidationException(String code, String field) {
        this(code, field, null, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public DomainValidationException(String code, String field, Object[] args) {
        this(code, field, args, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public DomainValidationException(String code, String field, Object[] args, HttpStatus status) {
        super(code);
        this.code = code;
        this.field = field;
        this.args = args;
        this.status = status == null ? HttpStatus.UNPROCESSABLE_ENTITY : status;
    }
}