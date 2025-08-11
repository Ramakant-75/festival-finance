package com.example.societyfest.service;


import com.example.societyfest.dto.DonationRequest;
import com.example.societyfest.dto.DonationResponse;
import com.example.societyfest.entity.Donation;
import com.example.societyfest.enums.PaymentMode;
import com.example.societyfest.repository.DonationRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepo;
    private final AuditLogService auditLogService;

    public DonationResponse addDonation(DonationRequest req, HttpServletRequest request) {
        Donation donation = Donation.builder()
                .roomNumber(req.getRoomNumber())
                .amount(req.getAmount())
                .building(req.getBuilding())
                .paymentMode(req.getPaymentMode())
                .date(req.getDate())
                .remarks(req.getRemarks())
                .build();
        donationRepo.save(donation);
        log.info("date : {}", req.getDate());
        auditLogService.logChange("ADD_DONATION", "DONATION", donation.getId().toString(), null, toResponse(donation), request);
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

    public Page<DonationResponse> getDonationsByYear(int year,
                                                     String building,PaymentMode paymentMode,
                                                     LocalDate date,Pageable pageable) {
        try {
            return donationRepo.findByYearAndFilters(year,building,paymentMode,date,pageable)
                    .map(this::toResponse);
        } catch (Exception e) {
            log.info("stacktrace : {}", e.getMessage());
        }
        return Page.empty(pageable);
    }

    public void updateDonation(Long id, DonationRequest req, HttpServletRequest request) {
        Donation donation = donationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        DonationResponse before = toResponse(new Donation(donation));

        if (req.getDate() == null) {
            req.setDate(LocalDate.now());
        }
        donation.setAmount(req.getAmount());
        donation.setPaymentMode(req.getPaymentMode());
        donation.setDate(req.getDate());
        donation.setRemarks(req.getRemarks());

        Donation updated = donationRepo.save(donation);

        DonationResponse after = toResponse(updated);

        auditLogService.logChange("EDIT_DONATION", "DONATION", donation.getId().toString(), before, after, request);
    }

    public Double getFilteredTotal(Integer year, String building, PaymentMode paymentMode,LocalDate date) {
        return donationRepo.findTotalByFilters(year, building, paymentMode,date);
    }
}

