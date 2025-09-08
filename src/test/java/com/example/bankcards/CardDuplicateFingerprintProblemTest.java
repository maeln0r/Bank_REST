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

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CardDuplicateFingerprintProblemTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    TestDataHelper data;
    JsonSupport json;

    String adminToken;

    @BeforeEach
    void init() throws Exception {
        json = new JsonSupport(om);
        data.ensureAdmin("adminD", "adminD@test.local", "Admin123!");
        adminToken = json.loginToken(mvc, "adminD", "Admin123!");
    }

    @Test
    @DisplayName("Create card duplicate (same owner + PAN) → 4xx with problem+json")
    void duplicateFingerprint() throws Exception {
        String exp = YearMonth.now().plusMonths(6).toString();
        var owner = data.ensureUser("dup2", "dup2@test.local", "U123!");
        var body = Map.of(
                "ownerId", owner.getId(),
                "expiry", exp,
                "pan", "4000000000000002",
                "initialBalance", new BigDecimal("10.00")
        );
        mvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated());

        var res = mvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn();

        JsonNode j = om.readTree(res.getResponse().getContentAsByteArray());
        assertThat(j.get("errors").isArray()).isTrue();
    }
}