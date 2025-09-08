package com.example.bankcards;

import com.example.bankcards.testutil.JsonSupport;
import com.example.bankcards.testutil.TestDataHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

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
        data.ensureUser("user1", "user1@example.com", "Password123!");
    }

    @Test
    @DisplayName("/api/auth/login — success & token shape")
    void login_success() throws Exception {
        var reqBody = "{" +
                "\"usernameOrEmail\":\"user1\"," +
                "\"password\":\"Password123!\"}";

        var res = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(reqBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = om.readTree(res.getResponse().getContentAsByteArray());
        assertThat(jsonNode.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(jsonNode.get("accessToken").asText()).isNotBlank();
        assertThat(jsonNode.get("refreshToken").asText()).isNotBlank();
        assertThat(jsonNode.get("expiresInSeconds").asLong()).isGreaterThan(0);
    }

    @Test
    @DisplayName("/api/auth/login — 401 on bad credentials")
    void login_badCredentials() throws Exception {
        var reqBody = "{" +
                "\"usernameOrEmail\":\"user1\"," +
                "\"password\":\"WRONG\"}";
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(reqBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/api/auth/refresh — success")
    void refresh_success() throws Exception {
        var loginRes = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"usernameOrEmail\":\"user1\"," +
                                "\"password\":\"Password123!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode loginJson = om.readTree(loginRes.getResponse().getContentAsByteArray());
        String refresh = loginJson.get("refreshToken").asText();

        var refreshRes = mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode refJson = om.readTree(refreshRes.getResponse().getContentAsByteArray());
        assertThat(refJson.get("accessToken").asText()).isNotBlank();
    }
}