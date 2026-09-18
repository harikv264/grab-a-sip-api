package com.grabasip.api.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Plan reference data. Mirrors PRODUCTS in the frontend lib/data.ts. */
@Service
public class PlanCatalog {

    public record Plan(String code, String name, int price) {}

    private final Map<String, Plan> plans = new LinkedHashMap<>();

    public PlanCatalog() {
        add("small", "Small Sip Bowl", 1699);
        add("large", "Large Sip Bowl", 2199);
        add("abc", "ABC Everyday", 1500);
        add("classic", "Classic Juice Plan", 1350);
    }

    private void add(String code, String name, int price) {
        plans.put(code, new Plan(code, name, price));
    }

    public List<Plan> all() {
        return new ArrayList<>(plans.values());
    }

    public Plan get(String code) {
        return code == null ? null : plans.get(code.trim().toLowerCase());
    }
}
