package com.example.societyfest.controller;

import com.example.societyfest.dto.DashboardSummaryResponse;
import com.example.societyfest.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        log.info("starting stats api --->");
        return ResponseEntity.ok(statsService.getSummary());
    }
}

