package com.futurepathway.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurepathway.config.GeminiProperties;
import com.futurepathway.exception.AiServiceException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Thin HTTP client for Gemini's {@code generateContent} endpoint.
 * Always requests {@code responseMimeType: application/json} and returns the
 * model's raw JSON text (markdown fences stripped).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiClient {

    private final WebClient geminiWebClient;
    private final GeminiProperties props;
    private final ObjectMapper objectMapper;

    public String generateJson(String prompt) {
        if (!props.isConfigured()) {
            throw new AiServiceException(
                    "Gemini API key is not configured. Set GEMINI_API_KEY to enable AI features.");
        }
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0.2));
        try {
            String raw = geminiWebClient.post()
                    .uri(uri -> uri.path("/v1beta/models/{model}:generateContent")
                            .queryParam("key", props.apiKey())
                            .build(props.model()))
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            resp -> resp.bodyToMono(String.class)
                                    .map(err -> new AiServiceException("Gemini API error: " + err)))
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(Math.max(props.timeoutSeconds(), 5)));
            if (raw == null || raw.isBlank()) {
                throw new AiServiceException("Gemini returned an empty response");
            }
            return extractText(raw);
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("Gemini call failed: " + e.getMessage(), e);
        }
    }

    /** Extracts candidates[0].content.parts[0].text and strips ```json fences. */
    String extractText(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty() || !parts.get(0).has("text")) {
                throw new AiServiceException("Gemini returned no text candidates: " + abbreviate(rawJson));
            }
            String text = parts.get(0).get("text").asText("").strip();
            if (text.startsWith("```")) {
                int firstNewline = text.indexOf('\n');
                int lastFence = text.lastIndexOf("```");
                if (firstNewline >= 0 && lastFence > firstNewline) {
                    text = text.substring(firstNewline + 1, lastFence).strip();
                }
            }
            if (text.isEmpty()) {
                throw new AiServiceException("Gemini returned empty text");
            }
            return text;
        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new AiServiceException("Could not parse Gemini response: " + e.getMessage(), e);
        }
    }

    private String abbreviate(String s) {
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }
}
