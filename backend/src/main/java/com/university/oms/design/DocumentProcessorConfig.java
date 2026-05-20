package com.university.oms.design;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assembles the DocumentProcessor decorator chain.
 */
@Configuration
public class DocumentProcessorConfig {
    @Bean
    public DocumentProcessor documentProcessor() {
        return new AiReviewDecorator(new SecrecyCheckDecorator(new BaseDocumentProcessor()));
    }
}
