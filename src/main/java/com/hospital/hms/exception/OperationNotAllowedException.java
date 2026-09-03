package com.hospital.hms.exception;

/**
 * Thrown when an operation is well-formed and the user is authorized to
 * attempt it, but it would violate a business/data-integrity rule — e.g.
 * deleting a Doctor profile that still has appointments or medical records
 * attached (which would silently cascade-delete clinical history).
 */
public class OperationNotAllowedException extends RuntimeException {

    public OperationNotAllowedException(String message) {
        super(message);
    }
}
