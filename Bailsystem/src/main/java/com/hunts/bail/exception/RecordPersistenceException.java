package com.hunts.bail.exception;

/** Thrown when a database save or retrieval operation fails. */
public class RecordPersistenceException extends RuntimeException {
    public RecordPersistenceException(String message) {
        super(message);
    }

    public RecordPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
