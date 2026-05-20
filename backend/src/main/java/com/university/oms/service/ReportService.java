package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.ReportRequest;
import com.university.oms.model.Report;
import com.university.oms.model.User;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {
    private final InMemoryDatabase db;
    private final ApprovalService approvalService;
    private final DataPersistence persistence;

    public ReportService(InMemoryDatabase db, ApprovalService approvalService, DataPersistence persistence) {
        this.db = db;
        this.approvalService = approvalService;
        this.persistence = persistence;
    }

    public List<Report> list() {
        return new ArrayList<Report>(db.reports().values());
    }

    public Report create(ReportRequest request) {
        User applicant = db.users().get(request.getApplicantId());
        if (applicant == null) {
            throw new BusinessException("用户不存在");
        }
        Report report = new Report();
        db.fill(report, db.nextId());
        report.setTitle(request.getTitle());
        report.setType(request.getType());
        report.setSecrecyLevel(request.getSecrecyLevel());
        report.setContent(request.getContent());
        report.setApplicantId(request.getApplicantId());
        report.setDeptId(applicant.getDeptId());
        report.setStatus("pending_secret_review");
        db.reports().put(report.getId(), report);
        persistence.saveReport(report);
        approvalService.record("report", report.getId(), request.getApplicantId(), "submit", "提交请示报告");
        return report;
    }
}
