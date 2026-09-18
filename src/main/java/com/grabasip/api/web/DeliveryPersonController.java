package com.grabasip.api.web;

import com.grabasip.api.domain.DeliveryPerson;
import com.grabasip.api.repo.DeliveryPersonRepository;
import com.grabasip.api.security.AdminGuard;
import com.grabasip.api.web.dto.DeliveryPersonRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/delivery-persons")
public class DeliveryPersonController {

    private final DeliveryPersonRepository repo;
    private final AdminGuard admin;

    public DeliveryPersonController(DeliveryPersonRepository repo, AdminGuard admin) {
        this.repo = repo;
        this.admin = admin;
    }

    @GetMapping
    public List<DeliveryPerson> list(@RequestParam String token) {
        admin.require(token);
        return repo.findAllByOrderByNameAsc();
    }

    @PostMapping
    public ResponseEntity<DeliveryPerson> create(@RequestParam String token,
                                                 @RequestBody DeliveryPersonRequest body) {
        admin.require(token);
        if (body.name() == null || body.name().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        DeliveryPerson p = new DeliveryPerson();
        p.setName(body.name().trim());
        p.setPhone(trim(body.phone()));
        p.setArea(trim(body.area()));
        p.setActive(body.active() == null || body.active());
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(p));
    }

    @PutMapping("/{id}")
    public DeliveryPerson update(@RequestParam String token, @PathVariable UUID id,
                                 @RequestBody DeliveryPersonRequest body) {
        admin.require(token);
        DeliveryPerson p = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));
        if (body.name() != null && !body.name().isBlank()) p.setName(body.name().trim());
        if (body.phone() != null) p.setPhone(trim(body.phone()));
        if (body.area() != null) p.setArea(trim(body.area()));
        if (body.active() != null) p.setActive(body.active());
        return repo.save(p);
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
