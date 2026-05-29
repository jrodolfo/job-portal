package net.jrodolfo.jobportal.exception;

import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

/**
 * Global exception handler for the job portal backend.
 * Provides consistent error responses across all REST controllers in the {@code net.jrodolfo.jobportal.controller} package.
 */
@RestControllerAdvice(basePackages = "net.jrodolfo.jobportal.controller")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link ResourceException}, typically thrown when a requested resource is not found.
     *
     * @param ex the exception instance
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with 404 Not Found status
     */
    @ExceptionHandler(ResourceException.class)
    public ResponseEntity<ErrorResponse> handleException(ResourceException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), System.currentTimeMillis()));
    }

    /**
     * Handles common client-side exceptions such as invalid arguments or malformed request body.
     *
     * @param ex the exception instance
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with 400 Bad Request status
     */
    @ExceptionHandler({
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleClientException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid request", System.currentTimeMillis()));
    }

    /**
     * Handles validation errors when {@code @Valid} parameters fail validation.
     * Extracts and joins all field error messages.
     *
     * @param ex the exception instance
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with 400 Bad Request status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : error.getField() + " is invalid")
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, System.currentTimeMillis()));
    }

    /**
     * Handles {@link ConstraintViolationException} which occurs during bean validation.
     *
     * @param ex the exception instance
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with 400 Bad Request status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, System.currentTimeMillis()));
    }

    /**
     * Handles database-related exceptions such as {@link DataAccessException} and {@link PersistenceException}.
     * Logs the full stack trace for debugging purposes.
     *
     * @param ex the exception instance
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with 500 Internal Server Error status
     */
    @ExceptionHandler({DataAccessException.class, PersistenceException.class})
    public ResponseEntity<ErrorResponse> handleDatabaseException(Exception ex) {
        log.error("Database error while processing request", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "A server error occurred while processing your request.",
                        System.currentTimeMillis()));
    }

    /**
     * Handles Spring's {@link ResponseStatusException}.
     *
     * @param ex the exception instance
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with the specific status and reason
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : "Request failed";
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), message, System.currentTimeMillis()));
    }

    /**
     * Fallback handler for any other unhandled {@link Exception}.
     * Logs the error and returns a generic 500 Internal Server Error response.
     *
     * @param ex the exception instance
     * @return a {@link ResponseEntity} containing an {@link ErrorResponse} with 500 Internal Server Error status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unexpected server error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Unexpected server error",
                        System.currentTimeMillis()));
    }

}
