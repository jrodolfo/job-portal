package net.jrodolfo.jobportal.constant;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeration of possible statuses for a job application.
 * Represents the lifecycle stages of an application within the system.
 */
@Schema(description = "Application lifecycle status.")
public enum ApplicationStatus {
    /** The applicant has submitted the application. */
    APPLIED,
    /** The application is currently being reviewed by the recruiter or hiring manager. */
    REVIEWING,
    /** The application has been accepted for the position. */
    ACCEPTED,
    /** The application has been rejected. */
    REJECTED,
    /** The applicant has withdrawn the application. */
    WITHDRAWN
}
