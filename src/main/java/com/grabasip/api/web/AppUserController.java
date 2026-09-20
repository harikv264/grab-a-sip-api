package com.grabasip.api.web;

import com.grabasip.api.domain.AppUser;
import com.grabasip.api.repo.AppUserRepository;
import com.grabasip.api.security.AdminGuard;
import com.grabasip.api.web.dto.AppUserLinkRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

/** Admin provisioning: link a Supabase auth user to a role + owned record. */
@RestController
@RequestMapping("/api/admin/app-users")
public class AppUserController {

    private static final Set<String> ROLES = Set.of("admin", "rider", "customer");

    private final AppUserRepository appUsers;
    private final AdminGuard admin;

    public AppUserController(AppUserRepository appUsers, AdminGuard admin) {
        this.appUsers = appUsers;
        this.admin = admin;
    }

    /** POST /api/admin/app-users — create/update the role mapping for an auth user. */
    @PostMapping
    public AppUser link(@RequestParam String token, @RequestBody AppUserLinkRequest body) {
        admin.require(token);
        if (body.id() == null) throw bad("auth user id is required");
        String role = body.role() == null ? "" : body.role().trim().toLowerCase();
        if (!ROLES.contains(role)) throw bad("role must be admin, rider or customer");
        if ("rider".equals(role) && body.deliveryPersonId() == null)
            throw bad("deliveryPersonId is required for a rider");
        if ("customer".equals(role) && body.customerId() == null)
            throw bad("customerId is required for a customer");

        AppUser u = appUsers.findById(body.id()).orElseGet(AppUser::new);
        u.setId(body.id());
        u.setRole(role);
        u.setCustomerId("customer".equals(role) ? body.customerId() : null);
        u.setDeliveryPersonId("rider".equals(role) ? body.deliveryPersonId() : null);
        u.setEmail(body.email());
        u.setPhone(body.phone());
        u.setActive(true);
        return appUsers.save(u);
    }

    private static ResponseStatusException bad(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}
