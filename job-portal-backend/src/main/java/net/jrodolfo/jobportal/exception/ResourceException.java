package net.jrodolfo.jobportal.exception;

/**
 * Custom exception indicating that a requested resource could not be found or processed.
 * This exception is typically handled by the {@link GlobalExceptionHandler} to return a 404 Not Found response.
 */
public class ResourceException extends RuntimeException {

    /**
     * Constructs a new ResourceException with the specified detail message.
     *
     * @param message the detail message
     */
    public ResourceException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceException with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public ResourceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ResourceException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
