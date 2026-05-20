package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.Travel;
import com.university.oms.model.TravelCheckResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Component
public class StandardTravelExpenseStrategy implements TravelExpenseStrategy {
    @Override
    public TravelCheckResult check(Travel travel) {
        if (travel.getEndDate().isBefore(travel.getStartDate())) {
            throw new BusinessException("返回日期不能早于出发日期");
        }
        long days = ChronoUnit.DAYS.between(travel.getStartDate(), travel.getEndDate()) + 1;
        BigDecimal hotel = hotelLimit(travel.getStaffLevel(), travel.getTravelType()).multiply(BigDecimal.valueOf(days));
        BigDecimal meal = BigDecimal.valueOf(isSpecialArea(travel.getDestination()) ? 120 : 100).multiply(BigDecimal.valueOf(days));
        BigDecimal localTransport = BigDecimal.valueOf(80).multiply(BigDecimal.valueOf(days));
        BigDecimal standard = hotel.add(meal).add(localTransport);
        BigDecimal budget = travel.getBudget() == null ? BigDecimal.ZERO : travel.getBudget();

        TravelCheckResult result = new TravelCheckResult();
        result.setStandardAmount(standard);
        result.setOverLimit(budget.compareTo(standard) > 0);
        result.getMessages().add("住宿、伙食补助、市内交通补助合计标准：" + standard + " 元。");
        result.getMessages().add(result.isOverLimit() ? "预算超过标准，请填写超标说明并提交财务复核。" : "预算未超过系统测算标准。");
        return result;
    }

    private BigDecimal hotelLimit(String staffLevel, String travelType) {
        BigDecimal base = "一类".equals(staffLevel) ? BigDecimal.valueOf(800)
                : ("二类".equals(staffLevel) ? BigDecimal.valueOf(650) : BigDecimal.valueOf(500));
        return "教学科研业务".equals(travelType) ? base.multiply(BigDecimal.valueOf(1.8)) : base;
    }

    private boolean isSpecialArea(String destination) {
        return destination != null && (destination.contains("西藏") || destination.contains("青海") || destination.contains("新疆"));
    }
}
