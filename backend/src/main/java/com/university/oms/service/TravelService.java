package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.design.TravelExpenseStrategy;
import com.university.oms.dto.TravelRequest;
import com.university.oms.model.Travel;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TravelService {
    private final InMemoryDatabase db;
    private final ApprovalService approvalService;
    private final TravelExpenseStrategy expenseStrategy;
    private final DataPersistence persistence;

    private static final Map<String, List<String>> ALLOWED_TRANSPORT = new HashMap<>();
    static {
        ALLOWED_TRANSPORT.put("一类", Arrays.asList("飞机", "高铁一等座", "高铁二等座", "火车软卧"));
        ALLOWED_TRANSPORT.put("二类", Arrays.asList("高铁一等座", "高铁二等座", "火车软卧", "火车硬卧"));
        ALLOWED_TRANSPORT.put("三类", Arrays.asList("高铁二等座", "火车硬卧", "火车硬座"));
        ALLOWED_TRANSPORT.put("level1", ALLOWED_TRANSPORT.get("一类"));
        ALLOWED_TRANSPORT.put("level2", ALLOWED_TRANSPORT.get("二类"));
        ALLOWED_TRANSPORT.put("level3", ALLOWED_TRANSPORT.get("三类"));
    }

    public TravelService(InMemoryDatabase db, ApprovalService approvalService, TravelExpenseStrategy expenseStrategy,
                         DataPersistence persistence) {
        this.db = db;
        this.approvalService = approvalService;
        this.expenseStrategy = expenseStrategy;
        this.persistence = persistence;
    }

    public List<Travel> list() {
        return new ArrayList<Travel>(db.travels().values());
    }

    public Travel create(TravelRequest request) {
        if (!db.users().containsKey(request.getApplicantId())) {
            throw new BusinessException("用户不存在");
        }
        Travel travel = new Travel();
        db.fill(travel, db.nextId());
        travel.setApplicantId(request.getApplicantId());
        travel.setDestination(request.getDestination());
        travel.setStartDate(request.getStartDate());
        travel.setEndDate(request.getEndDate());
        travel.setReason(request.getReason());
        travel.setStaffLevel(request.getStaffLevel());
        travel.setTravelType(request.getTravelType());
        travel.setTransport(request.getTransport());
        travel.setBudget(request.getBudget());
        travel.setActualExpense(request.getActualExpense());
        travel.setStatus("pending_dept");
        validateTransport(travel);
        travel.setCheckResult(expenseStrategy.check(travel));
        db.travels().put(travel.getId(), travel);
        persistence.saveTravel(travel);
        approvalService.record("travel", travel.getId(), request.getApplicantId(), "submit", "提交差旅申请");
        return travel;
    }

    private void validateTransport(Travel travel) {
        String transport = travel.getTransport();
        String staffLevel = travel.getStaffLevel();
        if (transport == null || transport.isEmpty()) {
            throw new BusinessException("交通工具不能为空");
        }
        if (staffLevel == null || staffLevel.isEmpty()) {
            throw new BusinessException("人员类别不能为空");
        }
        List<String> allowed = ALLOWED_TRANSPORT.get(staffLevel);
        if (allowed == null) {
            throw new BusinessException("未知人员类别：" + staffLevel);
        }
        if (!allowed.contains(transport)) {
            throw new BusinessException(staffLevel + "人员不允许乘坐" + transport);
        }
    }
}
