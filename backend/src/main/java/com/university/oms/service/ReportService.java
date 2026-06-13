package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.ReportRequest;
import com.university.oms.dto.ReportReplyRequest;
import com.university.oms.model.Report;
import com.university.oms.model.User;
import com.university.oms.repository.OmsRepository;
import com.university.oms.security.AuthContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 请示报告服务，处理请示报告的创建、审批和批复归档
 */
@Service
public class ReportService {
    private final OmsRepository repo;
    private final ApprovalService approvalService;
    private final WorkflowService workflowService;
    private final BusinessAccessService accessService;
    private final DictionaryService dictionaryService;

    public ReportService(OmsRepository repo, ApprovalService approvalService,
                         WorkflowService workflowService, BusinessAccessService accessService,
                         DictionaryService dictionaryService) {
        this.repo = repo;
        this.approvalService = approvalService;
        this.workflowService = workflowService;
        this.accessService = accessService;
        this.dictionaryService = dictionaryService;
    }

    /** 获取当前用户可见的请示报告列表（根据角色过滤） */
    public List<Report> list() {
        User user = AuthContext.currentUser();
        List<Report> reports = repo.findAllReports();
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

    /** 创建请示报告，进入保密审查阶段 */
    public Report create(ReportRequest request) {
        dictionaryService.requireEnabled("report_type", request.getType(), "请示报告类型");
        dictionaryService.requireEnabled("secrecy_level", request.getSecrecyLevel(), "密级");
        Long applicantId = AuthContext.currentUserIdOr(request.getApplicantId());
        User applicant = repo.findUserById(applicantId);
        if (applicant == null) {
            throw new BusinessException("用户不存在");
        }
        Report report = new Report();
        OmsRepository.fillEntity(report, repo.nextId());
        report.setTitle(request.getTitle());
        report.setType(request.getType());
        report.setSecrecyLevel(request.getSecrecyLevel());
        report.setContent(request.getContent());
        report.setApplicantId(applicantId);
        report.setDeptId(applicant.getDeptId());
        report.setStatus("pending_secret_review");
        repo.saveReport(report);
        approvalService.record("report", report.getId(), applicantId, "submit", "提交请示报告");
        workflowService.startFlow("report", report.getId(), report.getStatus(), applicantId);
        return report;
    }

    /** 批复归档请示报告（仅审批通过后可操作） */
    public Report reply(Long id, ReportReplyRequest request) {
        Report report = repo.findReportById(id);
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
        repo.saveReport(report);
        approvalService.record("report", id, AuthContext.currentUserIdOr(report.getApplicantId()), "reply", "批复归档");
        workflowService.advanceFlow("report", id, "approved", "archived", report.getApplicantId());
        return report;
    }
}
