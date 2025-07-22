package com.example.societyfest.controller;

import com.example.societyfest.dto.DonationRequest;
import com.example.societyfest.dto.DonationResponse;
import com.example.societyfest.repository.DonationRepository;
import com.example.societyfest.service.DonationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @Autowired
    private DonationRepository donationRepository;

    @PostMapping
    public ResponseEntity<DonationResponse> add(@RequestBody DonationRequest request) {
        log.info("calling add api ---> ");
        return ResponseEntity.ok(donationService.addDonation(request));
    }

    @GetMapping
    public ResponseEntity<List<DonationResponse>> list(@RequestParam(required = false) int year) {
        return ResponseEntity.ok(donationService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDonation(@PathVariable Long id, @RequestBody DonationRequest request){
        donationService.updateDonation(id, request);
        return ResponseEntity.ok("Donation updated");
    }

    @GetMapping("/yearwise")
    public ResponseEntity<List<DonationResponse>> listDonationsByYear(@RequestParam int year){
        return ResponseEntity.ok(donationService.getDonationsByYear(year));
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkIfExists(@RequestParam String building, @RequestParam String roomNumber,@RequestParam String year) {
        if (year.equals("NaN")){
            year = String.valueOf(LocalDate.now().getYear());
        }
        log.info("year : {} ", year);
        boolean exists = donationRepository.existsByRoomNumberAndYear(building, roomNumber, year);
        return ResponseEntity.ok(exists);
    }

}


