package com.example.societyfest.service;

import com.example.societyfest.dto.ExpenseRequest;
import com.example.societyfest.dto.ExpenseResponse;
import com.example.societyfest.entity.Expense;
import com.example.societyfest.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepo;

    public ExpenseResponse addExpense(ExpenseRequest req) {
        byte[] receiptBytes = null;
        try {
            if (req.getReceipt() != null && !req.getReceipt().isEmpty()) {
                receiptBytes = req.getReceipt().getBytes();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading receipt file", e);
        }

        Expense expense = Expense.builder()
                .category(req.getCategory())
                .amount(req.getAmount())
                .date(req.getDate())
                .description(req.getDescription())
                .addedBy(req.getAddedBy())
                .receipt(receiptBytes) // <--- Save file
                .build();

        expenseRepo.save(expense);
        return toResponse(expense);
    }


    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepo.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ExpenseResponse toResponse(Expense e) {
        return ExpenseResponse.builder()
                .id(e.getId())
                .category(e.getCategory())
                .amount(e.getAmount())
                .date(e.getDate())
                .description(e.getDescription())
                .addedBy(e.getAddedBy())
                .hasReceipt(e.getReceipt() != null)
                .build();
    }

    public ExpenseResponse updateExpense(Long id, ExpenseRequest req) {
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
                .receipt(imageData)
                .imageName(imageName)
                .build();

        expenseRepo.save(expense);
        return toResponse(expense);
    }


}

