package com.university.oms;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.DocumentRequest;
import com.university.oms.dto.MeetingRequest;
import com.university.oms.dto.SealApplyRequest;
import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;
import com.university.oms.service.DocumentService;
import com.university.oms.service.MeetingService;
import com.university.oms.service.SealService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OfficeManagementSystemIntegrationTest {
    @Autowired
    private DocumentService documentService;
    @Autowired
    private MeetingService meetingService;
    @Autowired
    private SealService sealService;

    @Test
    void secretDocumentBlocksExternalAiReview() {
        DocumentRequest request = new DocumentRequest();
        request.setTitle("关于涉密材料管理的通知");
        request.setDocType("通知");
        request.setSecrecyLevel("秘密");
        request.setContent("各单位：请做好涉密材料管理。");
        request.setApplicantId(2L);

        Document document = documentService.create(request);
        AiReviewResult result = documentService.review(document.getId());

        assertFalse(result.isPassed());
        assertTrue(result.getIssues().get(0).contains("禁止调用外部AI"));
    }

    @Test
    void largeIndoorMeetingRequiresSecurityMaterials() {
        MeetingRequest request = new MeetingRequest();
        request.setTitle("大型活动");
        request.setRoomId(2L);
        request.setOrganizerId(2L);
        request.setStartTime(LocalDateTime.of(2026, 6, 1, 9, 0));
        request.setEndTime(LocalDateTime.of(2026, 6, 1, 11, 0));
        request.setExpectedCount(510);
        request.setVenueType("室内");
        request.setParticipants(java.util.Arrays.asList(2L, 3L));
        request.setRecorderId(2L);

        BusinessException ex = assertThrows(BusinessException.class, () -> meetingService.create(request));

        assertTrue(ex.getMessage().contains("大型活动"));
    }

    @Test
    void sealApplyCreatesDraftBeforeMaterialUpload() {
        SealApplyRequest request = new SealApplyRequest();
        request.setSealId(1L);
        request.setApplicantId(2L);
        request.setPurpose("测试用印");

        assertEquals("draft", sealService.apply(request).getStatus());
    }
}
