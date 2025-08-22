package com.example.societyfest.repository;


import com.example.societyfest.entity.Donation;
import com.example.societyfest.enums.PaymentMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    @Query("""
      SELECT d FROM Donation d
      WHERE YEAR(d.date) = :year
        AND (:building IS NULL OR d.building = :building)
        AND (:paymentMode IS NULL OR d.paymentMode = :paymentMode)
        AND (:date IS NULL OR d.date = :date)
        AND (:isExternal IS NULL OR d.isExternal = :isExternal)
    """)
    Page<Donation> findByYearAndFilters(
            @Param("year") int year,
            @Param("building") String building,
            @Param("paymentMode") com.example.societyfest.enums.PaymentMode paymentMode,
            @Param("date") java.time.LocalDate date,
            @Param("isExternal") Boolean isExternal,
            Pageable pageable
    );

    @Query("SELECT d FROM Donation d WHERE YEAR(d.date) = :year")
    List<Donation> findAllStatsByYear(@Param("year") int year);

    @Query("SELECT d FROM Donation d WHERE d.roomNumber = :room AND YEAR(d.date) = :year")
    Optional<Donation> findByRoomAndYear(@Param("room") String roomNumber, @Param("year") int year);

    @Query("SELECT COUNT(d) > 0 FROM Donation d WHERE d.building = :building AND d.roomNumber = :roomNumber AND YEAR(d.date) = :year")
    boolean existsByRoomNumberAndYear(@Param("building") String building, @Param("roomNumber") String roomNumber, @Param("year") String year);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE YEAR(d.date) = :year")
    Double sumAmountByYear(@Param("year") int year);

    @Query("""
  SELECT d FROM Donation d
  WHERE YEAR(d.date) = :year
    AND (:building IS NULL OR d.building = :building)
    AND (:paymentMode IS NULL OR d.paymentMode = :paymentMode)
    AND (:date IS NULL OR d.date = :date)
""")
    List<Donation> findAllByFilters(
            @Param("year") int year,
            @Param("building") String building,
            @Param("paymentMode") com.example.societyfest.enums.PaymentMode paymentMode,
            @Param("date") java.time.LocalDate date
    );

    @Query("""
      SELECT SUM(d.amount) FROM Donation d
      WHERE YEAR(d.date) = :year
        AND (:building IS NULL OR d.building = :building)
        AND (:paymentMode IS NULL OR d.paymentMode = :paymentMode)
        AND (:date IS NULL OR d.date = :date)
        AND (:isExternal IS NULL OR
             (:isExternal = TRUE AND d.isExternal = TRUE) OR
             (:isExternal = FALSE AND d.isExternal = FALSE))
    """)
    Double findTotalByFilters(
            @Param("year") int year,
            @Param("building") String building,
            @Param("paymentMode") com.example.societyfest.enums.PaymentMode paymentMode,
            @Param("date") java.time.LocalDate date,
            @Param("isExternal") Boolean isExternal
    );


    @Query("SELECT COUNT(d) FROM Donation d WHERE d.paymentMode = :paymentMode AND YEAR(d.date) = :year")
    Long countByPaymentModeAndYear(@Param("paymentMode") PaymentMode paymentMode, @Param("year") int year);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE d.paymentMode = :paymentMode AND YEAR(d.date) = :year")
    Double sumByModeAndYear(@Param("paymentMode") PaymentMode paymentMode, @Param("year") int year);

}
