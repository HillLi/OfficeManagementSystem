package com.university.oms.design;

import com.university.oms.model.Travel;
import com.university.oms.model.TravelCheckResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TravelExpenseStrategyTest {
    @Test
    void teachingResearchTravelUsesRaisedHotelLimit() {
        Travel travel = new Travel();
        travel.setDestination("上海");
        travel.setStartDate(LocalDate.of(2026, 6, 1));
        travel.setEndDate(LocalDate.of(2026, 6, 3));
        travel.setStaffLevel("三类");
        travel.setTravelType("教学科研业务");
        travel.setBudget(new BigDecimal("2600"));

        TravelCheckResult result = new StandardTravelExpenseStrategy().check(travel);

        assertEquals(new BigDecimal("3240"), result.getStandardAmount());
        assertFalse(result.isOverLimit());
    }
}
