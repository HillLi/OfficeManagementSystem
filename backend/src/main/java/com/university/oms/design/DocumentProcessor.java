package com.university.oms.design;

import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;

/**
 * Decorator pattern — document processing interface.
 * Can be chained: BaseProcessor -> SecrecyCheckDecorator -> AiReviewDecorator.
 */
public interface DocumentProcessor {
    AiReviewResult process(Document document);
}
