package net.jrodolfo.jobportal.constant;

import lombok.Getter;

@Getter
public enum AuthProvider {
    LOCAL("LOCAL"),
    GOOGLE("GOOGLE");

    private final String value;

    AuthProvider(String value) {
        this.value = value;
    }

}
