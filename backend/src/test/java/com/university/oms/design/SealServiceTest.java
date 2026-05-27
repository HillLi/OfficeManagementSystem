package com.university.oms.design;

import com.university.oms.dto.SealApplyRequest;
import com.university.oms.model.SealApplication;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.repository.NoopDataPersistence;
import com.university.oms.service.ApprovalService;
import com.university.oms.service.AttachmentStorageService;
import com.university.oms.service.BusinessAccessService;
import com.university.oms.service.DictionaryService;
import com.university.oms.service.SealService;
import com.university.oms.service.WorkflowService;
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
        ApprovalFlowConfig flowConfig = new ApprovalFlowConfig();
        flowConfig.init();
        BusinessAccessService accessService = new BusinessAccessService(db);
        DictionaryService dictionaryService = new DictionaryService(db, new NoopDataPersistence());
        WorkflowService workflowService = new WorkflowService(db, new NoopDataPersistence(), flowConfig, accessService,
                new AttachmentStorageService("target/test-uploads/seal-service"), dictionaryService);
        service = new SealService(db,
                new ApprovalService(db, new NoopDataPersistence(),
                        flowConfig, new StateFactory(flowConfig),
                        new StatusChangeNotifier(new ArrayList<>()), workflowService, accessService),
                new NoopDataPersistence(), workflowService, dictionaryService);
    }

    @Test
    void applyWithoutMaterialCreatesDraft() {
        SealApplyRequest req = new SealApplyRequest();
        req.setSealId(1L);
        req.setApplicantId(2L);
        req.setPurpose("测试");

        SealApplication app = service.apply(req);
        assertEquals("draft", app.getStatus());
    }

    @Test
    void legacyMaterialUrlDoesNotSkipDraftSubmission() {
        SealApplyRequest req = new SealApplyRequest();
        req.setSealId(1L);
        req.setApplicantId(2L);
        req.setPurpose("测试用印");
        req.setMaterialUrl("/files/test.pdf");
        req.setCopies(2);

        SealApplication app = service.apply(req);
        assertNotNull(app);
        assertEquals("draft", app.getStatus());
    }

    @Test
    void takeOutSetsReturnDeadline() {
        SealApplyRequest req = new SealApplyRequest();
        req.setSealId(2L);
        req.setApplicantId(2L);
        req.setPurpose("外带测试");
        req.setMaterialUrl("/files/test.pdf");
        req.setTakeOut(true);
        req.setTakeOutReason("外出签署");
        req.setTakeOutLocation("合作单位");
        req.setSupervisorId(8L);
        req.setExpectedReturnTime(java.time.LocalDateTime.now().plusDays(2));

        SealApplication app = service.apply(req);
        assertNotNull(app.getReturnDeadline(), "外带申请应设置归还截止日期");
    }
}
