package com.example.societyfest.dto;

import com.example.societyfest.enums.PaymentMode;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationRequest {
    private String name;
    private String roomNumber;
    private double amount;
    private PaymentMode paymentMode;
    private String building;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String remarks;
    private Boolean isExternal;
}

