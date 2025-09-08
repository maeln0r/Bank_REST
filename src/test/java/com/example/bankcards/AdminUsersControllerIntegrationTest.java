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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminUsersControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    TestDataHelper data;
    JsonSupport json;

    String adminToken;
    String userToken;

    @BeforeEach
    void init() throws Exception {
        json = new JsonSupport(om);
        data.ensureAdmin("adminU", "adminU@test.local", "Admin123!");
        adminToken = json.loginToken(mvc, "adminU", "Admin123!");
        data.ensureUser("plainU", "plain@test.local", "User123!");
        userToken = json.loginToken(mvc, "plainU", "User123!");
    }

    @Test
    @DisplayName("ADMIN create user + duplicate check; USER forbidden for admin endpoints")
    void adminCreateAndSecurity() throws Exception {
        var body = Map.of(
                "username", "newUser",
                "email", "new@u.test",
                "password", "New123!"
        );
        var created = mvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode j = om.readTree(created.getResponse().getContentAsByteArray());
        assertThat(j.get("username").asText()).isEqualTo("newUser");

        mvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(body)))
                .andExpect(status().is4xxClientError());

        mvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN list users with pagination & query")
    void listUsers() throws Exception {
        mvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("query", "new")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}