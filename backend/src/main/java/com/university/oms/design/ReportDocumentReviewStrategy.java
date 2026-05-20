package com.university.oms.design;

import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
public class ReportDocumentReviewStrategy extends DefaultDocumentReviewStrategy {
    @Override
    public boolean supports(String docType) {
        return "请示".equals(docType) || "报告".equals(docType);
    }

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
