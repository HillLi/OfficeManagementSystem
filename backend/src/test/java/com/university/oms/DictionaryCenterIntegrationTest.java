package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DictionaryCenterIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loggedInUserCanReadCatalogAndVersionWithSeededBusinessStatuses() throws Exception {
        String token = login("user");

        JsonNode catalog = getJson("/api/dictionaries", token).get("data");
        assertFalse(catalog.get("version").asText().isEmpty());
        JsonNode statuses = catalog.get("dictionaries").get("business_status");
        assertTrue(hasEnabledLabel(statuses, "draft"));
        assertTrue(hasEnabledLabel(statuses, "pending_dept"));

        JsonNode version = getJson("/api/dictionaries/version", token).get("data");
        assertFalse(version.asText().isEmpty());
        assertEquals(catalog.get("version").asText(), version.asText());
    }

    @Test
    void ordinaryUserCannotMaintainDictionaries() throws Exception {
        String token = login("user");

        mockMvc.perform(post("/api/admin/dictionaries/types")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dictType\":\"denied_type\",\"dictName\":\"Denied\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanMaintainOrdinaryTypesAndItemsAndDisabledItemsRemainInCatalog() throws Exception {
        String token = login("admin");

        postJson("/api/admin/dictionaries/types",
                "{\"dictType\":\"integration_type\",\"dictName\":\"Integration Type\"}", token);
        JsonNode item = postJson("/api/admin/dictionaries/types/integration_type/items",
                "{\"dictCode\":\"sample\",\"dictLabel\":\"Sample\",\"sortOrder\":3}", token).get("data");
        assertEquals("sample", item.get("dictCode").asText());
        JsonNode disabledType = putJson("/api/admin/dictionaries/types/integration_type",
                "{\"dictType\":\"integration_type\",\"dictName\":\"Integration Type\",\"enabled\":false}", token).get("data");
        assertFalse(disabledType.get("enabled").asBoolean());

        postJson("/api/admin/dictionaries/types/meeting_type/items",
                "{\"dictCode\":\"integration_meeting\",\"dictLabel\":\"Meeting\",\"sortOrder\":1}", token);
        JsonNode disabledItem = putJson("/api/admin/dictionaries/types/meeting_type/items/integration_meeting",
                "{\"dictCode\":\"integration_meeting\",\"dictLabel\":\"Archived Meeting\",\"sortOrder\":1,\"enabled\":false}",
                token).get("data");
        assertEquals("Archived Meeting", disabledItem.get("dictLabel").asText());
        assertFalse(disabledItem.get("enabled").asBoolean());

        JsonNode meetingItems = getJson("/api/dictionaries", token).get("data")
                .get("dictionaries").get("meeting_type");
        assertTrue(hasItem(meetingItems, "integration_meeting", "Archived Meeting", false));

        JsonNode auditLogs = getJson("/api/workflow/audit-logs", token).get("data");
        assertTrue(hasDictionaryUpdateAudit(auditLogs, "meeting_type"));
    }

    @Test
    void systemDictionaryCodesAndEnabledStateAreProtected() throws Exception {
        String token = login("admin");

        putBadRequest("/api/admin/dictionaries/types/business_status",
                "{\"dictType\":\"changed_status\",\"dictName\":\"Business Status\",\"enabled\":true}", token);
        putBadRequest("/api/admin/dictionaries/types/business_status",
                "{\"dictType\":\"business_status\",\"dictName\":\"Business Status\",\"enabled\":false}", token);
        putBadRequest("/api/admin/dictionaries/types/business_status/items/draft",
                "{\"dictCode\":\"changed_draft\",\"dictLabel\":\"Draft\",\"enabled\":true}", token);
        putBadRequest("/api/admin/dictionaries/types/business_status/items/draft",
                "{\"dictCode\":\"draft\",\"dictLabel\":\"Draft\",\"enabled\":false}", token);
    }

    private boolean hasEnabledLabel(JsonNode items, String code) {
        for (JsonNode item : items) {
            if (code.equals(item.get("dictCode").asText())
                    && item.get("enabled").asBoolean()
                    && !item.get("dictLabel").asText().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasItem(JsonNode items, String code, String label, boolean enabled) {
        for (JsonNode item : items) {
            if (code.equals(item.get("dictCode").asText())
                    && label.equals(item.get("dictLabel").asText())
                    && enabled == item.get("enabled").asBoolean()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDictionaryUpdateAudit(JsonNode logs, String type) {
        for (JsonNode log : logs) {
            if ("dictionary".equals(log.get("module").asText())
                    && "update_item".equals(log.get("action").asText())
                    && log.get("detail").asText().contains(type)) {
                return true;
            }
        }
        return false;
    }

    private String login(String username) throws Exception {
        JsonNode response = postJson("/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"123456\"}", null);
        return response.get("data").get("token").asText();
    }

    private JsonNode postJson(String url, String json, String token) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        String body = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private JsonNode putJson(String url, String json, String token) throws Exception {
        String body = mockMvc.perform(put(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private JsonNode getJson(String url, String token) throws Exception {
        String body = mockMvc.perform(get(url).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private void putBadRequest(String url, String json, String token) throws Exception {
        mockMvc.perform(put(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
