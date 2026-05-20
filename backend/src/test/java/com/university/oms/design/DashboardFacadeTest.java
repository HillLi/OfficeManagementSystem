package com.university.oms.design;

import com.university.oms.model.DashboardStats;
import com.university.oms.repository.InMemoryDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DashboardFacadeTest {
    private InMemoryDatabase db;

    @BeforeEach
    void setUp() {
        db = new InMemoryDatabase();
        db.init();
    }

    @Test
    void aggregateReturnsCorrectCounts() {
        DashboardFacade facade = new DashboardFacade(db);
        DashboardStats stats = facade.aggregate();

        assertEquals(0, stats.getDocumentCount());
        assertEquals(0, stats.getMeetingCount());
        assertEquals(0, stats.getSealApplyCount());
    }

    @Test
    void aggregateWithDocumentsGivesStatusDistribution() {
        com.university.oms.model.Document doc = new com.university.oms.model.Document();
        doc.setTitle("测试");
        doc.setDocType("通知");
        doc.setStatus("pending_dept");
        db.fill(doc, 2001L);
        db.documents().put(2001L, doc);

        DashboardFacade facade = new DashboardFacade(db);
        DashboardStats stats = facade.aggregate();

        assertEquals(1, stats.getDocumentCount());
        assertTrue(stats.getDocumentStatusDistribution().containsKey("pending_dept"));
    }
}
