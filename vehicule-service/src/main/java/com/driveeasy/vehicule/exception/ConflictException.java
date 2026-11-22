package com.driveeasy.vehicule.exception;

/** -> 409 Conflict (duplicate immatriculation, delete of a rented vehicle...) */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
