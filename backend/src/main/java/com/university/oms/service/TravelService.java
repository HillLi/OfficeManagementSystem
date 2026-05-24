package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.design.TravelExpenseStrategy;
import com.university.oms.dto.TravelReimburseRequest;
import com.university.oms.dto.TravelRequest;
import com.university.oms.model.Travel;
import com.university.oms.model.User;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.security.AuthContext;
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
    private final WorkflowService workflowService;
    private final BusinessAccessService accessService;

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
                         DataPersistence persistence, WorkflowService workflowService, BusinessAccessService accessService) {
        this.db = db;
        this.approvalService = approvalService;
        this.expenseStrategy = expenseStrategy;
        this.persistence = persistence;
        this.workflowService = workflowService;
        this.accessService = accessService;
    }

    public List<Travel> list() {
        User user = AuthContext.currentUser();
        List<Travel> travels = new ArrayList<Travel>(db.travels().values());
        if (user == null || user.getRoleKeys().contains("admin") || user.getRoleKeys().contains("finance_staff")
                || user.getRoleKeys().contains("school_leader")) {
            return travels;
        }
        List<Travel> scoped = new ArrayList<Travel>();
        for (Travel travel : travels) {
            User applicant = db.users().get(travel.getApplicantId());
            if (travel.getApplicantId().equals(user.getId())
                    || (user.getRoleKeys().contains("dept_head") && applicant != null && user.getDeptId().equals(applicant.getDeptId()))) {
                scoped.add(travel);
            }
        }
        return scoped;
    }

    public Travel create(TravelRequest request) {
        Long applicantId = AuthContext.currentUserIdOr(request.getApplicantId());
        if (!db.users().containsKey(applicantId)) {
            throw new BusinessException("用户不存在");
        }
        Travel travel = new Travel();
        db.fill(travel, db.nextId());
        travel.setApplicantId(applicantId);
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
        approvalService.record("travel", travel.getId(), applicantId, "submit", "提交差旅申请");
        workflowService.startFlow("travel", travel.getId(), travel.getStatus(), applicantId);
        return travel;
    }

    public Travel reimburse(Long id, TravelReimburseRequest request) {
        Travel travel = db.travels().get(id);
        if (travel == null) {
            throw new BusinessException("差旅申请不存在");
        }
        accessService.requireTravelReimburse(travel);
        if (!"approved".equals(travel.getStatus())) {
            throw new BusinessException("只有审批通过的差旅可以提交报销");
        }
        travel.setActualExpense(request.getActualExpense());
        travel.setStatus("pending_finance");
        travel.setUpdatedAt(java.time.LocalDateTime.now());
        persistence.saveTravel(travel);
        if (request.getReceiptUrl() != null && !request.getReceiptUrl().trim().isEmpty()) {
            com.university.oms.dto.AttachmentRequest attachment = new com.university.oms.dto.AttachmentRequest();
            attachment.setBizType("travel");
            attachment.setBizId(id);
            attachment.setFileName("差旅报销凭证");
            attachment.setFileUrl(request.getReceiptUrl());
            workflowService.addAttachment(attachment);
        }
        approvalService.record("travel", id, AuthContext.currentUserIdOr(travel.getApplicantId()), "reimburse", "提交报销");
        workflowService.startFlow("travel", id, travel.getStatus(), travel.getApplicantId());
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
