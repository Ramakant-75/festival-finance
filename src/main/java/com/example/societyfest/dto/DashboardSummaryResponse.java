package com.example.societyfest.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private double totalDonations;
    private double totalExpenses;
    private double balance;

    private List<DailyStat> dailyDonations;
    private List<DailyStat> dailyExpenses;

    private Map<String, Double> paymentModeBreakdown;
    private Map<String, Double> expenseByCategory;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyStat {
        private LocalDate date;  // e.g. "2025-07-20"
        private double amount;
    }
}
