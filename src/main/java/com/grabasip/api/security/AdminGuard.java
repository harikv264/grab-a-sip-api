package com.grabasip.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Shared admin-token check for the admin endpoints (temporary until per-user auth). */
@Component
public class AdminGuard {

    private final String adminToken;

    public AdminGuard(@Value("${app.admin-token}") String adminToken) {
        this.adminToken = adminToken;
    }

    public void require(String token) {
        if (adminToken == null || adminToken.isBlank() || !adminToken.equals(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin token");
        }
    }
}
