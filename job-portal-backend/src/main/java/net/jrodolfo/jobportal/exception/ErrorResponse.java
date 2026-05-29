package net.jrodolfo.jobportal.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a standard error response payload returned by the API.
 * This class is used to provide consistent error details to clients when an exception occurs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error payload.")
public class ErrorResponse {

    /**
     * The HTTP status code of the error.
     */
    @Schema(description = "HTTP status code.", example = "404")
    private int status;

    /**
     * The descriptive error message.
     */
    @Schema(description = "Error message.", example = "Resource not found")
    private String message;

    /**
     * The timestamp in epoch milliseconds when the error occurred.
     */
    @Schema(description = "Epoch millis when error happened.", example = "1770000000000")
    private long timestamp;
}
