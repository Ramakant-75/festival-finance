package com.example.societyfest.controller;

import com.example.societyfest.dto.ExpenseResponse;
import com.example.societyfest.entity.Donation;
import com.example.societyfest.entity.Expense;
import com.example.societyfest.enums.PaymentMode;
import com.example.societyfest.repository.DonationRepository;
import com.example.societyfest.repository.ExpenseRepository;
import com.example.societyfest.service.ExpenseService;
import com.example.societyfest.util.ExcelGenerator;
import com.example.societyfest.util.PdfGenerator;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final DonationRepository donationRepo;
    private final ExpenseRepository expenseRepo;
    private final ExpenseService expenseService;

//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/donations")
    public ResponseEntity<InputStreamResource> exportDonations(
            @RequestParam int year,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) PaymentMode paymentMode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<Donation> donations = donationRepo.findAllByFilters(year, building, paymentMode, date);
        ByteArrayInputStream excelStream = (ByteArrayInputStream) ExcelGenerator.donationsToExcel(donations);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=donations.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(excelStream));
    }


//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/expenses")
    public ResponseEntity<InputStreamResource> exportExpenses() {
        var excelStream = ExcelGenerator.expensesToExcel(expenseRepo.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(excelStream));
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/festival-report")
    public ResponseEntity<InputStreamResource> generateFestivalReport(@RequestParam int year) {
        List<Expense> expenses = expenseRepo.findAllByYear(year); // Custom repo method
        double totalDonations = donationRepo.sumAmountByYear(year);
        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double balance = totalDonations - totalExpenses;

        ByteArrayInputStream pdf = PdfGenerator.generateFestivalReport(totalDonations, totalExpenses, balance, expenses,year);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=festival-report-" + year + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(new InputStreamResource(pdf));
    }

    @GetMapping("/export-detailed-expenses")
    public void exportDetailedExpenses(HttpServletResponse response) throws IOException {
        log.info("detailed export");
        List<ExpenseResponse> expenses = expenseService.getAllDetailedExpenseResponses();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=detailed_expenses.xlsx");

        InputStream excelStream = ExcelGenerator.detailedExpensesToExcel(expenses);
        IOUtils.copy(excelStream, response.getOutputStream());
        response.flushBuffer();
    }


}

