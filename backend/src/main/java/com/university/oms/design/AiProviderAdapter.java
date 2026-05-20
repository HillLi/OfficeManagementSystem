package com.university.oms.design;

import java.time.LocalDate;

public interface AiProviderAdapter {
    String draft(String docType, String topic, String keyPoints);
}
