package com.example.societyfest.entity;

import com.example.societyfest.enums.PaymentMode;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "donation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomNumber;

    private double amount;

    private String building;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String remarks;

    @PrePersist
    public void setDefaultDateIfNull(){
        if (this.date == null){
            this.date = LocalDate.now();
        }
    }
}
