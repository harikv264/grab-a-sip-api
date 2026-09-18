package com.grabasip.api.security;

import com.grabasip.api.domain.AppUser;
import com.grabasip.api.repo.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Resolves the caller's app_user (role + linked record) from their Supabase JWT. */
@Component
public class CurrentUserService {

    private final AppUserRepository appUsers;

    public CurrentUserService(AppUserRepository appUsers) {
        this.appUsers = appUsers;
    }

    /** The auth user id (JWT subject), if a valid Bearer token was presented. */
    public Optional<UUID> uid() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            try {
                return Optional.of(UUID.fromString(jwt.getToken().getSubject()));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /** The app_user row for the caller, if signed in and provisioned. */
    public Optional<AppUser> current() {
        return uid().flatMap(appUsers::findById).filter(AppUser::isActive);
    }

    public boolean hasRole(String role) {
        return current().map(u -> role.equalsIgnoreCase(u.getRole())).orElse(false);
    }

    public boolean isAdmin() {
        return hasRole("admin");
    }
}
