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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnnouncementIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void officeAdminCanPublishAnnouncementAndVisibleUsersCanReadIt() throws Exception {
        String officeToken = login("office");
        String userToken = login("user");
        String financeToken = login("finance");

        JsonNode created = postJson("/api/announcements",
                "{\"title\":\"Trial Run Notice\",\"content\":\"Please read the office system trial notice.\","
                        + "\"category\":\"notice\",\"targetType\":\"dept\",\"targetDeptId\":4,\"pinned\":true}",
                officeToken).get("data");
        long id = created.get("id").asLong();
        assertEquals("draft", created.get("status").asText());

        JsonNode published = postJson("/api/announcements/" + id + "/publish", "{}", officeToken).get("data");
        assertEquals("published", published.get("status").asText());

        JsonNode visibleRows = getJson("/api/announcements", userToken).get("data");
        assertTrue(contains(visibleRows, id));

        JsonNode hiddenRows = getJson("/api/announcements", financeToken).get("data");
        assertFalse(contains(hiddenRows, id));

        JsonNode latest = getJson("/api/announcements/latest?limit=3", userToken).get("data");
        assertTrue(contains(latest, id));
    }

    @Test
    void ordinaryUserCannotMaintainAnnouncements() throws Exception {
        String userToken = login("user");

        mockMvc.perform(post("/api/announcements")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"User Draft\",\"content\":\"Should be rejected\",\"targetType\":\"all\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanUpdateAndWithdrawAnnouncement() throws Exception {
        String adminToken = login("admin");

        long id = postJson("/api/announcements",
                "{\"title\":\"Admin Notice\",\"content\":\"Original\",\"targetType\":\"all\"}",
                adminToken).get("data").get("id").asLong();

        JsonNode updated = putJson("/api/announcements/" + id,
                "{\"title\":\"Admin Notice Updated\",\"content\":\"Updated body\",\"targetType\":\"all\",\"pinned\":false}",
                adminToken).get("data");
        assertEquals("Admin Notice Updated", updated.get("title").asText());

        postJson("/api/announcements/" + id + "/publish", "{}", adminToken);
        JsonNode withdrawn = postJson("/api/announcements/" + id + "/withdraw", "{}", adminToken).get("data");
        assertEquals("withdrawn", withdrawn.get("status").asText());

        JsonNode rows = getJson("/api/announcements", login("user")).get("data");
        assertFalse(contains(rows, id));
    }

    private boolean contains(JsonNode rows, long id) {
        for (JsonNode row : rows) {
            if (row.get("id").asLong() == id) {
                return true;
            }
        }
        return false;
    }

    private JsonNode getJson(String url, String token) throws Exception {
        String body = mockMvc.perform(get(url).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private JsonNode postJson(String url, String json, String token) throws Exception {
        String body = mockMvc.perform(post(url)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private JsonNode putJson(String url, String json, String token) throws Exception {
        String body = mockMvc.perform(put(url)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private String login(String username) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
                ;
        JsonNode response = objectMapper.readTree(body);
        return response.get("data").get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
