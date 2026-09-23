package com.inventoryhub.common;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(ApiErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, message);
    }
}
