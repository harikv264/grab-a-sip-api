package com.grabasip.api.web;

import com.grabasip.api.security.CurrentUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/** Who am I — the caller's role & linked record, resolved from their JWT. */
@RestController
@RequestMapping("/api")
public class MeController {

    private final CurrentUserService currentUser;

    public MeController(CurrentUserService currentUser) {
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        Map<String, Object> out = new HashMap<>();
        boolean authenticated = currentUser.uid().isPresent();
        out.put("authenticated", authenticated);

        var user = currentUser.current().orElse(null);
        if (user != null) {
            out.put("provisioned", true);
            out.put("role", user.getRole());
            out.put("customerId", user.getCustomerId());
            out.put("deliveryPersonId", user.getDeliveryPersonId());
            out.put("email", user.getEmail());
        } else {
            // Valid token but no app_users row yet (or no token at all).
            out.put("provisioned", false);
            out.put("role", null);
        }
        return out;
    }
}
