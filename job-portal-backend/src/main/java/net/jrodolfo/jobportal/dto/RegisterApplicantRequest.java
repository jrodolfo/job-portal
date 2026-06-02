package net.jrodolfo.jobportal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for public applicant self-registration.
 *
 * @param name     display name and local login name
 * @param email    unique email address for the account
 * @param password plain-text password supplied by the user before encoding
 */
public record RegisterApplicantRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password
) {
}
