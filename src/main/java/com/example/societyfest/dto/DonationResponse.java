package com.example.societyfest.dto;

import com.example.societyfest.enums.PaymentMode;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationResponse {
    private Long id;
    private String building;
    private int floor;
    private String roomNumber;
    private double amount;
    private PaymentMode paymentMode;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String remarks;
}
