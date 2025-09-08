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

class CardTransferNegativeIntegrationTest extends AbstractIntegrationTest {

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
        data.ensureAdmin("adminT", "adminT@test.local", "Admin123!");
        adminToken = json.loginToken(mvc, "adminT", "Admin123!");
    }

    @Test
    @DisplayName("Transfer — not owner/insufficient funds → 4xx")
    void transfer_negativeCases() throws Exception {
        String exp = YearMonth.now().plusMonths(6).toString();
        UserEntity A = data.ensureUser("A", "a@test.local", "U123!");
        UserEntity B = data.ensureUser("B", "b@test.local", "U123!");

        var a1 = Map.of("ownerId", A.getId(), "expiry", exp, "pan", "4111111111111111", "initialBalance", new BigDecimal("10.00"));
        var b1 = Map.of("ownerId", B.getId(), "expiry", exp, "pan", "4242424242424242", "initialBalance", new BigDecimal("5.00"));

        JsonNode ja1 = om.readTree(mvc.perform(post("/api/cards").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(a1)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsByteArray());
        JsonNode jb1 = om.readTree(mvc.perform(post("/api/cards").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(b1)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsByteArray());

        UUID a1id = UUID.fromString(ja1.get("id").asText());
        UUID b1id = UUID.fromString(jb1.get("id").asText());

        String aToken = json.loginToken(mvc, "A", "U123!");

        var cross = new TransferRequest(a1id, b1id, new BigDecimal("1.00"));
        mvc.perform(post("/api/cards/transfer")
                        .header("Authorization", "Bearer " + aToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(cross)))
                .andExpect(status().is4xxClientError());

        var tooMuch = new TransferRequest(a1id, a1id, new BigDecimal("20.00"));
        mvc.perform(post("/api/cards/transfer")
                        .header("Authorization", "Bearer " + aToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(tooMuch)))
                .andExpect(status().is4xxClientError());
    }
}
