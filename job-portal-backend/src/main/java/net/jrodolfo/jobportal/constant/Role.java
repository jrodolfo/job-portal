package net.jrodolfo.jobportal.constant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "User role.")
public enum Role {
    ADMIN("ADMIN"),
    APPLICANT("APPLICANT");

    private final String value;

    Role(String value) {
        this.value = value;
    }
}
