package com.example.societyfest.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentRequest {
    private double amount;
    private LocalDate paymentDate;
    private String paidBy;
    private String note;
    private String paymentMethod;
}
