package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.model.Travel;
import com.university.oms.model.TravelCheckResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

// 策略模式：标准差旅费用校验策略，按人员级别和地区类型核算费用标准
@Component
public class StandardTravelExpenseStrategy implements TravelExpenseStrategy {
    // 差旅费用标准表：按人员级别、业务类型、地区类型分类
    private static final Map<String, TravelStandard> STANDARDS = new HashMap<String, TravelStandard>();

    static {
        add("一类", "教学科研业务", "普通地区", "1440", "100", "80");
        add("二类", "教学科研业务", "普通地区", "1170", "100", "80");
        add("三类", "教学科研业务", "普通地区", "900", "100", "80");
        add("一类", "教学科研业务", "特殊地区", "1440", "120", "80");
        add("二类", "教学科研业务", "特殊地区", "1170", "120", "80");
        add("三类", "教学科研业务", "特殊地区", "900", "120", "80");
        add("一类", "其他业务", "普通地区", "800", "100", "80");
        add("二类", "其他业务", "普通地区", "650", "100", "80");
        add("三类", "其他业务", "普通地区", "500", "100", "80");
        add("一类", "其他业务", "特殊地区", "800", "120", "80");
        add("二类", "其他业务", "特殊地区", "650", "120", "80");
        add("三类", "其他业务", "特殊地区", "500", "120", "80");
    }

    // 根据差旅信息校验预算是否符合标准
    @Override
    public TravelCheckResult check(Travel travel) {
        if (travel.getEndDate().isBefore(travel.getStartDate())) {
            throw new BusinessException("返回日期不能早于出发日期");
        }
        long days = ChronoUnit.DAYS.between(travel.getStartDate(), travel.getEndDate()) + 1;
        TravelStandard standardRow = resolveStandard(travel);
        BigDecimal hotel = standardRow.hotelLimit.multiply(BigDecimal.valueOf(days));
        BigDecimal meal = standardRow.mealSubsidy.multiply(BigDecimal.valueOf(days));
        BigDecimal localTransport = standardRow.localTransportSubsidy.multiply(BigDecimal.valueOf(days));
        BigDecimal standard = hotel.add(meal).add(localTransport);
        BigDecimal budget = travel.getBudget() == null ? BigDecimal.ZERO : travel.getBudget();

        TravelCheckResult result = new TravelCheckResult();
        result.setStandardAmount(standard);
        result.setOverLimit(budget.compareTo(standard) > 0);
        result.getMessages().add("住宿、伙食补助、市内交通补助合计标准：" + standard + " 元。");
        result.getMessages().add(result.isOverLimit() ? "预算超过标准，请填写超标说明并提交财务复核。" : "预算未超过系统测算标准。");
        return result;
    }

    // 根据差旅信息解析对应的费用标准
    private TravelStandard resolveStandard(Travel travel) {
        String cityLevel = isSpecialArea(travel.getDestination()) ? "特殊地区" : "普通地区";
        String travelType = "教学科研业务".equals(travel.getTravelType()) ? "教学科研业务" : "其他业务";
        TravelStandard standard = STANDARDS.get(key(travel.getStaffLevel(), travelType, cityLevel));
        if (standard == null) {
            throw new BusinessException("未配置差旅标准：" + travel.getStaffLevel() + "/" + travel.getTravelType() + "/" + cityLevel);
        }
        return standard;
    }

    // 判断目的地是否属于特殊地区（西藏、青海、新疆）
    private boolean isSpecialArea(String destination) {
        return destination != null && (destination.contains("西藏") || destination.contains("青海") || destination.contains("新疆"));
    }

    private static void add(String staffLevel, String travelType, String cityLevel,
                            String hotelLimit, String mealSubsidy, String localTransportSubsidy) {
        STANDARDS.put(key(staffLevel, travelType, cityLevel),
                new TravelStandard(new BigDecimal(hotelLimit), new BigDecimal(mealSubsidy),
                        new BigDecimal(localTransportSubsidy)));
    }

    private static String key(String staffLevel, String travelType, String cityLevel) {
        return staffLevel + "|" + travelType + "|" + cityLevel;
    }

    // 差旅费用标准内部类
    private static class TravelStandard {
        private final BigDecimal hotelLimit; // 住宿标准上限
        private final BigDecimal mealSubsidy; // 伙食补助
        private final BigDecimal localTransportSubsidy; // 市内交通补助

        private TravelStandard(BigDecimal hotelLimit, BigDecimal mealSubsidy, BigDecimal localTransportSubsidy) {
            this.hotelLimit = hotelLimit;
            this.mealSubsidy = mealSubsidy;
            this.localTransportSubsidy = localTransportSubsidy;
        }
    }
}
