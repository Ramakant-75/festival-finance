package com.example.societyfest.repository;

import com.example.societyfest.entity.ExpensePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpensePaymentRepository extends JpaRepository<ExpensePayment, Long> {

    @Query(value = """
    SELECT COALESCE(SUM(ep.amount), 0)
    FROM expense_payment ep
    JOIN expense e ON ep.expense_id = e.id
    WHERE (:category IS NULL OR e.category = :category)
      AND (:year IS NULL OR YEAR(e.date) = :year)
      AND (:addedBy IS NULL OR e.added_by = :addedBy)
""", nativeQuery = true)
    Double getTotalPaid(@Param("category") String category,
                        @Param("year") Integer year,
                        @Param("addedBy") String addedBy);

}
