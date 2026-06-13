package com.university.oms.design;

import com.university.oms.nlp.DocumentSimilarityService;
import com.university.oms.nlp.NlpQualityAnalysisDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 装饰器模式：公文处理器装饰链配置，组装 NLP质量分析 -> AI审核 -> 保密检查 -> 基础处理
@Configuration
public class DocumentProcessorConfig {

    private final DocumentSimilarityService similarityService;

    public DocumentProcessorConfig(DocumentSimilarityService similarityService) {
        this.similarityService = similarityService;
    }

    // 构建并返回装饰器链
    @Bean
    public DocumentProcessor documentProcessor() {
        return new NlpQualityAnalysisDecorator(
                new AiReviewDecorator(
                        new SecrecyCheckDecorator(
                                new BaseDocumentProcessor())),
                similarityService);
    }
}
