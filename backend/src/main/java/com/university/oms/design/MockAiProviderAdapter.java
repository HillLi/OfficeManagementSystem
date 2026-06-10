package com.university.oms.design;

import java.time.LocalDate;

/**
 * Mock AI provider - replaced by NlpAiProviderAdapter as the primary implementation.
 * Kept as a fallback for testing.
 */
public class MockAiProviderAdapter implements AiProviderAdapter {
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
