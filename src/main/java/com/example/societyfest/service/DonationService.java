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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepo;
    private final AuditLogService auditLogService;

    // Base milestones up to 100k
    private static final List<Integer> BASE_MILESTONES = List.of(25000, 50000, 75000, 100000);
    private static final int STEP_SIZE = 25000; // milestones will continue in steps of 25k

    public DonationResponse addDonation(DonationRequest req, HttpServletRequest request) {
        int currentYear = LocalDate.now().getYear();
        Double beforeTotal = donationRepo.sumAmountByYear(currentYear);

        Donation donation = Donation.builder()
                .roomNumber(req.getRoomNumber())
                .name(req.getName())
                .amount(req.getAmount())
                .building(req.getBuilding())
                .paymentMode(req.getPaymentMode())
                .date(req.getDate() != null ? req.getDate() : LocalDate.now())
                .remarks(req.getRemarks())
                .isExternal(req.getIsExternal())
                .build();

        donationRepo.save(donation);
        auditLogService.logChange("ADD_DONATION", "DONATION", donation.getId().toString(), null, toResponse(donation), request);

        Double afterTotal = donationRepo.sumAmountByYear(currentYear);
        List<Integer> unlocked = checkUnlockedMilestones(beforeTotal, afterTotal);

        DonationResponse response = toResponse(donation);
        response.setUnlockedMilestones(unlocked);
        return response;
    }

    public List<DonationResponse> getAll() {
        try {
            return donationRepo.findAll()
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("stacktrace : {}", e.getMessage());
        }
        return null;
    }

    private DonationResponse toResponse(Donation donation) {
        return DonationResponse.builder()
                .id(donation.getId())
                .name(donation.getName())
                .amount(donation.getAmount())
                .paymentMode(donation.getPaymentMode())
                .building(donation.getBuilding())
                .date(donation.getDate())
                .remarks(donation.getRemarks())
                .roomNumber(donation.getRoomNumber())
                .isExternal(donation.getIsExternal())
                .build();
    }

    public Page<DonationResponse> getDonationsByYear(int year,
                                                     String building, PaymentMode paymentMode,
                                                     LocalDate date, Boolean isExternal, Pageable pageable) {
        try {
            return donationRepo.findByYearAndFilters(year, building, paymentMode, date, isExternal, pageable)
                    .map(this::toResponse);
        } catch (Exception e) {
            log.info("stacktrace : {}", e.getMessage());
        }
        return Page.empty(pageable);
    }

    public DonationResponse updateDonation(Long id, DonationRequest req, HttpServletRequest request) {
        int currentYear = LocalDate.now().getYear();
        Double beforeTotal = donationRepo.sumAmountByYear(currentYear);

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
        donation.setName(req.getName());
        donation.setIsExternal(req.getIsExternal());

        Donation updated = donationRepo.save(donation);

        Double afterTotal = donationRepo.sumAmountByYear(currentYear);
        List<Integer> unlocked = checkUnlockedMilestones(beforeTotal, afterTotal);

        DonationResponse after = toResponse(updated);
        after.setUnlockedMilestones(unlocked);
        after.setTotalDonation(afterTotal.longValue()); // 👈 also return running total

        auditLogService.logChange("EDIT_DONATION", "DONATION", donation.getId().toString(), before, after, request);

        return after;
    }

    public Double getFilteredTotal(Integer year, String building, PaymentMode paymentMode, LocalDate date, Boolean isExternal) {
        return donationRepo.findTotalByFilters(year, building, paymentMode, date, isExternal);
    }

    private List<Integer> checkUnlockedMilestones(Double previousTotal, Double newTotal) {
        List<Integer> allMilestones = new ArrayList<>(BASE_MILESTONES);

        // extend dynamically beyond the last base milestone
        int lastBase = BASE_MILESTONES.get(BASE_MILESTONES.size() - 1);
        int maxTarget = (int) Math.ceil(newTotal / STEP_SIZE) * STEP_SIZE;

        for (int m = lastBase + STEP_SIZE; m <= maxTarget; m += STEP_SIZE) {
            allMilestones.add(m);
        }

        return allMilestones.stream()
                .filter(m -> previousTotal < m && newTotal >= m)
                .collect(Collectors.toList());
    }
}
