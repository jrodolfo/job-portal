package net.jrodolfo.jobportal.exception;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error payload.")
public class ErrorResponse {
    
    @Schema(description = "HTTP status code.", example = "404")
    private int status;
    @Schema(description = "Error message.", example = "Resource not found")
    private String message;
    @Schema(description = "Epoch millis when error happened.", example = "1770000000000")
    private long timestamp;
}
