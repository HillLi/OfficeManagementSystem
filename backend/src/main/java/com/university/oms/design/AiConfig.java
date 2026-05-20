package com.university.oms.design;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Singleton pattern — AI service configuration.
 * Spring guarantees a single instance per application context.
 */
@Component
public class AiConfig {
    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    private String apiKey = "demo-key";
    private String modelName = "mock-ai";
    private String endpoint = "http://localhost:8080/mock-ai";

    @PostConstruct
    public void init() {
        log.info("AiConfig singleton initialized: model={}, endpoint={}", modelName, endpoint);
    }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
}
