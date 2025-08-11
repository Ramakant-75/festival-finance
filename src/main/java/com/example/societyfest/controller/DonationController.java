package com.example.societyfest.controller;

import com.example.societyfest.dto.DonationRequest;
import com.example.societyfest.dto.DonationResponse;
import com.example.societyfest.enums.PaymentMode;
import com.example.societyfest.repository.DonationRepository;
import com.example.societyfest.service.DonationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @Autowired
    private DonationRepository donationRepository;

    @PostMapping
    public ResponseEntity<DonationResponse> add(@RequestBody DonationRequest request, HttpServletRequest httpServletRequest) {
        log.info("calling add api ---> ");
        return ResponseEntity.ok(donationService.addDonation(request,httpServletRequest));
    }

    @GetMapping
    public ResponseEntity<Page<DonationResponse>> list(@RequestParam int year,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(required = false) String building,
                                                       @RequestParam(required = false)PaymentMode paymentMode,
                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        PageRequest pageRequest = PageRequest.of(page,size);
        return ResponseEntity.ok(donationService.getDonationsByYear(year,building,paymentMode,date,pageRequest));
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDonation(@PathVariable Long id, @RequestBody DonationRequest request,
                                            HttpServletRequest httpServletRequest){
        donationService.updateDonation(id, request,httpServletRequest);
        return ResponseEntity.ok("Donation updated");
    }

//    @GetMapping("/yearwise")
//    public ResponseEntity<List<DonationResponse>> listDonationsByYear(@RequestParam int year){
//        return ResponseEntity.ok(donationService.getDonationsByYear(year));
//    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkIfExists(@RequestParam String building, @RequestParam String roomNumber,@RequestParam String year) {
        if (year.equals("NaN")){
            year = String.valueOf(LocalDate.now().getYear());
        }
        log.info("year : {} ", year);
        boolean exists = donationRepository.existsByRoomNumberAndYear(building, roomNumber, year);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/total")
    public ResponseEntity<Double> getFilteredTotal(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) PaymentMode paymentMode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Double total = donationService.getFilteredTotal(year, building, paymentMode,date);
        return ResponseEntity.ok(total != null ? total : 0.0);
    }

}


