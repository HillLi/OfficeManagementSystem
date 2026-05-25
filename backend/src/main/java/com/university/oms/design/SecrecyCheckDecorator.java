package com.university.oms.design;

import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;

/**
 * Decorator — blocks AI review for secret/confidential documents.
 */
public class SecrecyCheckDecorator implements DocumentProcessor {
    private final DocumentProcessor wrapped;

    public SecrecyCheckDecorator(DocumentProcessor wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public AiReviewResult process(Document document) {
        String level = document.getSecrecyLevel();
        if (restrictedLevel(level)) {
            AiReviewResult blocked = new AiReviewResult();
            blocked.setPassed(false);
            blocked.setRecommendedSecrecyLevel(level);
            blocked.getIssues().add("涉密材料禁止调用外部AI服务，请使用人工审核或本地模型。");
            return blocked;
        }
        return wrapped.process(document);
    }

    private boolean restrictedLevel(String level) {
        if (level == null) {
            return false;
        }
        String normalized = level.trim().toLowerCase();
        return "内部".equals(level) || "秘密".equals(level) || "机密".equals(level) || "绝密".equals(level)
                || "internal".equals(normalized) || "secret".equals(normalized)
                || "confidential".equals(normalized) || "top_secret".equals(normalized)
                || "top-secret".equals(normalized);
    }
}
