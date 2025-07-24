package com.example.societyfest.repository;

import com.example.societyfest.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query(value = "SELECT e FROM Expense e WHERE YEAR(e.date) = :year")
    List<Expense> findAllByYear(@Param("year") int year);
}
