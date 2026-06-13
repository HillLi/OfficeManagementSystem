package com.university.oms.design;

import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;

// 策略模式：公文审核策略接口
public interface DocumentReviewStrategy {
    // 判断是否支持该公文类型
    boolean supports(String docType);
    // 对公文执行审核并返回结果
    AiReviewResult review(Document document);
}
