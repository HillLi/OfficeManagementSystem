package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.ReportRequest;
import com.university.oms.dto.ReportReplyRequest;
import com.university.oms.model.Report;
import com.university.oms.model.User;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.security.AuthContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {
    private final InMemoryDatabase db;
    private final ApprovalService approvalService;
    private final DataPersistence persistence;
    private final WorkflowService workflowService;
    private final BusinessAccessService accessService;

    public ReportService(InMemoryDatabase db, ApprovalService approvalService, DataPersistence persistence,
                         WorkflowService workflowService, BusinessAccessService accessService) {
        this.db = db;
        this.approvalService = approvalService;
        this.persistence = persistence;
        this.workflowService = workflowService;
        this.accessService = accessService;
    }

    public List<Report> list() {
        User user = AuthContext.currentUser();
        List<Report> reports = new ArrayList<Report>(db.reports().values());
        if (user == null || user.getRoleKeys().contains("admin") || user.getRoleKeys().contains("office_admin")
                || user.getRoleKeys().contains("school_leader")) {
            return reports;
        }
        List<Report> scoped = new ArrayList<Report>();
        for (Report report : reports) {
            if (report.getApplicantId().equals(user.getId())
                    || (user.getRoleKeys().contains("dept_head") && report.getDeptId().equals(user.getDeptId()))) {
                scoped.add(report);
            }
        }
        return scoped;
    }

    public Report create(ReportRequest request) {
        Long applicantId = AuthContext.currentUserIdOr(request.getApplicantId());
        User applicant = db.users().get(applicantId);
        if (applicant == null) {
            throw new BusinessException("用户不存在");
        }
        Report report = new Report();
        db.fill(report, db.nextId());
        report.setTitle(request.getTitle());
        report.setType(request.getType());
        report.setSecrecyLevel(request.getSecrecyLevel());
        report.setContent(request.getContent());
        report.setApplicantId(applicantId);
        report.setDeptId(applicant.getDeptId());
        report.setStatus("pending_secret_review");
        db.reports().put(report.getId(), report);
        persistence.saveReport(report);
        approvalService.record("report", report.getId(), applicantId, "submit", "提交请示报告");
        workflowService.startFlow("report", report.getId(), report.getStatus(), applicantId);
        return report;
    }

    public Report reply(Long id, ReportReplyRequest request) {
        Report report = db.reports().get(id);
        if (report == null) {
            throw new BusinessException("请示报告不存在");
        }
        accessService.requireReportReply(report);
        if (!"approved".equals(report.getStatus())) {
            throw new BusinessException("只有审批通过的请示报告可以批复归档");
        }
        report.setReply(request.getReply());
        report.setStatus("archived");
        report.setUpdatedAt(java.time.LocalDateTime.now());
        persistence.saveReport(report);
        approvalService.record("report", id, AuthContext.currentUserIdOr(report.getApplicantId()), "reply", "批复归档");
        workflowService.advanceFlow("report", id, "approved", "archived", report.getApplicantId());
        return report;
    }
}
