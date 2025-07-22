package com.example.societyfest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @Lob
    @Column(name = "receipt",columnDefinition = "MEDIUMBLOB")
    private byte[] receipt;

    private String imageName;
}

