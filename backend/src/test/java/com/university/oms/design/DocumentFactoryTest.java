package com.university.oms.design;

import com.university.oms.dto.DocumentRequest;
import com.university.oms.model.Document;
import com.university.oms.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentFactoryTest {
    @Test
    void createSetsDocNoFormat() {
        DocumentRequest request = new DocumentRequest();
        request.setTitle("关于测试的通知");
        request.setDocType("通知");
        request.setContent("各单位：测试内容。");
        request.setApplicantId(1L);

        User user = new User();
        user.setId(1L);
        user.setDeptId(4L);

        Document doc = new DocumentFactory().create(1001L, request, user);

        assertTrue(doc.getDocNo().contains("〔"), "文号应包含〔");
        assertTrue(doc.getDocNo().contains("号"), "文号应以号结尾");
        assertTrue(doc.getDocNo().contains("1001"), "文号应包含ID");
    }

    @Test
    void createSetsStatusDraft() {
        DocumentRequest request = new DocumentRequest();
        request.setTitle("测试标题");
        request.setDocType("通知");
        request.setContent("正文内容");
        request.setApplicantId(1L);

        User user = new User();
        user.setId(1L);
        user.setDeptId(4L);

        Document doc = new DocumentFactory().create(1L, request, user);

        assertEquals("draft", doc.getStatus());
    }

    @Test
    void createSetsApplicantFields() {
        DocumentRequest request = new DocumentRequest();
        request.setTitle("测试");
        request.setDocType("报告");
        request.setContent("正文");
        request.setApplicantId(5L);

        User user = new User();
        user.setId(5L);
        user.setDeptId(2L);

        Document doc = new DocumentFactory().create(1L, request, user);

        assertEquals(5L, doc.getApplicantId());
        assertEquals(2L, doc.getDeptId());
    }
}
