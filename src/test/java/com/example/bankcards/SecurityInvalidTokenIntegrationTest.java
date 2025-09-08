package com.example.bankcards;

import com.example.bankcards.testutil.TestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityInvalidTokenIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    TestDataHelper data;

    @BeforeEach
    void setUp() {
        data.ensureUser("secU", "secU@test.local", "U123!");
    }

    @Test
    @DisplayName("GET /api/users/me — invalid token → 4xx (401/403 depending on config)")
    void me_invalidToken() throws Exception {
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/users/me — no token → 403 (Anonymous) — в текущей конфигурации")
    void me_noToken() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }
}