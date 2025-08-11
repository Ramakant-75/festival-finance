package com.example.societyfest.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private double amount;
    private LocalDate paymentDate;
    private String paidBy;
    private String note;
    private String paymentMethod;
}
