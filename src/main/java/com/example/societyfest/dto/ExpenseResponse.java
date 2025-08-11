package com.example.societyfest.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {
    private Long id;
    private String category;
    private double amount;
    private LocalDate date;
    private String description;
    private String addedBy;
    private List<String> receiptFileNames;
    private boolean hasReceipt;
    private Double totalPaid;
    private Double balanceAmount;
    private List<PaymentResponse> payments;
    private List<ReceiptMetadata> receipts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReceiptMetadata {
        private Long id;
        private String fileName;
    }
}

