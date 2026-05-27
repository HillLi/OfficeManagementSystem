package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.common.ForbiddenException;
import com.university.oms.dto.SealApplyRequest;
import com.university.oms.dto.SealTransferRequest;
import com.university.oms.model.Attachment;
import com.university.oms.model.Seal;
import com.university.oms.model.SealApplication;
import com.university.oms.model.SealTransfer;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.model.User;
import com.university.oms.security.AuthContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SealService {
    private final InMemoryDatabase db;
    private final ApprovalService approvalService;
    private final DataPersistence persistence;
    private final WorkflowService workflowService;
    private final DictionaryService dictionaryService;

    public SealService(InMemoryDatabase db, ApprovalService approvalService, DataPersistence persistence,
                       WorkflowService workflowService, DictionaryService dictionaryService) {
        this.db = db;
        this.approvalService = approvalService;
        this.persistence = persistence;
        this.workflowService = workflowService;
        this.dictionaryService = dictionaryService;
    }

    public List<Seal> seals() {
        return new ArrayList<Seal>(db.seals().values());
    }

    public List<SealApplication> applications() {
        User user = AuthContext.currentUser();
        List<SealApplication> apps = new ArrayList<SealApplication>(db.sealApplications().values());
        if (user == null || user.getRoleKeys().contains("admin") || user.getRoleKeys().contains("office_admin")
                || user.getRoleKeys().contains("school_leader") || user.getRoleKeys().contains("seal_keeper")) {
            enrichAll(apps);
            return apps;
        }
        List<SealApplication> scoped = new ArrayList<SealApplication>();
        for (SealApplication app : apps) {
            User applicant = db.users().get(app.getApplicantId());
            if (app.getApplicantId().equals(user.getId())
                    || (user.getRoleKeys().contains("dept_head") && applicant != null && user.getDeptId().equals(applicant.getDeptId()))) {
                scoped.add(app);
            }
        }
        enrichAll(scoped);
        return scoped;
    }

    public SealApplication apply(SealApplyRequest request) {
        dictionaryService.requireEnabled("matter_level", request.getMatterLevel(), "事项等级");
        Long applicantId = AuthContext.currentUserIdOr(request.getApplicantId());
        requireSeal(request.getSealId());
        if (request.isTakeOut() && (blank(request.getTakeOutReason()) || blank(request.getTakeOutLocation())
                || request.getSupervisorId() == null || request.getExpectedReturnTime() == null)) {
            throw new BusinessException("外带用印必须填写原因、地点、监交人和预计归还时间");
        }
        SealApplication application = new SealApplication();
        db.fill(application, db.nextId());
        application.setSealId(request.getSealId());
        application.setApplicantId(applicantId);
        application.setPurpose(request.getPurpose());
        application.setCopies(request.getCopies());
        application.setTakeOut(request.isTakeOut());
        application.setMatterLevel(request.getMatterLevel());
        application.setTakeOutReason(request.getTakeOutReason());
        application.setTakeOutLocation(request.getTakeOutLocation());
        application.setSupervisorId(request.getSupervisorId());
        application.setStatus("draft");
        if (request.isTakeOut()) {
            application.setReturnDeadline(request.getExpectedReturnTime());
        }
        db.sealApplications().put(application.getId(), application);
        persistence.saveSealApplication(application);
        return enrich(application);
    }

    public SealApplication submit(Long id) {
        SealApplication application = find(id);
        requireCurrentApplicantOrAdmin(application);
        if (activeMaterialCount(id) == 0) {
            throw new BusinessException("请至少上传一份有效用印材料后再提交");
        }
        Seal seal = requireSeal(application.getSealId());
        application.setStatus(seal.getSealName().contains("北京大学") ? "pending_office" : "pending_dept");
        application.setUpdatedAt(LocalDateTime.now());
        persistence.saveSealApplication(application);
        approvalService.record("seal", application.getId(), application.getApplicantId(), "submit", "提交用印申请");
        workflowService.startFlow("seal", application.getId(), application.getStatus(), application.getApplicantId());
        return enrich(application);
    }

    public SealApplication markUsed(Long id, Long keeperId) {
        Long operatorId = AuthContext.currentUserIdOr(keeperId);
        requireKeeper(operatorId);
        SealApplication app = find(id);
        app.setUseTime(LocalDateTime.now());
        app.setStatus("used");
        Seal seal = db.seals().get(app.getSealId());
        seal.setStatus(app.isTakeOut() ? "lent" : "in_use");
        persistence.saveSealApplication(app);
        persistence.saveSeal(seal);
        approvalService.record("seal", id, operatorId, "use", "用印登记");
        workflowService.audit("seal", "use", "seal", id, "用印登记");
        return app;
    }

    public SealTransfer transfer(SealTransferRequest request) {
        Long operatorId = AuthContext.currentUserIdOr(null);
        requireKeeper(operatorId);
        if (!db.seals().containsKey(request.getSealId())) {
            throw new BusinessException("印章不存在");
        }
        if (!db.users().containsKey(request.getReceiverId()) || !db.users().containsKey(request.getSupervisorId())) {
            throw new BusinessException("移交接收人或监交人不存在");
        }
        SealTransfer transfer = new SealTransfer();
        db.fill(transfer, db.nextId());
        transfer.setSealId(request.getSealId());
        transfer.setTransferorId(operatorId);
        transfer.setReceiverId(request.getReceiverId());
        transfer.setSupervisorId(request.getSupervisorId());
        transfer.setMaterialUrl(request.getMaterialUrl());
        transfer.setRemark(request.getRemark());
        transfer.setTransferTime(LocalDateTime.now());
        db.sealTransfers().put(transfer.getId(), transfer);
        persistence.saveSealTransfer(transfer);
        workflowService.audit("seal", "transfer", "seal", request.getSealId(), "移交记录#" + transfer.getId());
        return transfer;
    }

    public List<SealTransfer> transfers() {
        requireKeeper(AuthContext.currentUserIdOr(null));
        return new ArrayList<SealTransfer>(db.sealTransfers().values());
    }

    public SealApplication markReturned(Long id, Long keeperId) {
        Long operatorId = AuthContext.currentUserIdOr(keeperId);
        requireKeeper(operatorId);
        SealApplication app = find(id);
        String oldStatus = app.getStatus();
        app.setReturnTime(LocalDateTime.now());
        app.setStatus("returned");
        Seal seal = db.seals().get(app.getSealId());
        seal.setStatus("in_store");
        persistence.saveSealApplication(app);
        persistence.saveSeal(seal);
        if (app.getReturnDeadline() != null && LocalDateTime.now().isAfter(app.getReturnDeadline())) {
            approvalService.record("seal", id, operatorId, "overdue_return", "逾期归还（截止" + app.getReturnDeadline() + "）");
        } else {
            approvalService.record("seal", id, operatorId, "return", "归还确认");
        }
        workflowService.advanceFlow("seal", id, oldStatus, "returned", app.getApplicantId());
        return app;
    }

    private SealApplication find(Long id) {
        SealApplication app = db.sealApplications().get(id);
        if (app == null) {
            throw new BusinessException("用印申请不存在");
        }
        return app;
    }

    private Seal requireSeal(Long id) {
        Seal seal = db.seals().get(id);
        if (seal == null) {
            throw new BusinessException("印章不存在");
        }
        return seal;
    }

    private void requireCurrentApplicantOrAdmin(SealApplication application) {
        User user = AuthContext.requireUser();
        if (!application.getApplicantId().equals(user.getId()) && !user.getRoleKeys().contains("admin")) {
            throw new ForbiddenException("无权提交该用印申请");
        }
        if (!"draft".equals(application.getStatus())) {
            throw new BusinessException("只有草稿用印申请可以提交");
        }
    }

    private void enrichAll(List<SealApplication> applications) {
        for (SealApplication application : applications) {
            enrich(application);
        }
    }

    private SealApplication enrich(SealApplication application) {
        Seal seal = db.seals().get(application.getSealId());
        application.setSealName(seal == null ? "" : seal.getSealName());
        application.setMaterialCount(activeMaterialCount(application.getId()));
        return application;
    }

    private int activeMaterialCount(Long applicationId) {
        int count = 0;
        for (Attachment attachment : db.attachments()) {
            if ("seal".equals(attachment.getBizType()) && applicationId.equals(attachment.getBizId())
                    && !attachment.isDeleted()) {
                count++;
            }
        }
        return count;
    }

    private void requireKeeper(Long userId) {
        User user = db.users().get(userId);
        if (user == null || !(user.getRoleKeys().contains("seal_keeper") || user.getRoleKeys().contains("office_admin")
                || user.getRoleKeys().contains("admin"))) {
            throw new ForbiddenException("只有印章保管人或党办校办人员可以登记用印、归还和移交");
        }
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
