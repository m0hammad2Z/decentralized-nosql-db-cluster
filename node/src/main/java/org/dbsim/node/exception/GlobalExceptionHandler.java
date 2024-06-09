package org.dbsim.node.exception;

import com.fasterxml.jackson.core.JsonParseException;
import org.dbsim.node.model.message.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {
        return new ResponseEntity<>(new ApiResponse<>(false, "Internal server error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = DBOperationException.class)
    public ResponseEntity<ApiResponse<?>> handleDBOperationException(DBOperationException e) {
        return switch (e.getErrorType()) {
            case NOT_FOUND ->
                    new ResponseEntity<>(new ApiResponse<>(false, "Resource not found", e.getMessage()), HttpStatus.NOT_FOUND);
            case RESOURCE_ALREADY_EXISTS ->
                    new ResponseEntity<>(new ApiResponse<>(false, "Resource already exists", e.getMessage()), HttpStatus.CONFLICT);
            case INVALID_INPUT ->
                    new ResponseEntity<>(new ApiResponse<>(false, "Invalid input", e.getMessage()), HttpStatus.BAD_REQUEST);
            default ->
                    new ResponseEntity<>(new ApiResponse<>(false, "Internal server error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return new ResponseEntity<>(new ApiResponse<>(false, "Validation failed", List.of(Objects.requireNonNull(e.getMessage()))), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = JsonParseException.class)
    public ResponseEntity<ApiResponse<?>> handleJsonParseException(JsonParseException e) {
        return new ResponseEntity<>(new ApiResponse<>(false, "Invalid JSON", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(ValidationException e) {
        return new ResponseEntity<>(new ApiResponse<>(false, "Validation failed", List.of(e.getMessage())), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(new ApiResponse<>(false, "Invalid argument", List.of(e.getMessage())), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalStateException(IllegalStateException e) {
        return new ResponseEntity<>(new ApiResponse<>(false, "Illegal state", List.of(e.getMessage())), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidTokenException(InvalidTokenException e) {
        return new ResponseEntity<>(new ApiResponse<>(false, "Invalid token", List.of(e.getMessage())), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return new ResponseEntity<>(new ApiResponse<>(false, "Invalid request body", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

}