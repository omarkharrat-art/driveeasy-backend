package com.driveeasy.location.exception;

/** -> 400 Bad Request (a business rule is not respected) */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
