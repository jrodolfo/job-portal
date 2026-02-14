package net.jrodolfo.jobportal.constant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Authentication provider.")
public enum AuthProvider {
    LOCAL("LOCAL"),
    GOOGLE("GOOGLE");

    private final String value;

    AuthProvider(String value) {
        this.value = value;
    }

}
