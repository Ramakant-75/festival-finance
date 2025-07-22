package com.example.societyfest.service;


import com.example.societyfest.dto.DonationRequest;
import com.example.societyfest.dto.DonationResponse;
import com.example.societyfest.entity.Donation;
import com.example.societyfest.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepo;

    public DonationResponse addDonation(DonationRequest req) {
        Donation donation = Donation.builder()
                .roomNumber(req.getRoomNumber())
                .amount(req.getAmount())
                .building(req.getBuilding())
                .paymentMode(req.getPaymentMode())
                .date(req.getDate())
                .remarks(req.getRemarks())
                .build();
        donationRepo.save(donation);
        log.info("date : {}" , req.getDate());
        return toResponse(donation);
    }

    public List<DonationResponse> getAll() {
        try {
            return donationRepo.findAll()
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }catch (Exception e){
            log.error("stacktrace : {}", e.getMessage());
        }
        return null;
    }

    private DonationResponse toResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .amount(donation.getAmount())
                .paymentMode(donation.getPaymentMode())
                .building(donation.getBuilding())
                .date(donation.getDate())
                .remarks(donation.getRemarks())
                .roomNumber(donation.getRoomNumber())
                .build();
    }

    public List<DonationResponse> getDonationsByYear(int year) {
        try {
            return donationRepo.findAllByYear(year)
                    .stream()
                    .map(d -> DonationResponse.builder()
                            .roomNumber(d.getRoomNumber())
                            .amount(d.getAmount())
                            .building(d.getBuilding())
                            .paymentMode(d.getPaymentMode())
                            .date(d.getDate())
                            .remarks(d.getRemarks())
                            .build())
                    .toList();
        } catch (Exception e) {
            log.info("stacktrace : {}", e.getMessage());
        }
        return null;
    }

    public void updateDonation(Long id, DonationRequest req) {
        Donation donation = donationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        if (req.getDate() == null){
            req.setDate(LocalDate.now());
        }
        donation.setAmount(req.getAmount());
        donation.setPaymentMode(req.getPaymentMode());
        donation.setDate(req.getDate());
        donation.setRemarks(req.getRemarks());

        donationRepo.save(donation);
    }
}

