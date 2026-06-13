package com.university.oms.design;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

// 单例模式：AI服务配置（由Spring保证单例）
@Component
public class AiConfig {
    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    private String apiKey = "demo-key"; // AI服务API密钥
    private String modelName = "mock-ai"; // AI模型名称
    private String endpoint = "http://localhost:8080/mock-ai"; // AI服务端点地址

    // 初始化时打印配置信息
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
