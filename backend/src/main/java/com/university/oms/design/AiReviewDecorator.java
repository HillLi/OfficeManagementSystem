package com.university.oms.design;

import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;

// 装饰器模式：在基础处理之后增加内容敏感词检测
public class AiReviewDecorator implements DocumentProcessor {
    private final DocumentProcessor wrapped; // 被装饰的内部处理器

    // 注入被装饰的处理器
    public AiReviewDecorator(DocumentProcessor wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public AiReviewResult process(Document document) {
        AiReviewResult result = wrapped.process(document);
        String content = document.getContent();
        if (content != null && (content.contains("秘密") || content.contains("涉密"))) {
            result.setRecommendedSecrecyLevel("内部");
            result.getSuggestions().add("正文包含敏感词，建议至少按内部事项控制知悉范围。");
        } else if (result.getRecommendedSecrecyLevel() == null) {
            result.setRecommendedSecrecyLevel(document.getSecrecyLevel());
        }
        result.setPassed(result.getIssues().isEmpty());
        return result;
    }
}
