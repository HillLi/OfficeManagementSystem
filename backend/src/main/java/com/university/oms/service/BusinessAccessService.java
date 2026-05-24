package com.university.oms.service;

import com.university.oms.common.ForbiddenException;
import com.university.oms.model.Document;
import com.university.oms.model.DocumentDistribution;
import com.university.oms.model.Meeting;
import com.university.oms.model.Report;
import com.university.oms.model.SealApplication;
import com.university.oms.model.Travel;
import com.university.oms.model.User;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.security.AuthContext;
import org.springframework.stereotype.Service;

@Service
public class BusinessAccessService {
    private final InMemoryDatabase db;

    public BusinessAccessService(InMemoryDatabase db) {
        this.db = db;
    }

    public void requireDocumentSubmit(Document document) {
        User user = AuthContext.requireUser();
        if (!document.getApplicantId().equals(user.getId()) && !hasRole(user, "admin")) {
            deny("无权提交该公文");
        }
    }

    public void requireDocumentArchive(Document document) {
        User user = AuthContext.requireUser();
        if (!hasRole(user, "office_admin") && !hasRole(user, "admin")) {
            deny("无权归档该公文");
        }
    }

    public void requireDocumentDistribute(Document document) {
        requireDocumentArchive(document);
    }

    public void requireDocumentReceipt(DocumentDistribution distribution) {
        User user = AuthContext.requireUser();
        if (!distribution.getReceiverId().equals(user.getId()) && !hasRole(user, "admin")) {
            deny("无权签收该公文");
        }
    }

    public void requireDocumentRemind(Document document) {
        requireDocumentArchive(document);
    }

    public void requireMeetingMinutesArchive(Meeting meeting) {
        User user = AuthContext.requireUser();
        if (!meeting.getOrganizerId().equals(user.getId())
                && !hasRole(user, "office_admin")
                && !hasRole(user, "admin")) {
            deny("无权归档该会议纪要");
        }
    }

    public void requireReportReply(Report report) {
        User user = AuthContext.requireUser();
        if (!hasRole(user, "office_admin")
                && !hasRole(user, "school_leader")
                && !hasRole(user, "admin")) {
            deny("无权批复归档该请示报告");
        }
    }

    public void requireTravelReimburse(Travel travel) {
        User user = AuthContext.requireUser();
        if (!travel.getApplicantId().equals(user.getId()) && !hasRole(user, "admin")) {
            deny("无权提交该差旅报销");
        }
    }

    public void requireBusinessRead(String bizType, Long bizId) {
        if (!canReadBusiness(bizType, bizId)) {
            deny("无权访问该业务数据");
        }
    }

    public boolean canReadBusiness(String bizType, Long bizId) {
        User user = AuthContext.requireUser();
        if (hasRole(user, "admin")) {
            return true;
        }
        if ("document".equals(bizType)) {
            Document document = db.documents().get(bizId);
            return document != null && canReadForApplicant(user, document.getApplicantId(), document.getDeptId(),
                    hasRole(user, "office_admin") || hasRole(user, "school_leader"))
                    || isDocumentReceiver(user, bizId);
        }
        if ("meeting".equals(bizType)) {
            Meeting meeting = db.meetings().get(bizId);
            return meeting != null && canReadForApplicant(user, meeting.getOrganizerId(), departmentOf(meeting.getOrganizerId()),
                    hasRole(user, "office_admin") || hasRole(user, "school_leader") || hasRole(user, "security_staff"));
        }
        if ("seal".equals(bizType)) {
            SealApplication application = db.sealApplications().get(bizId);
            return application != null && canReadForApplicant(user, application.getApplicantId(), departmentOf(application.getApplicantId()),
                    hasRole(user, "office_admin") || hasRole(user, "seal_keeper"));
        }
        if ("travel".equals(bizType)) {
            Travel travel = db.travels().get(bizId);
            return travel != null && canReadForApplicant(user, travel.getApplicantId(), departmentOf(travel.getApplicantId()),
                    hasRole(user, "finance_staff") || hasRole(user, "school_leader"));
        }
        if ("report".equals(bizType)) {
            Report report = db.reports().get(bizId);
            return report != null && canReadForApplicant(user, report.getApplicantId(), report.getDeptId(),
                    hasRole(user, "office_admin") || hasRole(user, "school_leader"));
        }
        return false;
    }

    private boolean canReadForApplicant(User user, Long applicantId, Long deptId, boolean manager) {
        return user.getId().equals(applicantId)
                || manager
                || (hasRole(user, "dept_head") && deptId != null && deptId.equals(user.getDeptId()));
    }

    private Long departmentOf(Long userId) {
        User user = db.users().get(userId);
        return user == null ? null : user.getDeptId();
    }

    private boolean isDocumentReceiver(User user, Long documentId) {
        for (DocumentDistribution distribution : db.documentDistributions().values()) {
            if (documentId.equals(distribution.getDocumentId()) && user.getId().equals(distribution.getReceiverId())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRole(User user, String role) {
        return user.getRoleKeys().contains(role);
    }

    private void deny(String message) {
        throw new ForbiddenException(message);
    }
}
