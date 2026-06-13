package com.university.oms.design;

import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;

// 装饰器模式：公文处理接口，支持链式装饰（保密检查 -> AI审核 -> NLP质量分析）
public interface DocumentProcessor {
    // 对公文执行处理并返回审核结果
    AiReviewResult process(Document document);
}
