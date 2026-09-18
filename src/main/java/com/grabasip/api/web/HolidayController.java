package com.grabasip.api.web;

import com.grabasip.api.domain.Holiday;
import com.grabasip.api.repo.HolidayRepository;
import com.grabasip.api.security.AdminGuard;
import com.grabasip.api.web.dto.HolidayRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    private final HolidayRepository repo;
    private final AdminGuard admin;

    public HolidayController(HolidayRepository repo, AdminGuard admin) {
        this.repo = repo;
        this.admin = admin;
    }

    @GetMapping
    public List<Holiday> list(@RequestParam String token) {
        admin.require(token);
        return repo.findAllByOrderByDateDesc();
    }

    @PostMapping
    public ResponseEntity<Holiday> create(@RequestParam String token,
                                          @RequestBody HolidayRequest body) {
        admin.require(token);
        if (body.date() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
        if (repo.existsByDate(body.date()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That date is already a holiday");
        Holiday h = new Holiday();
        h.setDate(body.date());
        h.setName(body.name() == null ? null : body.name().trim());
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(h));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@RequestParam String token, @PathVariable UUID id) {
        admin.require(token);
        repo.deleteById(id);
        return Map.of("ok", true);
    }
}
