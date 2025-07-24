package com.example.societyfest.service;

import com.example.societyfest.dto.DashboardSummaryResponse;
import com.example.societyfest.entity.Donation;
import com.example.societyfest.entity.Expense;
import com.example.societyfest.repository.DonationRepository;
import com.example.societyfest.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final DonationRepository donationRepo;
    private final ExpenseRepository expenseRepo;

    public DashboardSummaryResponse getSummary(int year) {
        List<Donation> donations = donationRepo.findAllByYear(year);
        List<Expense> expenses = expenseRepo.findAllByYear(year);

        double totalDonations = donations.stream().mapToDouble(Donation::getAmount).sum();
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double balance = totalDonations - totalExpenses;


        List<DashboardSummaryResponse.DailyStat> dailyDonations = donations.stream()
                .collect(Collectors.groupingBy(Donation::getDate, Collectors.summingDouble(Donation::getAmount)))
                .entrySet().stream()
                .map(e -> new DashboardSummaryResponse.DailyStat(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DashboardSummaryResponse.DailyStat::getDate))
                .collect(Collectors.toList());

        List<DashboardSummaryResponse.DailyStat> dailyExpenses = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getDate, Collectors.summingDouble(Expense::getAmount)))
                .entrySet().stream()
                .map(e -> new DashboardSummaryResponse.DailyStat(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DashboardSummaryResponse.DailyStat::getDate))
                .collect(Collectors.toList());

        Map<String, Double> paymentModeBreakdown = donations.stream()
                .collect(Collectors.groupingBy(d -> d.getPaymentMode().name(), Collectors.summingDouble(Donation::getAmount)));

        Map<String, Double> expenseByCategory = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)));

        return DashboardSummaryResponse.builder()
                .totalDonations(totalDonations)
                .totalExpenses(totalExpenses)
                .balance(balance)
                .dailyDonations(dailyDonations)
                .dailyExpenses(dailyExpenses)
                .paymentModeBreakdown(paymentModeBreakdown)
                .expenseByCategory(expenseByCategory)
                .build();
    }
}
