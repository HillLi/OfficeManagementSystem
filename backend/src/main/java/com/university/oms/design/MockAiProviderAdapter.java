package com.university.oms.design;

import java.time.LocalDate;

// 适配器模式：模拟AI服务提供者，用于测试环境或作为后备实现
public class MockAiProviderAdapter implements AiProviderAdapter {
    // 根据公文类型和主题生成模拟公文草稿
    @Override
    public String draft(String docType, String topic, String keyPoints) {
        String points = keyPoints == null || keyPoints.trim().isEmpty() ? "请结合学校实际推进落实。" : keyPoints;
        return "关于" + topic + "的" + docType + "\n\n"
                + "各单位：\n"
                + "为规范推进" + topic + "相关工作，根据学校办公管理要求，现将有关事项" + docType + "如下：\n"
                + "一、工作目标\n" + points + "\n"
                + "二、工作要求\n请各单位结合实际认真落实，按时反馈办理情况。\n\n"
                + "北京大学\n" + LocalDate.now();
    }
}
