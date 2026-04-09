package com.hunts.bail.exception;

/**
 * Thrown when form input fails business-rule validation.
 */
public class ValidationException extends RuntimeException {

    private final java.util.List<String> errors;

    public ValidationException(java.util.List<String> errors) {
        super("Validation failed: " + String.join("; ", errors));
        this.errors = java.util.Collections.unmodifiableList(errors);
    }

    public ValidationException(String singleError) {
        this(java.util.List.of(singleError));
    }

    public java.util.List<String> getErrors() {
        return errors;
    }
}
