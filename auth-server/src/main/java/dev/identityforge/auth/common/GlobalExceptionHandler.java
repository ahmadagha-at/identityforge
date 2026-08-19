package dev.identityforge.auth.common;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "dev.identityforge.auth.web")
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> conflict(ConflictException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "conflict", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "not_found", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<ApiError> invalid(InvalidRequestException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "invalid_request", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception,
                                        HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return response(HttpStatus.BAD_REQUEST, "validation_failed",
                "The request contains invalid fields.", request, errors);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled request failure", exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error",
                "The request could not be completed.", request, Map.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message,
                                              HttpServletRequest request,
                                              Map<String, String> fieldErrors) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(), status.value(), code, message, request.getRequestURI(), fieldErrors));
    }
}
