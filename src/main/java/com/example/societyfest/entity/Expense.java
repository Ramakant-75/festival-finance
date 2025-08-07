package com.example.societyfest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expense")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;

    private double amount;

    private LocalDate date;

    private String description;

    private String addedBy;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ExpenseReceipt> receipts;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL)
    private List<ExpensePayment> payments = new ArrayList<>();

    public Double getTotalPaid() {
        if (payments == null) return 0.0;
        return payments.stream().mapToDouble(p -> p.getAmount()).sum();
    }

    public Double getBalanceAmount() {
        return this.amount - getTotalPaid();
    }

    public Expense(Expense other) {
        this.id = other.id;
        this.category = other.category;
        this.amount = other.amount;
        this.date = other.date;
        this.description = other.description;
        this.addedBy = other.addedBy;
        this.receipts = other.getReceipts() != null ? new ArrayList<>(other.getReceipts()) : new ArrayList<>();
        this.payments = other.getPayments() != null ? new ArrayList<>(other.getPayments()) : new ArrayList<>();
    }
}
