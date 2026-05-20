package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.SealApplyRequest;
import com.university.oms.model.Seal;
import com.university.oms.model.SealApplication;
import com.university.oms.repository.DataPersistence;
import com.university.oms.repository.InMemoryDatabase;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SealService {
    private final InMemoryDatabase db;
    private final ApprovalService approvalService;
    private final DataPersistence persistence;

    public SealService(InMemoryDatabase db, ApprovalService approvalService, DataPersistence persistence) {
        this.db = db;
        this.approvalService = approvalService;
        this.persistence = persistence;
    }

    public List<Seal> seals() {
        return new ArrayList<Seal>(db.seals().values());
    }

    public List<SealApplication> applications() {
        return new ArrayList<SealApplication>(db.sealApplications().values());
    }

    public SealApplication apply(SealApplyRequest request) {
        Seal seal = db.seals().get(request.getSealId());
        if (seal == null) {
            throw new BusinessException("印章不存在");
        }
        if (request.getMaterialUrl() == null || request.getMaterialUrl().trim().isEmpty()) {
            throw new BusinessException("用印材料不能为空，严禁在空白纸张上用印");
        }
        SealApplication application = new SealApplication();
        db.fill(application, db.nextId());
        application.setSealId(request.getSealId());
        application.setApplicantId(request.getApplicantId());
        application.setPurpose(request.getPurpose());
        application.setMaterialUrl(request.getMaterialUrl());
        application.setCopies(request.getCopies());
        application.setTakeOut(request.isTakeOut());
        application.setMatterLevel(request.getMatterLevel());
        application.setStatus(seal.getSealName().contains("北京大学") ? "pending_office" : "pending_dept");
        if (request.isTakeOut()) {
            application.setReturnDeadline(LocalDateTime.now().plusDays(7));
        }
        db.sealApplications().put(application.getId(), application);
        persistence.saveSealApplication(application);
        approvalService.record("seal", application.getId(), request.getApplicantId(), "submit", "提交用印申请");
        return application;
    }

    public SealApplication markUsed(Long id, Long keeperId) {
        SealApplication app = find(id);
        app.setUseTime(LocalDateTime.now());
        app.setStatus("used");
        Seal seal = db.seals().get(app.getSealId());
        seal.setStatus(app.isTakeOut() ? "lent" : "in_use");
        persistence.saveSealApplication(app);
        persistence.saveSeal(seal);
        approvalService.record("seal", id, keeperId, "use", "用印登记");
        return app;
    }

    public SealApplication markReturned(Long id, Long keeperId) {
        SealApplication app = find(id);
        app.setReturnTime(LocalDateTime.now());
        app.setStatus("returned");
        Seal seal = db.seals().get(app.getSealId());
        seal.setStatus("in_store");
        persistence.saveSealApplication(app);
        persistence.saveSeal(seal);
        if (app.getReturnDeadline() != null && LocalDateTime.now().isAfter(app.getReturnDeadline())) {
            approvalService.record("seal", id, keeperId, "overdue_return", "逾期归还（截止" + app.getReturnDeadline() + "）");
        } else {
            approvalService.record("seal", id, keeperId, "return", "归还确认");
        }
        return app;
    }

    private SealApplication find(Long id) {
        SealApplication app = db.sealApplications().get(id);
        if (app == null) {
            throw new BusinessException("用印申请不存在");
        }
        return app;
    }
}
