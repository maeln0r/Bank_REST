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

class ProblemDetailsBeanValidationContractTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    TestDataHelper data;

    String adminToken;

    @BeforeEach
    void init() throws Exception {
        data.ensureAdmin("adminBV", "adminBV@test.local", "Admin123!");
        adminToken = new com.example.bankcards.testutil.JsonSupport(om).loginToken(mvc, "adminBV", "Admin123!");
    }

    @Test
    @DisplayName("problem+json: PAN length invalid (Pattern) → 400 with code=error.validation & errors[0].code=Pattern")
    void problemForInvalidPanLength() throws Exception {
        String exp = YearMonth.now().plusMonths(6).toString();
        var bad = Map.of(
                "ownerId", data.ensureUser("bv", "bv@test.local", "U123!").getId(),
                "expiry", exp,
                "pan", "123",
                "initialBalance", new BigDecimal("10.00")
        );
        var res = mvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(om.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn();
        JsonNode j = om.readTree(res.getResponse().getContentAsByteArray());
        assertThat(j.get("code").asText()).isEqualTo("error.validation");
        assertThat(j.get("errors").get(0).get("field").asText()).isEqualTo("pan");
        assertThat(j.get("errors").get(0).get("code").asText()).isIn("Pattern", "NotBlank");
    }
}