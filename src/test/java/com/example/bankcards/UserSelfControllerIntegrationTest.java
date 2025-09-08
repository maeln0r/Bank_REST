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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserSelfControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    TestDataHelper data;
    JsonSupport json;

    String token;

    @BeforeEach
    void setUp() throws Exception {
        json = new JsonSupport(om);
        data.ensureUser("selfUser", "self@example.com", "Self123!");
        token = json.loginToken(mvc, "selfUser", "Self123!");
    }

    @Test
    @DisplayName("GET /api/users/me — 200 for authenticated, 403 without token")
    void me_ok_and_unauthorized() throws Exception {
        var ok = mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode j = om.readTree(ok.getResponse().getContentAsByteArray());
        assertThat(j.get("username").asText()).isEqualTo("selfUser");
        assertThat(j.get("email").asText()).isEqualTo("self@example.com");

        mvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/users/me/password — 204 success; 422 on wrong current")
    void changePassword_success_and_wrongCurrent() throws Exception {
        var okBody = Map.of(
                "currentPassword", "Self123!",
                "newPassword", "Self456!"
        );
        mvc.perform(post("/api/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(okBody)))
                .andExpect(status().isNoContent());

        String newToken = json.loginToken(mvc, "selfUser", "Self456!");
        assertThat(newToken).isNotBlank();

        var badBody = Map.of(
                "currentPassword", "WRONG",
                "newPassword", "Self789!"
        );
        mvc.perform(post("/api/users/me/password")
                        .header("Authorization", "Bearer " + newToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(badBody)))
                .andExpect(status().isUnprocessableEntity());
    }
}