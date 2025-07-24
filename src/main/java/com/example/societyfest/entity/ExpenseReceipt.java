package com.example.societyfest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expense_attachment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "file", columnDefinition = "MEDIUMBLOB")
    private byte[] file;

    private String fileName;

    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private Expense expense;
}
