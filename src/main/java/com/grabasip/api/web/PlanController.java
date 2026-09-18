package com.grabasip.api.web;

import com.grabasip.api.service.PlanCatalog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanCatalog catalog;

    public PlanController(PlanCatalog catalog) {
        this.catalog = catalog;
    }

    /** GET /api/plans — the plan reference list. */
    @GetMapping
    public List<PlanCatalog.Plan> all() {
        return catalog.all();
    }
}
