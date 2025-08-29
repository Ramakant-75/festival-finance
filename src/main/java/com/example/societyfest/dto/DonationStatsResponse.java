package com.example.societyfest.dto;

import lombok.Data;

import java.util.List;

@Data
public class DonationStatsResponse {
    private String earlyBirdDonator;
    private String highestDonatingBuilding;
    private Double highestBuildingAmount;
    private String leastDonatingBuilding;
    private Double leastBuildingAmount;
    private List<TopDonator> topDonators;
    private Double totalDonations;


    @Data
    public static class TopDonator {
        private String building;
        private String room;
        private Double totalAmount;

        public TopDonator(String building, String room, Double totalAmount) {
            this.building = building;
            this.room = room;
            this.totalAmount = totalAmount;
        }

    }
}

