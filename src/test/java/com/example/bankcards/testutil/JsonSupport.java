package com.example.bankcards.testutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class JsonSupport {
    private final ObjectMapper om;

    public JsonSupport(ObjectMapper om) {
        this.om = om;
    }

    public String loginToken(org.springframework.test.web.servlet.MockMvc mvc, String userOrEmail, String password) throws Exception {
        MockHttpServletRequestBuilder req = post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"usernameOrEmail\":\"" + userOrEmail + "\"," +
                        "\"password\":\"" + password + "\"}");
        MvcResult res = mvc.perform(req)
                .andReturn();
        if (res.getResponse().getStatus() != 200) {
            throw new IllegalStateException("Login failed: status=" + res.getResponse().getStatus() + ", body=" + res.getResponse().getContentAsString());
        }
        JsonNode json = om.readTree(res.getResponse().getContentAsByteArray());
        return json.get("accessToken").asText();
    }

    public String refreshToken(org.springframework.test.web.servlet.MockMvc mvc, String refresh) throws Exception {
        MockHttpServletRequestBuilder req = post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"refreshToken\":\"" + refresh + "\"}");
        MvcResult res = mvc.perform(req).andReturn();
        if (res.getResponse().getStatus() != 200) {
            throw new IllegalStateException("Refresh failed: status=" + res.getResponse().getStatus() + ", body=" + res.getResponse().getContentAsString());
        }
        JsonNode json = om.readTree(res.getResponse().getContentAsByteArray());
        return json.get("accessToken").asText();
    }
}