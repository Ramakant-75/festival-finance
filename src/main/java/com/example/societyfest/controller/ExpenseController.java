package com.example.societyfest.controller;

import com.example.societyfest.dto.ExpenseRequest;
import com.example.societyfest.dto.ExpenseResponse;
import com.example.societyfest.dto.ExpenseUpdateRequest;
import com.example.societyfest.dto.PaymentRequest;
import com.example.societyfest.entity.ExpenseReceipt;
import com.example.societyfest.repository.ExpenseReceiptRepository;
import com.example.societyfest.repository.ExpenseRepository;
import com.example.societyfest.service.ExpenseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepo;

    @Autowired
    private ExpenseReceiptRepository attachmentRepo;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExpenseResponse> addExpense(
            @ModelAttribute ExpenseRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(expenseService.addExpense(request,httpServletRequest));
    }


    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> list(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String addedBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<ExpenseResponse> result = expenseService.getFilteredExpenses(year, category, addedBy, pageable);
        return ResponseEntity.ok(result);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id, @RequestBody ExpenseUpdateRequest req, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(expenseService.updateExpense(id, req,httpServletRequest));
    }

    @PostMapping("/upload")
    public ResponseEntity<ExpenseResponse> uploadExpenseWithImage(
            @RequestPart("data") ExpenseRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ResponseEntity.ok(expenseService.saveExpenseWithImage(request, image));
    }



    @PostMapping("/{id}/upload")
    public ResponseEntity<?> upload(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files) throws Exception {
        expenseService.uploadReceipts(id, files);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{expenseId}/receipts/{receiptId}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long expenseId, @PathVariable Long receiptId) {
        ExpenseReceipt receipt = attachmentRepo.findById(receiptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receipt not found"));

        if (!receipt.getExpense().getId().equals(expenseId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receipt does not belong to the given expense");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(receipt.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + receipt.getFileName() + "\"")
                .body(receipt.getFile());
    }


    @GetMapping("/total")
    public ResponseEntity<Double> getFilteredTotal(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String addedBy
    ) {
        Double total = expenseService.getFilteredTotal(year, category, addedBy);
        return ResponseEntity.ok(total != null ? total : 0.0);
    }

    @GetMapping("/total-paid")
    public ResponseEntity<Double> getFilteredTotalPaid(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String addedBy
    ) {
        Double total = expenseService.getTotalPaid(category, year, addedBy);
        return ResponseEntity.ok(total != null ? total : 0.0);
    }

    @PostMapping("/{expenseId}/payments")
    public ResponseEntity<ExpenseResponse> addPayment(
            @PathVariable Long expenseId,
            @RequestBody PaymentRequest paymentRequest,
            HttpServletRequest httpServletRequest
    ) {
        ExpenseResponse response = expenseService.addPaymentToExpense(expenseId, paymentRequest, httpServletRequest);
        return ResponseEntity.ok(response);
    }

}

