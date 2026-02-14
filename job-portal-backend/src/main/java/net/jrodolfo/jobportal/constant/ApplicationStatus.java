package net.jrodolfo.jobportal.constant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Application lifecycle status.")
public enum ApplicationStatus {
    APPLIED,
    REVIEWING,
    ACCEPTED,
    REJECTED,
    WITHDRAWN
}
