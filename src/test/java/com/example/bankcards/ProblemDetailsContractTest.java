package com.example.bankcards;

import com.example.bankcards.testutil.TestDataHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProblemDetailsContractTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    TestDataHelper data;

    String adminToken;
    String userToken;

    @BeforeEach
    void init() throws Exception {
        data.ensureAdmin("adminP", "adminP@test.local", "Admin123!");
        adminToken = new com.example.bankcards.testutil.JsonSupport(om).loginToken(mvc, "adminP", "Admin123!");
        data.ensureUser("puser", "puser@test.local", "User123!");
        userToken = new com.example.bankcards.testutil.JsonSupport(om).loginToken(mvc, "puser", "User123!");
    }

    @Test
    @DisplayName("problem+json: invalid Luhn → 422 with code=error.card.pan.luhn")
    void problemForInvalidLuhn() throws Exception {
        String exp = YearMonth.now().plusMonths(6).toString();
        var bad = Map.of(
                "ownerId", data.ensureUser("ownerP", "ownerP@test.local", "U123!").getId(),
                "expiry", exp,
                "pan", "4111111111111112",
                "initialBalance", new BigDecimal("10.00")
        );
        var res = mvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn();
        JsonNode j = om.readTree(res.getResponse().getContentAsByteArray());
        assertThat(j.get("status").asInt()).isEqualTo(422);
        assertThat(j.get("code").asText()).isEqualTo("error.card.pan.luhn");
        assertThat(j.get("errors").isArray()).isTrue();
        assertThat(j.get("errors").get(0).get("field").asText()).isEqualTo("pan");
    }

    @Test
    @DisplayName("problem+json: wrong current password → 422 with code=error.password.current_invalid")
    void problemForWrongCurrentPassword() throws Exception {
        var bad = Map.of(
                "currentPassword", "WRONG",
                "newPassword", "NewPass1!"
        );
        var res = mvc.perform(post("/api/users/me/password")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn();
        JsonNode j = om.readTree(res.getResponse().getContentAsByteArray());
        assertThat(j.get("status").asInt()).isEqualTo(422);
        assertThat(j.get("code").asText()).isEqualTo("error.password.current_invalid");
        assertThat(j.get("errors").get(0).get("field").asText()).isEqualTo("currentPassword");
    }
}