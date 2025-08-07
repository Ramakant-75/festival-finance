package com.example.societyfest.controller;

import com.example.societyfest.entity.AuditLog;
import com.example.societyfest.repository.AuditLogRepository;
import com.example.societyfest.util.JsonUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<AuditLog>> getLogs(
            @RequestParam Optional<String> username,
            @RequestParam Optional<String> action,
            @RequestParam Optional<String> entityType,
            @RequestParam Optional<Integer> year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> logs = auditLogRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            username.ifPresent(u -> {
                if (!u.trim().isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("username")), u.trim().toLowerCase() + "%"));
                }
            });

            action.ifPresent(a -> {
                if (!a.trim().isEmpty()) {
                    predicates.add(cb.equal(root.get("action"), a.trim()));
                }
            });

            entityType.ifPresent(e -> {
                if (!e.trim().isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("entityType")), e.trim().toLowerCase() + "%"));
                }
            });

            year.ifPresent(y -> {
                LocalDateTime start = LocalDate.of(y, 1, 1).atStartOfDay();
                LocalDateTime end = LocalDate.of(y, 12, 31).atTime(LocalTime.MAX);
                predicates.add(cb.between(root.get("timestamp"), start, end));
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
        return ResponseEntity.ok(logs);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAllAsExcel() {
        List<AuditLog> logs = auditLogRepository.findAll(Sort.by("timestamp").descending());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Audit Logs");
            Row header = sheet.createRow(0);
            String[] columns = {"ID", "Username", "Action", "Entity Type", "Entity ID", "Before State", "After State", "IP Address", "User Agent", "Timestamp"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (AuditLog log : logs) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(log.getId());
                row.createCell(1).setCellValue(log.getUsername());
                row.createCell(2).setCellValue(log.getAction());
                row.createCell(3).setCellValue(log.getEntityType());
                row.createCell(4).setCellValue(log.getEntityId());

                row.createCell(5).setCellValue(JsonUtil.toJson(log.getBeforeState()));
                row.createCell(6).setCellValue(JsonUtil.toJson(log.getAfterState()));

                row.createCell(7).setCellValue(log.getIpAddress());
                row.createCell(8).setCellValue(log.getUserAgent());
                row.createCell(9).setCellValue(log.getTimestamp().toString());
            }

            workbook.write(out);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-logs.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(out.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
