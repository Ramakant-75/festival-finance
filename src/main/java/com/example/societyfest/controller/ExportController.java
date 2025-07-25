package com.example.societyfest.controller;

import com.example.societyfest.entity.Donation;
import com.example.societyfest.entity.Expense;
import com.example.societyfest.enums.PaymentMode;
import com.example.societyfest.repository.DonationRepository;
import com.example.societyfest.repository.ExpenseRepository;
import com.example.societyfest.util.ExcelGenerator;
import com.example.societyfest.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final DonationRepository donationRepo;
    private final ExpenseRepository expenseRepo;

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


    @GetMapping("/expenses")
    public ResponseEntity<InputStreamResource> exportExpenses() {
        var excelStream = ExcelGenerator.expensesToExcel(expenseRepo.findAll());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(excelStream));
    }

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

//    @PostMapping("/festival-pdf")
//    public ResponseEntity<InputStreamResource> generateFestivalPdf(
//            @RequestParam("year") int year,
//            @RequestParam("chartImage") MultipartFile chartImage
//    ) throws Exception {
//
//        List<Donation> donations = donationRepo.findAllByYear(year);
//        List<Expense> expenses = expenseRepo.findAllByYear(year);
//
//        double totalDonations = donations.stream().mapToDouble(Donation::getAmount).sum();
//        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
//        double balance = totalDonations - totalExpenses;
//
//        byte[] chartImageBytes = chartImage.getBytes();
//
//        ByteArrayInputStream pdf = PdfGenerator.generateDetailedReport(
//                year,
//                totalDonations,
//                totalExpenses,
//                balance,
//                expenses,
//                chartImageBytes
//        );
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=festival-report-" + year + ".pdf")
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(new InputStreamResource(pdf));
//    }


}

