package com.futurepathway.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * End-to-end test against a real PostgreSQL (Testcontainers):
 * Flyway migrations + registration + login + JWT-guarded access.
 * Requires Docker at test time.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private TestRestTemplate rest;

    @Test
    void registerLoginAndGuardedAccess() {
        Map<String, Object> reg = new HashMap<>();
        reg.put("name", "Asha");
        reg.put("email", "asha@example.com");
        reg.put("password", "Passw0rd1");
        reg.put("rollNo", "CSE2026001");
        reg.put("branch", "CSE");
        reg.put("batchYear", 2026);
        ResponseEntity<Map> registered =
                rest.postForEntity("/api/auth/register/student", reg, Map.class);
        assertEquals(HttpStatus.CREATED, registered.getStatusCode());
        String token = (String) registered.getBody().get("token");
        assertNotNull(token);

        ResponseEntity<Map> login = rest.postForEntity("/api/auth/login",
                Map.of("email", "asha@example.com", "password", "Passw0rd1"), Map.class);
        assertEquals(HttpStatus.OK, login.getStatusCode());
        assertNotNull(login.getBody().get("token"));

        ResponseEntity<String> anonymous = rest.getForEntity("/api/students/me", String.class);
        assertTrue(anonymous.getStatusCode().is4xxClientError());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map> me = rest.exchange("/api/students/me", HttpMethod.GET,
                new HttpEntity<>(headers), Map.class);
        assertEquals(HttpStatus.OK, me.getStatusCode());
        assertEquals("CSE2026001", me.getBody().get("rollNo"));
    }

    @Test
    void skillCatalogSeededAndAdminCanLogin() {
        ResponseEntity<Map> login = rest.postForEntity("/api/auth/login",
                Map.of("email", "admin@futurepathway.local", "password", "Admin@123"), Map.class);
        assertEquals(HttpStatus.OK, login.getStatusCode());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth((String) login.getBody().get("token"));
        ResponseEntity<Map> skills = rest.exchange("/api/skills?size=50", HttpMethod.GET,
                new HttpEntity<>(headers), Map.class);
        assertEquals(HttpStatus.OK, skills.getStatusCode());
        List<?> content = (List<?>) skills.getBody().get("content");
        assertTrue(content.size() >= 16, "V2 seed should provide 16 catalog skills");
    }
}
