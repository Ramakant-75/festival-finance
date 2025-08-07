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

    // inside class Expense
    public Expense(Expense other) {
        this.id = other.id;
        this.category = other.category;
        this.amount = other.amount;
        this.date = other.date;
        this.description = other.description;
        this.addedBy = other.addedBy;
    }

}

