package com.university.oms.design;

import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// 策略模式：请示/报告类公文的专用审核策略
@Order(1)
@Component
public class ReportDocumentReviewStrategy extends DefaultDocumentReviewStrategy {
    // 仅支持请示和报告类公文
    @Override
    public boolean supports(String docType) {
        return "请示".equals(docType) || "报告".equals(docType);
    }

    // 在默认审核基础上增加请示类公文的专项检查
    @Override
    public AiReviewResult review(Document document) {
        AiReviewResult result = super.review(document);
        if ("请示".equals(document.getDocType()) && document.getContent() != null
                && !document.getContent().contains("请示事项")) {
            result.getSuggestions().add("请示类公文建议明确写出“请示事项”和期望批复内容。");
        }
        result.setPassed(result.getIssues().isEmpty());
        return result;
    }
}
