package com.example.societyfest.controller;

import com.example.societyfest.dto.DashboardSummaryResponse;
import com.example.societyfest.dto.DonationStatsResponse;
import com.example.societyfest.service.DonationStatsService;
import com.example.societyfest.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final DonationStatsService donationStatsService;

//    @PreAuthorize("hasRole('ADMIN')")
    @CrossOrigin
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(@RequestParam int year) {
        log.info("starting stats api --->");
        return ResponseEntity.ok(statsService.getSummary(year));
    }

    @GetMapping("/donations")
    public ResponseEntity<DonationStatsResponse> getDonationStats(@RequestParam(value = "year", required = false) Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(donationStatsService.getStats(targetYear));
    }
}

