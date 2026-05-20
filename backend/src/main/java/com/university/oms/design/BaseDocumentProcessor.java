package com.university.oms.design;

import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;

/**
 * Base component for Decorator pattern — basic format checks.
 */
public class BaseDocumentProcessor implements DocumentProcessor {
    @Override
    public AiReviewResult process(Document document) {
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
        result.setPassed(result.getIssues().isEmpty());
        return result;
    }
}
