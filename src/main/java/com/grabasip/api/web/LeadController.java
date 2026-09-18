package com.grabasip.api.web;

import com.grabasip.api.domain.Lead;
import com.grabasip.api.repo.LeadRepository;
import com.grabasip.api.web.dto.LeadRequest;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadRepository leads;
    private final com.grabasip.api.security.AdminGuard admin;

    public LeadController(LeadRepository leads, com.grabasip.api.security.AdminGuard admin) {
        this.leads = leads;
        this.admin = admin;
    }

    /** POST /api/leads — record a serviceability check as a lead. */
    @PostMapping
    public Map<String, Object> create(@RequestBody LeadRequest body) {
        Lead lead = new Lead();
        lead.setRawLocation(trim(body.rawLocation(), 200));
        lead.setPincode(validPincode(body.pincode()));
        lead.setMatchedArea(trim(body.matchedArea(), 120));
        lead.setServiceable(Boolean.TRUE.equals(body.serviceable()));
        lead.setPhone(validPhone(body.phone()));
        Lead saved = leads.save(lead);
        return Map.of("ok", true, "id", saved.getId().toString());
    }

    /** GET /api/leads?token=...&all=1 — admin JSON list (undelivered by default). */
    @GetMapping
    public List<Lead> list(@RequestParam String token,
                           @RequestParam(defaultValue = "0") String all) {
        requireAdmin(token);
        return "1".equals(all)
                ? leads.findAllByOrderByCreatedAtDesc()
                : leads.findByServiceableOrderByCreatedAtDesc(false);
    }

    /** GET /api/leads/export?token=...&all=1 — download .xlsx. */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam String token,
                                         @RequestParam(defaultValue = "0") String all) throws IOException {
        requireAdmin(token);
        boolean wantAll = "1".equals(all);
        List<Lead> rows = wantAll
                ? leads.findAllByOrderByCreatedAtDesc()
                : leads.findByServiceableOrderByCreatedAtDesc(false);

        byte[] xlsx = buildWorkbook(rows, wantAll);
        String filename = wantAll ? "grabasip-all-leads.xlsx" : "grabasip-undelivered-areas.xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    // ── helpers ──────────────────────────────────────────────────
    private void requireAdmin(String token) {
        admin.require(token);
    }

    private static String trim(String s, int max) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.substring(0, Math.min(t.length(), max));
    }

    private static String validPincode(String s) {
        return s != null && s.matches("\\d{6}") ? s : null;
    }

    private static String validPhone(String s) {
        if (s == null) return null;
        String digits = s.replaceAll("\\D", "");
        return digits.length() >= 8 ? s.trim().substring(0, Math.min(s.trim().length(), 20)) : null;
    }

    private static byte[] buildWorkbook(List<Lead> rows, boolean wantAll) throws IOException {
        String[] headers = {"created_at", "pincode", "matched_area", "raw_location", "is_serviceable", "phone"};
        DateTimeFormatter fmt = DateTimeFormatter.ISO_INSTANT;
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(wantAll ? "All leads" : "Undelivered areas");

            CellStyle headStyle = wb.createCellStyle();
            Font bold = wb.createFont();
            bold.setBold(true);
            headStyle.setFont(bold);

            Row head = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = head.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headStyle);
            }

            int r = 1;
            for (Lead lead : rows) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(lead.getCreatedAt() == null ? "" : fmt.format(lead.getCreatedAt()));
                row.createCell(1).setCellValue(nz(lead.getPincode()));
                row.createCell(2).setCellValue(nz(lead.getMatchedArea()));
                row.createCell(3).setCellValue(nz(lead.getRawLocation()));
                row.createCell(4).setCellValue(lead.isServiceable());
                row.createCell(5).setCellValue(nz(lead.getPhone()));
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            wb.write(out);
            return out.toByteArray();
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
