package com.university.oms.design;

import com.university.oms.model.DashboardStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DashboardFacadeTest {
    @Autowired
    private DashboardFacade facade;

    @Test
    void aggregateReturnsCorrectCounts() {
        DashboardStats stats = facade.aggregate();

        assertEquals(0, stats.getDocumentCount());
        assertEquals(0, stats.getMeetingCount());
        assertEquals(0, stats.getSealApplyCount());
    }

    @Test
    void aggregateWithNoDocumentsGivesEmptyStatusDistribution() {
        DashboardStats stats = facade.aggregate();

        assertTrue(stats.getDocumentStatusDistribution().isEmpty()
                || stats.getDocumentStatusDistribution().values().stream().allMatch(v -> v == 0L));
    }
}
