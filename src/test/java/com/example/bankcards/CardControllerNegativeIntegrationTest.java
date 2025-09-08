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

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CardControllerNegativeIntegrationTest extends AbstractIntegrationTest {

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
        data.ensureAdmin("adminC", "adminC@test.local", "Admin123!");
        adminToken = json.loginToken(mvc, "adminC", "Admin123!");
    }

    @Test
    @DisplayName("Create card — invalid PAN length → 400; invalid Luhn → 422")
    void create_invalidPan() throws Exception {
        String exp = YearMonth.now().plusMonths(6).toString();
        var badLen = Map.of(
                "ownerId", data.ensureUser("u1", "u1@test.local", "U123!").getId(),
                "expiry", exp,
                "pan", "123",
                "initialBalance", new BigDecimal("10.00")
        );
        mvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(badLen)))
                .andExpect(status().isBadRequest());

        var badLuhn = Map.of(
                "ownerId", data.ensureUser("u2", "u2@test.local", "U123!").getId(),
                "expiry", exp,
                "pan", "4111111111111112",
                "initialBalance", new BigDecimal("10.00")
        );
        mvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(badLuhn)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("Create card — duplicate PAN fingerprint for same owner → 4xx")
    void create_duplicatePanFingerprint() throws Exception {
        String exp = YearMonth.now().plusMonths(6).toString();
        var owner = data.ensureUser("dup", "dup@test.local", "U123!");
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
        mvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().is4xxClientError());
    }
}