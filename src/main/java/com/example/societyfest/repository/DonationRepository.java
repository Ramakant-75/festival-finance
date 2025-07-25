package com.example.societyfest.repository;


import com.example.societyfest.entity.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    @Query("SELECT d FROM Donation d WHERE YEAR(d.date) = :year")
    Page<Donation> findAllByYear(@Param("year") int year, Pageable page);

    @Query("SELECT d FROM Donation d WHERE YEAR(d.date) = :year")
    List<Donation> findAllStatsByYear(@Param("year") int year);

    @Query("SELECT d FROM Donation d WHERE d.roomNumber = :room AND YEAR(d.date) = :year")
    Optional<Donation> findByRoomAndYear(@Param("room") String roomNumber, @Param("year") int year);

    @Query("SELECT COUNT(d) > 0 FROM Donation d WHERE d.building = :building AND d.roomNumber = :roomNumber AND YEAR(d.date) = :year")
    boolean existsByRoomNumberAndYear(@Param("building") String building, @Param("roomNumber") String roomNumber, @Param("year") String year);

    @Query("SELECT SUM(d.amount) FROM Donation d WHERE YEAR(d.date) = :year")
    Double sumAmountByYear(@Param("year") int year);
}
