package org.dbsim.broadcasting.exception;

public class BroadcastException extends RuntimeException {
    public BroadcastException(String message) {
        super(message);
    }

    public BroadcastException(String message, Throwable cause) {
        super(message, cause);
    }
}
