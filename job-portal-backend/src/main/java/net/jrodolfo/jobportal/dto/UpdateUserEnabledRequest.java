package net.jrodolfo.jobportal.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserEnabledRequest(
        @NotNull(message = "Enabled is required")
        Boolean enabled
) {
}
