package com.grabasip.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Admin access check. Passes when EITHER the legacy admin token matches OR
 * the caller presents a valid Supabase JWT whose app_user role is admin.
 * This lets the token and per-user JWT auth run side by side during migration.
 */
@Component
public class AdminGuard {

    private final String adminToken;
    private final CurrentUserService currentUser;

    public AdminGuard(@Value("${app.admin-token}") String adminToken,
                      CurrentUserService currentUser) {
        this.adminToken = adminToken;
        this.currentUser = currentUser;
    }

    public void require(String token) {
        if (adminToken != null && !adminToken.isBlank() && adminToken.equals(token)) {
            return;
        }
        if (currentUser.isAdmin()) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin access required");
    }
}
