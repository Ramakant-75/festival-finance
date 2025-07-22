package com.example.societyfest.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRequest {
    private String category;
    private double amount;
    private LocalDate date;
    private String description;
    private String addedBy;
    private MultipartFile receipt;
}

