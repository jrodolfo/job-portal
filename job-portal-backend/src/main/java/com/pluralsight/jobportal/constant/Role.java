package com.pluralsight.jobportal.constant;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("ADMIN"),
    APPLICANT("APPLICANT");

    private final String value;

    Role(String value) {
        this.value = value;
    }
}
