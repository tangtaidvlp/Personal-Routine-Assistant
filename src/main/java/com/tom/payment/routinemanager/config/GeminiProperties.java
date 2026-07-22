package com.tom.payment.routinemanager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct; 

@Component
@ConfigurationProperties(prefix = "spring.ai.google.genai")
public class GeminiProperties {

    private static final Logger log = LoggerFactory.getLogger(GeminiProperties.class);

    // Spring Boot's relaxed binding automatically maps 'api-key' from YAML to 'apiKey' here
    private String apiKey;

    // A standard setter is REQUIRED for @ConfigurationProperties to work
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    @PostConstruct
    public void logPropertyOnStartup() {
        // SECURITY WARNING: Logging raw API keys in production is highly discouraged.
        // It is best practice to mask the key so it doesn't leak into your log management systems.
        
        if (apiKey != null && apiKey.length() > 10) {
            String maskedKey = apiKey.substring(0, 6) + "*".repeat(apiKey.length() - 15) + apiKey.substring(apiKey.length() - 10);
            log.info("✅✅✅✅✅✅✅✅✅✅✅ Gemini API Key loaded successfully. Value: {}", maskedKey);
            
            // If you strictly need the raw key printed to standard out for local debugging:
            // System.out.println("Raw Gemini API Key: " + apiKey);
        } else {
            log.error("❌❌❌❌❌❌❌❌❌❌❌ Gemini API Key is missing or invalid!");
        }
    }
}