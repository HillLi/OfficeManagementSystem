package com.university.oms.design;

import com.university.oms.model.AiReviewResult;
import com.university.oms.model.Document;

public interface DocumentReviewStrategy {
    boolean supports(String docType);
    AiReviewResult review(Document document);
}
