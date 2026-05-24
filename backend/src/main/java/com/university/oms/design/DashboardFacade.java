package com.university.oms.design;

import com.university.oms.model.*;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiPredicate;

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
        return aggregate((bizType, bizId) -> true);
    }

    public DashboardStats aggregate(BiPredicate<String, Long> canRead) {
        List<Document> documents = visible("document", db.documents().values(), canRead);
        List<SealApplication> sealApplications = visible("seal", db.sealApplications().values(), canRead);
        List<Meeting> meetings = visible("meeting", db.meetings().values(), canRead);
        List<Travel> travels = visible("travel", db.travels().values(), canRead);
        List<Report> reports = visible("report", db.reports().values(), canRead);
        DashboardStats stats = new DashboardStats();
        stats.setDocumentCount(documents.size());
        stats.setPendingDocumentCount(documents.stream()
                .filter(d -> d.getStatus().startsWith("pending")).count());
        stats.setSealApplyCount(sealApplications.size());
        stats.setMeetingCount(meetings.size());
        stats.setTravelCount(travels.size());
        stats.setReportCount(reports.size());
        stats.setLargeActivityCount(meetings.stream()
                .filter(Meeting::isLargeActivity).count());

        BigDecimal total = BigDecimal.ZERO;
        for (Travel travel : travels) {
            if (travel.getBudget() != null) {
                total = total.add(travel.getBudget());
            }
        }
        stats.setTravelBudgetTotal(total);

        Map<String, Long> statusDist = new LinkedHashMap<>();
        for (Document doc : documents) {
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
        countByMonth(monthly, documents, fmt);
        countByMonth(monthly, sealApplications, fmt);
        countByMonth(monthly, meetings, fmt);
        countByMonth(monthly, travels, fmt);
        countByMonth(monthly, reports, fmt);
        stats.setMonthlyBusinessCounts(monthly);

        return stats;
    }

    private <T extends BaseEntity> List<T> visible(String bizType, Collection<T> entities,
                                                    BiPredicate<String, Long> canRead) {
        List<T> result = new ArrayList<T>();
        for (T entity : entities) {
            if (canRead.test(bizType, entity.getId())) {
                result.add(entity);
            }
        }
        return result;
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
