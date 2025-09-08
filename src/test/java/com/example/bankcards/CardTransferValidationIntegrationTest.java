package com.example.bankcards;

import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.entity.UserEntity;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CardTransferValidationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    TestDataHelper data;
    JsonSupport json;

    String adminToken;
    String userToken;
    UUID c1;
    UUID c2;

    @BeforeEach
    void setUp() throws Exception {
        json = new JsonSupport(om);
        data.ensureAdmin("adminV", "adminV@test.local", "Admin123!");
        adminToken = json.loginToken(mvc, "adminV", "Admin123!");
        UserEntity u = data.ensureUser("vuser", "vuser@test.local", "User123!");
        userToken = json.loginToken(mvc, "vuser", "User123!");

        String exp = YearMonth.now().plusMonths(6).toString();
        var r1 = Map.of("ownerId", u.getId(), "expiry", exp, "pan", "4111111111111111", "initialBalance", new BigDecimal("50.00"));
        var r2 = Map.of("ownerId", u.getId(), "expiry", exp, "pan", "4242424242424242", "initialBalance", new BigDecimal("10.00"));

        JsonNode j1 = om.readTree(mvc.perform(post("/api/cards").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(r1)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsByteArray());
        JsonNode j2 = om.readTree(mvc.perform(post("/api/cards").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(r2)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsByteArray());
        c1 = UUID.fromString(j1.get("id").asText());
        c2 = UUID.fromString(j2.get("id").asText());
    }

    @Test
    @DisplayName("Transfer amount == 0 → 4xx")
    void amountZero() throws Exception {
        var req = new TransferRequest(c1, c2, new BigDecimal("0.00"));
        mvc.perform(post("/api/cards/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(req)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Transfer amount < 0 → 4xx")
    void amountNegative() throws Exception {
        var req = new TransferRequest(c1, c2, new BigDecimal("-1.00"));
        mvc.perform(post("/api/cards/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(req)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Transfer amount scale > 2 → 4xx")
    void amountScaleTooBig() throws Exception {
        var req = new TransferRequest(c1, c2, new BigDecimal("1.001"));
        mvc.perform(post("/api/cards/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(req)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Transfer from == to → 4xx")
    void fromEqualsTo() throws Exception {
        var req = new TransferRequest(c1, c1, new BigDecimal("1.00"));
        mvc.perform(post("/api/cards/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(req)))
                .andExpect(status().is4xxClientError());
    }
}