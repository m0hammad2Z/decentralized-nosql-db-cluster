package org.dbsim.broadcasting.exception;

public class ConnectionFailedException extends RuntimeException {
    public ConnectionFailedException(String message) {
        super(message);
    }

    public ConnectionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}