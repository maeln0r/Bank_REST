package com.example.bankcards;

import com.example.bankcards.testutil.JsonSupport;
import com.example.bankcards.testutil.TestDataHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthRefreshNegativeIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    TestDataHelper data;
    JsonSupport json;

    @BeforeEach
    void setUp() {
        json = new JsonSupport(om);
        data.ensureUser("refU", "ref@test.local", "U123!");
    }

    @Test
    @DisplayName("/api/auth/refresh — unknown/invalid refresh token → 4xx")
    void refresh_invalid() throws Exception {
        var body = Map.of("refreshToken", "00000000-0000-0000-0000-000000000000");
        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().is4xxClientError());
    }
}