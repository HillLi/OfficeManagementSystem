package com.university.oms.design;

import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentReviewStrategyTest {
    @Test
    void reviewWarnsWhenTitleIsNotStandard() {
        Document document = new Document();
        document.setTitle("系统试运行通知");
        document.setDocType("通知");
        document.setDocNo("校发〔2026〕1号");
        document.setSecrecyLevel("公开");
        document.setContent("各单位：为提高办公效率，现组织开展系统试运行工作，请按要求反馈使用情况。");

        AiReviewResult result = new DefaultDocumentReviewStrategy().review(document);

        assertFalse(result.isPassed());
        assertTrue(result.getIssues().get(0).contains("标题"));
    }

    @Test
    void reportStrategyAddsReportSpecificSuggestion() {
        Document document = new Document();
        document.setTitle("关于资源支持的请示");
        document.setDocType("请示");
        document.setDocNo("校发〔2026〕2号");
        document.setSecrecyLevel("内部");
        document.setContent("为保障系统试运行效果，拟申请服务器资源和测试账号支持。");

        AiReviewResult result = new ReportDocumentReviewStrategy().review(document);

        assertTrue(result.getSuggestions().stream().anyMatch(text -> text.contains("请示事项")));
    }
}
