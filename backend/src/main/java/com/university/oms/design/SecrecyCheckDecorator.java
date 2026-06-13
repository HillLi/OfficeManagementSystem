package com.university.oms.design;

import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;

// 装饰器模式：保密检查装饰器，涉密公文禁止调用外部AI服务
public class SecrecyCheckDecorator implements DocumentProcessor {
    private final DocumentProcessor wrapped; // 被装饰的内部处理器

    // 注入被装饰的处理器
    public SecrecyCheckDecorator(DocumentProcessor wrapped) {
        this.wrapped = wrapped;
    }

    // 检查密级，涉密公文直接拦截并返回警告，非密公文交由内部处理器处理
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

    // 判断密级是否属于受限制级别（内部/秘密/机密/绝密）
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
