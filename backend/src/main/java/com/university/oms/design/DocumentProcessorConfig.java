package com.university.oms.design;

import com.university.oms.nlp.DocumentSimilarityService;
import com.university.oms.nlp.NlpQualityAnalysisDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assembles the DocumentProcessor decorator chain.
 * Chain: NlpQualityAnalysisDecorator -> AiReviewDecorator -> SecrecyCheckDecorator -> BaseDocumentProcessor
 */
@Configuration
public class DocumentProcessorConfig {

    private final DocumentSimilarityService similarityService;

    public DocumentProcessorConfig(DocumentSimilarityService similarityService) {
        this.similarityService = similarityService;
    }

    @Bean
    public DocumentProcessor documentProcessor() {
        return new NlpQualityAnalysisDecorator(
                new AiReviewDecorator(
                        new SecrecyCheckDecorator(
                                new BaseDocumentProcessor())),
                similarityService);
    }
}
