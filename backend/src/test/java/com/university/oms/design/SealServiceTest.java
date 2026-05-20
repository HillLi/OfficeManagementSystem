package com.university.oms.design;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.SealApplyRequest;
import com.university.oms.model.SealApplication;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.repository.NoopDataPersistence;
import com.university.oms.service.ApprovalService;
import com.university.oms.service.SealService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SealServiceTest {
    private InMemoryDatabase db;
    private SealService service;

    @BeforeEach
    void setUp() {
        db = new InMemoryDatabase();
        db.init();
        service = new SealService(db,
                new ApprovalService(db, new NoopDataPersistence(),
                        new ApprovalFlowConfig(), new StateFactory(new ApprovalFlowConfig()),
                        new StatusChangeNotifier(new ArrayList<>())),
                new NoopDataPersistence());
    }

    @Test
    void applySealMissingMaterialThrows() {
        SealApplyRequest req = new SealApplyRequest();
        req.setSealId(1L);
        req.setApplicantId(2L);
        req.setPurpose("测试");
        req.setMaterialUrl("");

        assertThrows(BusinessException.class, () -> service.apply(req));
    }

    @Test
    void applyWithMaterialSucceeds() {
        SealApplyRequest req = new SealApplyRequest();
        req.setSealId(1L);
        req.setApplicantId(2L);
        req.setPurpose("测试用印");
        req.setMaterialUrl("/files/test.pdf");
        req.setCopies(2);

        SealApplication app = service.apply(req);
        assertNotNull(app);
        assertEquals("pending_office", app.getStatus());
    }

    @Test
    void takeOutSetsReturnDeadline() {
        SealApplyRequest req = new SealApplyRequest();
        req.setSealId(2L);
        req.setApplicantId(2L);
        req.setPurpose("外带测试");
        req.setMaterialUrl("/files/test.pdf");
        req.setTakeOut(true);

        SealApplication app = service.apply(req);
        assertNotNull(app.getReturnDeadline(), "外带申请应设置归还截止日期");
    }
}
