package com.example.bankcards;

import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.JpaCardRepository;
import com.example.bankcards.repository.JpaUserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CardControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    TestDataHelper data;
    @Autowired
    JpaUserRepository users;
    @Autowired
    JpaCardRepository cards;
    JsonSupport json;

    String adminToken;
    UserEntity user;

    @BeforeEach
    void init() throws Exception {
        json = new JsonSupport(om);
        data.ensureAdmin("adminTest", "admin@test.local", "Admin123!");
        user = data.ensureUser("userA", "userA@test.local", "User123!");
        adminToken = json.loginToken(mvc, "adminTest", "Admin123!");
    }

    @Test
    @DisplayName("ADMIN: create 2 cards for user; USER: transfer between them")
    void createAndTransfer() throws Exception {
        UUID ownerId = user.getId();
        String exp = YearMonth.now().plusMonths(6).toString();

        var req1 = Map.of(
                "ownerId", ownerId,
                "expiry", exp,
                "pan", "4111111111111111",
                "initialBalance", new BigDecimal("100.00")
        );
        var req2 = Map.of(
                "ownerId", ownerId,
                "expiry", exp,
                "pan", "4242424242424242",
                "initialBalance", new BigDecimal("5.00")
        );

        var res1 = mvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(req1)))
                .andExpect(status().isCreated())
                .andReturn();
        var res2 = mvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(req2)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode j1 = om.readTree(res1.getResponse().getContentAsByteArray());
        JsonNode j2 = om.readTree(res2.getResponse().getContentAsByteArray());
        UUID c1 = UUID.fromString(j1.get("id").asText());
        UUID c2 = UUID.fromString(j2.get("id").asText());
        assertThat(j1.get("ownerId").asText()).isEqualTo(ownerId.toString());
        assertThat(j2.get("ownerId").asText()).isEqualTo(ownerId.toString());
        assertThat(j1.get("maskedNumber").asText()).matches("\\*{4} \\*{4} \\*{4} \\d{4}");

        String userToken = json.loginToken(mvc, "userA", "User123!");
        var transfer = new TransferRequest(c1, c2, new BigDecimal("40.00"));
        mvc.perform(post("/api/cards/transfer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(transfer)))
                .andExpect(status().isNoContent());

        var get1 = mvc.perform(get("/api/cards/" + c1)
                        .header("Authorization", "Bearer " + userToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        var get2 = mvc.perform(get("/api/cards/" + c2)
                        .header("Authorization", "Bearer " + userToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode g1 = om.readTree(get1.getResponse().getContentAsByteArray());
        JsonNode g2 = om.readTree(get2.getResponse().getContentAsByteArray());
        assertThat(new BigDecimal(g1.get("balance").asText())).isEqualByComparingTo("60.00");
        assertThat(new BigDecimal(g2.get("balance").asText())).isEqualByComparingTo("45.00");
    }
}