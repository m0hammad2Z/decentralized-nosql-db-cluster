package org.dbsim.node.exception;

import org.dbsim.node.enums.DBExceptionErrorType;

public class DBOperationException extends RuntimeException {
    private final DBExceptionErrorType errorType;

    public DBOperationException(String message, DBExceptionErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    // Get the error type
    public DBExceptionErrorType getErrorType() {
        return this.errorType;
    }
}
