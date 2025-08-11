package com.example.societyfest.repository;

import com.example.societyfest.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query(value = "SELECT e FROM Expense e WHERE YEAR(e.date) = :year")
    List<Expense> findAllByYear(@Param("year") int year);

    @Query("SELECT e FROM Expense e WHERE "
            + "(:year IS NULL OR FUNCTION('YEAR', e.date) = :year) AND "
            + "(:category IS NULL OR e.category = :category) AND "
            + "(:addedBy IS NULL OR LOWER(e.addedBy) LIKE LOWER(CONCAT('%', :addedBy, '%')))")
    Page<Expense> findFiltered(
            @Param("year") Integer year,
            @Param("category") String category,
            @Param("addedBy") String addedBy,
            Pageable pageable
    );

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE "
            + "(:year IS NULL OR FUNCTION('YEAR', e.date) = :year) AND "
            + "(:category IS NULL OR e.category = :category) AND "
            + "(:addedBy IS NULL OR LOWER(e.addedBy) LIKE LOWER(CONCAT('%', :addedBy, '%')))")
    Double getTotalAmountFiltered(
            @Param("year") Integer year,
            @Param("category") String category,
            @Param("addedBy") String addedBy
    );

}
