package com.driveeasy.location.exception;

/** -> 503 Service Unavailable : another microservice could not be reached. */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
