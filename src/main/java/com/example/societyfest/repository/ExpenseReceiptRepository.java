package com.example.societyfest.repository;

import com.example.societyfest.entity.ExpenseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseReceiptRepository extends JpaRepository<ExpenseReceipt, Long> {

    List<ExpenseReceipt> findByExpenseId(Long expenseId);
}
