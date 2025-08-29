package com.example.societyfest.service;

import com.example.societyfest.dto.DonationStatsResponse;
import com.example.societyfest.repository.DonationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class DonationStatsService {

    @Autowired
    private DonationRepository donationRepository;

    public DonationStatsResponse getStats() {
        DonationStatsResponse response = new DonationStatsResponse();

        // 1. Early bird
        donationRepository.findFirstByIsExternalFalseOrderByIdAsc()
                .ifPresent(d -> response.setEarlyBirdDonator(d.getBuilding() + "-" + d.getRoomNumber()));

        // 2. Highest & Least building
        List<Object[]> buildingTotals = donationRepository.findBuildingTotals();
        if (!buildingTotals.isEmpty()) {
            Object[] highest = buildingTotals.get(0);
            response.setHighestDonatingBuilding((String) highest[0]);
            response.setHighestBuildingAmount((Double) highest[1]);

            Object[] least = buildingTotals.get(buildingTotals.size() - 1);
            response.setLeastDonatingBuilding((String) least[0]);
            response.setLeastBuildingAmount((Double) least[1]);
        }

        // 3. Top 3 donators
        List<Object[]> topDonators = donationRepository.findTopDonators();
        List<DonationStatsResponse.TopDonator> topList = topDonators.stream()
                .limit(3)
                .map(obj -> new DonationStatsResponse.TopDonator(
                        (String) obj[0],
                        (String) obj[1],
                        (Double) obj[2]
                ))
                .toList();

        Double total = donationRepository.sumAmountByYear(LocalDate.now().getYear()); // <-- implement this query
        response.setTotalDonations(total != null ? total : 0);

        response.setTopDonators(topList);

        return response;
    }
}
