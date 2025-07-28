package com.example.societyfest.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExpenseUpdateRequest {
    private String category;
    private double amount;
    private LocalDate date;
    private String description;
    private String addedBy;
}
