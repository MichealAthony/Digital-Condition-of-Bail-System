package com.hunts.bail.exception;

/** Thrown when a login attempt fails or a user lacks the required role. */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
