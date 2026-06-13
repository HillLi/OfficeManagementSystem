package com.university.oms.design;

import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;
import org.springframework.stereotype.Component;

// 策略模式：默认公文审核策略，适用于所有公文类型
@Component
public class DefaultDocumentReviewStrategy implements DocumentReviewStrategy {
    // 支持所有公文类型
    @Override
    public boolean supports(String docType) {
        return true;
    }

    // 执行通用的格式和内容审核
    @Override
    public AiReviewResult review(Document document) {
        AiReviewResult result = new AiReviewResult();
        if (document.getTitle() == null || !document.getTitle().startsWith("关于")) {
            result.getIssues().add("标题建议采用“关于XXX的" + document.getDocType() + "”格式。");
        }
        if (document.getContent() == null || document.getContent().length() < 30) {
            result.getIssues().add("正文内容较短，建议补充背景、事项和工作要求。");
        }
        if (document.getDocNo() == null || !document.getDocNo().contains("〔")) {
            result.getSuggestions().add("文号建议采用“校发〔2026〕XX号”等规范格式。");
        }
        if (document.getContent() != null && (document.getContent().contains("秘密") || document.getContent().contains("涉密"))) {
            result.setRecommendedSecrecyLevel("内部");
            result.getSuggestions().add("正文包含敏感词，建议至少按内部事项控制知悉范围。");
        } else {
            result.setRecommendedSecrecyLevel(document.getSecrecyLevel());
        }
        result.setPassed(result.getIssues().isEmpty());
        return result;
    }
}
