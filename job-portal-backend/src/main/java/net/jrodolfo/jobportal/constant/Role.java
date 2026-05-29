package net.jrodolfo.jobportal.constant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Enumeration of user roles within the system.
 * Used for authorization and defining access levels to various functionalities.
 */
@Getter
@Schema(description = "User role.")
public enum Role {
    /** Administrator with full access to manage the system, including users and job postings. */
    ADMIN("ADMIN"),
    /** Applicant who can search for jobs and submit applications. */
    APPLICANT("APPLICANT");

    private final String value;

    /**
     * Constructs a {@code Role} with the specified string value.
     *
     * @param value the string representation of the role
     */
    Role(String value) {
        this.value = value;
    }
}
