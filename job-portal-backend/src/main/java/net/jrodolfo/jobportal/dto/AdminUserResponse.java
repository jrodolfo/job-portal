package net.jrodolfo.jobportal.dto;

import net.jrodolfo.jobportal.constant.AuthProvider;
import net.jrodolfo.jobportal.constant.Role;
import net.jrodolfo.jobportal.model.User;

import java.time.LocalDateTime;

/**
 * Admin-safe user payload.
 * <p>
 * This DTO exposes identity, role, provider, enabled state, and audit
 * timestamps while intentionally excluding password hashes.
 */
public record AdminUserResponse(
        Long id,
        String name,
        String email,
        AuthProvider authProvider,
        Role role,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Builds the admin-safe response from a managed user entity.
     *
     * @param user the user entity to project
     * @return a response DTO without password data
     */
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAuthProvider(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
