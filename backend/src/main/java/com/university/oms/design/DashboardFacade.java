package com.university.oms.design;

import com.university.oms.model.*;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Facade pattern — aggregates statistics from all business modules.
 */
@Component
public class DashboardFacade {
    private final InMemoryDatabase db;

    public DashboardFacade(InMemoryDatabase db) {
        this.db = db;
    }

    public DashboardStats aggregate() {
        DashboardStats stats = new DashboardStats();
        stats.setDocumentCount(db.documents().size());
        stats.setPendingDocumentCount(db.documents().values().stream()
                .filter(d -> d.getStatus().startsWith("pending")).count());
        stats.setSealApplyCount(db.sealApplications().size());
        stats.setMeetingCount(db.meetings().size());
        stats.setTravelCount(db.travels().size());
        stats.setReportCount(db.reports().size());
        stats.setLargeActivityCount(db.meetings().values().stream()
                .filter(Meeting::isLargeActivity).count());

        BigDecimal total = BigDecimal.ZERO;
        for (Travel travel : db.travels().values()) {
            if (travel.getBudget() != null) {
                total = total.add(travel.getBudget());
            }
        }
        stats.setTravelBudgetTotal(total);

        Map<String, Long> statusDist = new LinkedHashMap<>();
        for (Document doc : db.documents().values()) {
            statusDist.merge(doc.getStatus(), 1L, Long::sum);
        }
        stats.setDocumentStatusDistribution(statusDist);

        Map<String, Long> monthly = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            String key = now.minusMonths(i).format(fmt);
            monthly.put(key, 0L);
        }
        countByMonth(monthly, db.documents().values(), fmt);
        countByMonth(monthly, db.sealApplications().values(), fmt);
        countByMonth(monthly, db.meetings().values(), fmt);
        countByMonth(monthly, db.travels().values(), fmt);
        countByMonth(monthly, db.reports().values(), fmt);
        stats.setMonthlyBusinessCounts(monthly);

        return stats;
    }

    private void countByMonth(Map<String, Long> monthly, Collection<? extends BaseEntity> entities,
                              DateTimeFormatter fmt) {
        for (BaseEntity e : entities) {
            if (e.getCreatedAt() != null) {
                String key = e.getCreatedAt().format(fmt);
                if (monthly.containsKey(key)) {
                    monthly.merge(key, 1L, Long::sum);
                }
            }
        }
    }
}
