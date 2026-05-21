package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.design.*;
import com.university.oms.dto.ApprovalRequest;
import com.university.oms.model.*;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.security.AuthContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalService {
    private final InMemoryDatabase db;
    private final DataPersistence persistence;
    private final ApprovalFlowConfig flowConfig;
    private final StateFactory stateFactory;
    private final StatusChangeNotifier notifier;
    private final WorkflowService workflowService;

    public ApprovalService(InMemoryDatabase db, DataPersistence persistence,
                           ApprovalFlowConfig flowConfig, StateFactory stateFactory,
                           StatusChangeNotifier notifier, WorkflowService workflowService) {
        this.db = db;
        this.persistence = persistence;
        this.flowConfig = flowConfig;
        this.stateFactory = stateFactory;
        this.notifier = notifier;
        this.workflowService = workflowService;
    }

    public ApprovalRecord record(String bizType, Long bizId, Long operatorId, String action, String opinion) {
        ApprovalRecord record = new ApprovalRecord();
        db.fill(record, db.nextId());
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setOperatorId(operatorId);
        record.setAction(action);
        record.setOpinion(opinion);
        db.approvals().add(record);
        persistence.saveApproval(record);
        return record;
    }

    public List<ApprovalRecord> list(String bizType, Long bizId) {
        return db.approvals().stream()
                .filter(r -> bizType == null || r.getBizType().equals(bizType))
                .filter(r -> bizId == null || r.getBizId().equals(bizId))
                .collect(Collectors.toList());
    }

    public Object approve(String bizType, Long bizId, ApprovalRequest request) {
        Long operatorId = AuthContext.currentUserIdOr(request.getOperatorId());
        User operator = db.users().get(operatorId);
        if (operator == null) {
            throw new BusinessException("操作用户不存在");
        }

        BaseEntity entity = findEntity(bizType, bizId);
        String oldStatus = getStatus(entity);

        BusinessState state = stateFactory.getState(flowKey(bizType, entity, oldStatus), oldStatus);
        String newStatus;
        if ("reject".equals(request.getAction())) {
            newStatus = state.reject(operator);
        } else {
            newStatus = state.approve(operator);
        }

        setStatus(entity, newStatus, operatorId);
        record(bizType, bizId, operatorId, request.getAction(), request.getOpinion());
        notifier.notify(bizType, bizId, oldStatus, newStatus, operatorId);
        workflowService.advanceFlow(bizType, bizId, oldStatus, newStatus, applicantId(entity));

        return entity;
    }

    private BaseEntity findEntity(String bizType, Long bizId) {
        switch (bizType) {
            case "document":
                Document doc = db.documents().get(bizId);
                if (doc == null) throw new BusinessException("公文不存在");
                return doc;
            case "seal":
                SealApplication app = db.sealApplications().get(bizId);
                if (app == null) throw new BusinessException("用印申请不存在");
                return app;
            case "meeting":
                Meeting meeting = db.meetings().get(bizId);
                if (meeting == null) throw new BusinessException("会议不存在");
                return meeting;
            case "travel":
                Travel travel = db.travels().get(bizId);
                if (travel == null) throw new BusinessException("差旅申请不存在");
                return travel;
            case "report":
                Report report = db.reports().get(bizId);
                if (report == null) throw new BusinessException("请示报告不存在");
                return report;
            default:
                throw new BusinessException("未知业务类型");
        }
    }

    private String flowKey(String bizType, BaseEntity entity, String currentStatus) {
        if ("meeting".equals(bizType) && (entity instanceof Meeting)
                && (((Meeting) entity).isLargeActivity() || "pending_security".equals(currentStatus))) {
            return "meeting_large";
        }
        if ("seal".equals(bizType)) {
            return "pending_office".equals(currentStatus) ? "seal_office" : "seal_dept";
        }
        return bizType;
    }

    private String getStatus(BaseEntity entity) {
        if (entity instanceof Document) return ((Document) entity).getStatus();
        if (entity instanceof SealApplication) return ((SealApplication) entity).getStatus();
        if (entity instanceof Meeting) return ((Meeting) entity).getStatus();
        if (entity instanceof Travel) return ((Travel) entity).getStatus();
        if (entity instanceof Report) return ((Report) entity).getStatus();
        return "unknown";
    }

    private Long applicantId(BaseEntity entity) {
        if (entity instanceof Document) return ((Document) entity).getApplicantId();
        if (entity instanceof SealApplication) return ((SealApplication) entity).getApplicantId();
        if (entity instanceof Meeting) return ((Meeting) entity).getOrganizerId();
        if (entity instanceof Travel) return ((Travel) entity).getApplicantId();
        if (entity instanceof Report) return ((Report) entity).getApplicantId();
        return null;
    }

    private void setStatus(BaseEntity entity, String status, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        if (entity instanceof Document) {
            Document doc = (Document) entity;
            doc.setStatus(status);
            doc.setUpdatedAt(now);
            persistence.saveDocument(doc);
        } else if (entity instanceof SealApplication) {
            SealApplication app = (SealApplication) entity;
            app.setStatus(status);
            app.setUpdatedAt(now);
            persistence.saveSealApplication(app);
        } else if (entity instanceof Meeting) {
            Meeting meeting = (Meeting) entity;
            meeting.setStatus(status);
            meeting.setUpdatedAt(now);
            persistence.saveMeeting(meeting);
        } else if (entity instanceof Travel) {
            Travel travel = (Travel) entity;
            travel.setStatus(status);
            travel.setUpdatedAt(now);
            persistence.saveTravel(travel);
        } else if (entity instanceof Report) {
            Report report = (Report) entity;
            report.setStatus(status);
            report.setUpdatedAt(now);
            persistence.saveReport(report);
        }
    }
}
