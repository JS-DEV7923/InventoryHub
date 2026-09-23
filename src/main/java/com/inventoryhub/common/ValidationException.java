package com.inventoryhub.common;

import org.springframework.http.HttpStatus;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, message);
    }
}
