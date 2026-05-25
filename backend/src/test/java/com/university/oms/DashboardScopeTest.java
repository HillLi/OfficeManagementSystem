package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardScopeTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void dashboardAndExportRespectCurrentUserBusinessScope() throws Exception {
        String userToken = login("user");
        String adminToken = login("admin");
        int userBaseline = dashboard(userToken).get("documentCount").asInt();
        int adminBaseline = dashboard(adminToken).get("documentCount").asInt();

        createDocument("own-visible-dashboard-title", userToken);
        createDocument("other-secret-dashboard-title", adminToken);

        assertEquals(userBaseline + 1, dashboard(userToken).get("documentCount").asInt());
        assertEquals(adminBaseline + 2, dashboard(adminToken).get("documentCount").asInt());
        assertTrue(statistics(userToken).get("documentCount").asInt() < statistics(adminToken).get("documentCount").asInt());
        mockMvc.perform(get("/api/statistics/export").header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("other-secret-dashboard-title"))));
    }

    private void createDocument(String title, String token) throws Exception {
        postJson("/api/documents",
                "{\"title\":\"" + title + "\",\"docType\":\"notice\",\"secrecyLevel\":\"internal\","
                        + "\"content\":\"scope verification content\",\"applicantId\":2}", token);
    }

    private JsonNode dashboard(String token) throws Exception {
        return getJson("/api/dashboard", token).get("data");
    }

    private JsonNode statistics(String token) throws Exception {
        return getJson("/api/statistics", token).get("data");
    }

    private String login(String username) throws Exception {
        return postJson("/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"123456\"}", null)
                .get("data").get("token").asText();
    }

    private JsonNode postJson(String url, String json, String token) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) {
            request.header("Authorization", bearer(token));
        }
        String body = mockMvc.perform(request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private JsonNode getJson(String url, String token) throws Exception {
        String body = mockMvc.perform(get(url).header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
