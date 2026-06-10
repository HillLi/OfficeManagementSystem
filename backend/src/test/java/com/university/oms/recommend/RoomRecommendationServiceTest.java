package com.university.oms.recommend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomRecommendationServiceTest {

    private final RoomRecommendationService service = new RoomRecommendationService(null);

    @Test
    void capacityFitPerfectMatch() {
        double score = service.computeCapacityFit(20, 20);
        assertEquals(1.0, score, 0.001, "Exact match should score 1.0");
    }

    @Test
    void capacityFitSlightlyLarger() {
        double score = service.computeCapacityFit(25, 20);
        assertTrue(score > 0.5, "Slightly larger room should score reasonably high, got " + score);
    }

    @Test
    void capacityFitMuchLarger() {
        double score = service.computeCapacityFit(200, 20);
        assertTrue(score < 0.5, "Much larger room should score low, got " + score);
    }

    @Test
    void capacityFitTooSmall() {
        double score = service.computeCapacityFit(10, 20);
        assertEquals(0.0, score, 0.001, "Room too small should score 0.0");
    }

    @Test
    void capacityFitGaussianDecay() {
        double perfect = service.computeCapacityFit(50, 50);
        double slightlyOver = service.computeCapacityFit(60, 50);
        double moreOver = service.computeCapacityFit(100, 50);
        assertTrue(perfect > slightlyOver, "Perfect fit should score higher than slightly over");
        assertTrue(slightlyOver > moreOver, "Slightly over should score higher than much over");
    }

    @Test
    void equipmentMatchNoRequirement() {
        double score = service.computeEquipmentMatch("投影仪,白板", null);
        assertEquals(1.0, score, 0.001, "No requirement means perfect match");
    }

    @Test
    void equipmentMatchEmptyRequirement() {
        double score = service.computeEquipmentMatch("投影仪,白板", "");
        assertEquals(1.0, score, 0.001, "Empty requirement means perfect match");
    }

    @Test
    void equipmentMatchNoRoomEquipment() {
        double score = service.computeEquipmentMatch(null, "投影仪");
        assertEquals(0.0, score, 0.001, "No room equipment should score 0.0");
    }

    @Test
    void utilizationBalanceNoMeetings() {
        double score = service.computeUtilizationBalance(1L, java.util.Collections.emptyList());
        assertEquals(1.0, score, 0.001, "No meetings means perfect balance");
    }

    @Test
    void capacityFitHandlesZeroExpected() {
        double score = service.computeCapacityFit(10, 0);
        assertTrue(score >= 0, "Should not throw with expected=0");
    }
}
