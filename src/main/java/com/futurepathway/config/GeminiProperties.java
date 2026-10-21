package com.futurepathway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String baseUrl,
        long timeoutSeconds) {

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
