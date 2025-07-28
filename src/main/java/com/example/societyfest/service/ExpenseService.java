package com.example.societyfest.service;

import com.example.societyfest.dto.ExpenseRequest;
import com.example.societyfest.dto.ExpenseResponse;
import com.example.societyfest.dto.ExpenseUpdateRequest;
import com.example.societyfest.entity.Expense;
import com.example.societyfest.entity.ExpenseReceipt;
import com.example.societyfest.repository.ExpenseReceiptRepository;
import com.example.societyfest.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepo;

    @Autowired
    private ExpenseReceiptRepository receiptRepo;

    public ExpenseResponse addExpense(ExpenseRequest req) {
        boolean hasReceipt = (req.getReceipts() != null && req.getReceipts().length > 0);

        Expense expense = Expense.builder()
                .category(req.getCategory())
                .amount(req.getAmount())
                .date(req.getDate())
                .description(req.getDescription())
                .addedBy(req.getAddedBy())
                .build();

        Expense savedExpense = expenseRepo.save(expense);

        if (req.getReceipts() != null) {
            for (MultipartFile file : req.getReceipts()) {
                if (!file.isEmpty()) {
                    try {
                        ExpenseReceipt receipt = ExpenseReceipt.builder()
                                .expense(savedExpense)
                                .fileName(file.getOriginalFilename())
                                .file(file.getBytes())
                                .contentType(file.getContentType())
                                .build();
                        receiptRepo.save(receipt);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to store receipt: " + file.getOriginalFilename(), e);
                    }
                }
            }
        }

        return toResponse(savedExpense);
    }

    public void uploadReceipts(Long expenseId, List<MultipartFile> files) throws Exception {
        Expense expense = expenseRepo.findById(expenseId).orElseThrow();
        List<ExpenseReceipt> receipts = new ArrayList<>();
        for (MultipartFile file : files) {
            receipts.add(ExpenseReceipt.builder()
                    .file(file.getBytes())
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .expense(expense)
                    .build());
        }
        receiptRepo.saveAll(receipts);
    }

    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepo.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ExpenseResponse toResponse(Expense e) {
        List<ExpenseReceipt> receiptList = receiptRepo.findByExpenseId(e.getId());

        boolean hasReceipt = !receiptList.isEmpty();

        List<ExpenseResponse.ReceiptMetadata> receiptMetadataList = receiptList.stream()
                .map(r -> new ExpenseResponse.ReceiptMetadata(r.getId(), r.getFileName()))
                .collect(Collectors.toList());

        return ExpenseResponse.builder()
                .id(e.getId())
                .category(e.getCategory())
                .amount(e.getAmount())
                .date(e.getDate())
                .description(e.getDescription())
                .addedBy(e.getAddedBy())
                .hasReceipt(hasReceipt)
                .receipts(receiptMetadataList)
                .build();
    }


    public ExpenseResponse updateExpense(Long id, ExpenseUpdateRequest req) {
        Expense expense = expenseRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Expense not found"));

        expense.setCategory(req.getCategory());
        expense.setAmount(req.getAmount());
        expense.setDate(req.getDate());
        expense.setDescription(req.getDescription());
        expense.setAddedBy(req.getAddedBy());

        expenseRepo.save(expense);
        return toResponse(expense);
    }

    public ExpenseResponse saveExpenseWithImage(ExpenseRequest req, MultipartFile image) {
        byte[] imageData = null;
        String imageName = null;

        if (image != null && !image.isEmpty()) {
            String type = image.getContentType();
            if (type == null || !type.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed.");
            }
            try {
                imageData = image.getBytes();
                imageName = image.getOriginalFilename();
            } catch (IOException e) {
                throw new RuntimeException("Failed to read image file.");
            }
        }

        Expense expense = Expense.builder()
                .category(req.getCategory())
                .amount(req.getAmount())
                .date(req.getDate())
                .description(req.getDescription())
                .addedBy(req.getAddedBy())
                .build();

        expenseRepo.save(expense);
        return toResponse(expense);
    }

    public List<ExpenseResponse> getExpensesByYear(int year) {
        List<Expense> list = expenseRepo.findAllByYear(year);
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Page<ExpenseResponse> getFilteredExpenses(Integer year, String category, String addedBy, Pageable pageable) {
        Page<Expense> page = expenseRepo.findFiltered(year, category, addedBy, pageable);
        return page.map(this::toResponse);
    }

    public Double getFilteredTotal(Integer year, String category, String addedBy) {
        return expenseRepo.getTotalAmountFiltered(year, category, addedBy);
    }


}
