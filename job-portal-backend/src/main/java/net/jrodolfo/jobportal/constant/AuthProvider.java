package net.jrodolfo.jobportal.constant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Enumeration of supported authentication providers.
 * Defines the source of the user's authentication credentials.
 */
@Getter
@Schema(description = "Authentication provider.")
public enum AuthProvider {
    /** Local authentication using email and password stored in the system database. */
    LOCAL("LOCAL"),
    /** External authentication using Google OAuth2. */
    GOOGLE("GOOGLE");

    private final String value;

    /**
     * Constructs an {@code AuthProvider} with the specified string value.
     *
     * @param value the string representation of the provider
     */
    AuthProvider(String value) {
        this.value = value;
    }

}
