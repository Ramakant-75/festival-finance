package com.example.societyfest.controller;

import com.example.societyfest.dto.ExpenseRequest;
import com.example.societyfest.dto.ExpenseResponse;
import com.example.societyfest.entity.Expense;
import com.example.societyfest.repository.ExpenseRepository;
import com.example.societyfest.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepo;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExpenseResponse> addExpense(
            @ModelAttribute ExpenseRequest request
    ) {
        return ResponseEntity.ok(expenseService.addExpense(request));
    }


    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> list() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id, @RequestBody ExpenseRequest req) {
        return ResponseEntity.ok(expenseService.updateExpense(id, req));
    }

    @PostMapping("/upload")
    public ResponseEntity<ExpenseResponse> uploadExpenseWithImage(
            @RequestPart("data") ExpenseRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ResponseEntity.ok(expenseService.saveExpenseWithImage(request, image));
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> getExpenseReceipt(@PathVariable Long id) {
        Expense expense = expenseRepo.findById(id).orElseThrow(() -> new RuntimeException("Expense not found"));

        if (expense.getReceipt() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // or detect dynamically
        return new ResponseEntity<>(expense.getReceipt(), headers, HttpStatus.OK);
    }


}

