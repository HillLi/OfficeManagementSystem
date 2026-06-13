package com.university.oms.design;

import java.time.LocalDate;

// 适配器模式：AI服务提供者的统一接口
public interface AiProviderAdapter {
    // 根据公文类型、主题和要点生成公文草稿
    String draft(String docType, String topic, String keyPoints);
}
