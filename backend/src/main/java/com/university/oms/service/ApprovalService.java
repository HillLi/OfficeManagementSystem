package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.design.*;
import com.university.oms.dto.ApprovalRequest;
import com.university.oms.model.*;
import com.university.oms.repository.OmsRepository;
import com.university.oms.security.AuthContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalService {
    private final OmsRepository repo;
    private final ApprovalFlowConfig flowConfig;
    private final StateFactory stateFactory;
    private final StatusChangeNotifier notifier;
    private final WorkflowService workflowService;
    private final BusinessAccessService accessService;

    public ApprovalService(OmsRepository repo,
                           ApprovalFlowConfig flowConfig, StateFactory stateFactory,
                           StatusChangeNotifier notifier, WorkflowService workflowService,
                           BusinessAccessService accessService) {
        this.repo = repo;
        this.flowConfig = flowConfig;
        this.stateFactory = stateFactory;
        this.notifier = notifier;
        this.workflowService = workflowService;
        this.accessService = accessService;
    }

    public ApprovalRecord record(String bizType, Long bizId, Long operatorId, String action, String opinion) {
        ApprovalRecord record = new ApprovalRecord();
        OmsRepository.fillEntity(record, repo.nextId());
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setOperatorId(operatorId);
        record.setAction(action);
        record.setOpinion(opinion);
        repo.saveApproval(record);
        return record;
    }

    public List<ApprovalRecord> list(String bizType, Long bizId) {
        if (bizType != null && bizId != null) {
            accessService.requireBusinessRead(bizType, bizId);
        }
        return repo.findAllApprovals().stream()
                .filter(r -> bizType == null || r.getBizType().equals(bizType))
                .filter(r -> bizId == null || r.getBizId().equals(bizId))
                .filter(r -> bizType != null && bizId != null || accessService.canReadBusiness(r.getBizType(), r.getBizId()))
                .collect(Collectors.toList());
    }

    public Object approve(String bizType, Long bizId, ApprovalRequest request) {
        Long operatorId = AuthContext.currentUserIdOr(request.getOperatorId());
        User operator = repo.findUserById(operatorId);
        if (operator == null) {
            throw new BusinessException("操作用户不存在");
        }

        BaseEntity entity = findEntity(bizType, bizId);
        String oldStatus = getStatus(entity);
        accessService.requireBusinessApproval(operator, bizType, bizId, oldStatus, flowConfig.getRequiredRole(oldStatus));

        BusinessState state = stateFactory.getState(flowKey(bizType, entity, oldStatus), oldStatus);
        String newStatus;
        if ("reject".equals(request.getAction())) {
            newStatus = state.reject(operator);
        } else {
            newStatus = state.approve(operator);
        }
        if (entity instanceof Travel && "approved".equals(newStatus)
                && ((Travel) entity).isReimbursementSubmitted()) {
            newStatus = "archived";
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
                Document doc = repo.findDocumentById(bizId);
                if (doc == null) throw new BusinessException("公文不存在");
                return doc;
            case "seal":
                SealApplication app = repo.findSealApplicationById(bizId);
                if (app == null) throw new BusinessException("用印申请不存在");
                return app;
            case "meeting":
                Meeting meeting = repo.findMeetingById(bizId);
                if (meeting == null) throw new BusinessException("会议不存在");
                return meeting;
            case "travel":
                Travel travel = repo.findTravelById(bizId);
                if (travel == null) throw new BusinessException("差旅申请不存在");
                return travel;
            case "report":
                Report report = repo.findReportById(bizId);
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
            SealApplication application = (SealApplication) entity;
            boolean major = "重大事项".equals(application.getMatterLevel());
            Seal seal = repo.findSealById(application.getSealId());
            boolean schoolSeal = seal != null && seal.getSealName().contains("北京大学");
            if (major && schoolSeal) {
                return "seal_school_major";
            }
            if (major) {
                return "seal_dept_major";
            }
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
            repo.saveDocument(doc);
        } else if (entity instanceof SealApplication) {
            SealApplication app = (SealApplication) entity;
            app.setStatus(status);
            app.setUpdatedAt(now);
            repo.saveSealApplication(app);
        } else if (entity instanceof Meeting) {
            Meeting meeting = (Meeting) entity;
            meeting.setStatus(status);
            meeting.setUpdatedAt(now);
            repo.saveMeeting(meeting);
        } else if (entity instanceof Travel) {
            Travel travel = (Travel) entity;
            travel.setStatus(status);
            travel.setUpdatedAt(now);
            repo.saveTravel(travel);
        } else if (entity instanceof Report) {
            Report report = (Report) entity;
            report.setStatus(status);
            report.setUpdatedAt(now);
            repo.saveReport(report);
        }
    }
}
