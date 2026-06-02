package net.jrodolfo.jobportal.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for enabling or disabling an applicant account.
 *
 * @param enabled {@code true} to allow login and authenticated access,
 *                {@code false} to block login and active sessions
 */
public record UpdateUserEnabledRequest(
        @NotNull(message = "Enabled is required")
        Boolean enabled
) {
}
