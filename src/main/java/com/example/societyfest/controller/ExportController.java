package com.example.societyfest.controller;

import com.example.societyfest.repository.DonationRepository;
import com.example.societyfest.repository.ExpenseRepository;
import com.example.societyfest.util.ExcelGenerator;
import com.example.societyfest.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final DonationRepository donationRepo;
    private final ExpenseRepository expenseRepo;

    @GetMapping("/donations")
    public ResponseEntity<InputStreamResource> exportDonations() {
        var excelStream = ExcelGenerator.donationsToExcel(donationRepo.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=donations.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(excelStream));
    }

    @GetMapping("/expenses")
    public ResponseEntity<InputStreamResource> exportExpenses() {
        var excelStream = ExcelGenerator.expensesToExcel(expenseRepo.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(excelStream));
    }

    @GetMapping("/summary")
    public ResponseEntity<InputStreamResource> exportFestivalSummary() {
        var donations = donationRepo.findAll();
        var expenses = expenseRepo.findAll();

        double previousYearCarryForward = 1200.00; // ← you can read from DB or config later

        ByteArrayInputStream pdf = PdfGenerator.generateFestivalSummary(donations, expenses, previousYearCarryForward);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=festival-summary.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }

}

