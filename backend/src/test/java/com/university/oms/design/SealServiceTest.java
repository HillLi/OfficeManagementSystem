package com.university.oms.design;

import com.university.oms.dto.SealApplyRequest;
import com.university.oms.model.SealApplication;
import com.university.oms.service.SealService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SealServiceTest {
    @Autowired
    private SealService service;

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
        req.setExpectedReturnTime(LocalDateTime.now().plusDays(2));

        SealApplication app = service.apply(req);
        assertNotNull(app.getReturnDeadline(), "外带申请应设置归还截止日期");
    }
}
