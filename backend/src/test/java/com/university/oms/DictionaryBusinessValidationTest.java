package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DictionaryBusinessValidationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void disabledMeetingTypeCannotBeSelectedForNewMeeting() throws Exception {
        String adminToken = login("admin");
        String userToken = login("user");

        postJson("/api/admin/dictionaries/types/meeting_type/items",
                "{\"dictCode\":\"qa_disabled\",\"dictLabel\":\"Disabled meeting\",\"sortOrder\":99}", adminToken);
        putJson("/api/admin/dictionaries/types/meeting_type/items/qa_disabled",
                "{\"dictCode\":\"qa_disabled\",\"dictLabel\":\"Disabled meeting\",\"sortOrder\":99,\"enabled\":false}",
                adminToken);

        expectBadRequest("/api/meetings",
                "{\"title\":\"Disabled dictionary meeting\",\"roomId\":1,"
                        + "\"startTime\":\"2027-03-01T09:00:00\",\"endTime\":\"2027-03-01T10:00:00\","
                        + "\"organizerId\":2,\"expectedCount\":10,\"venueType\":\"室内\","
                        + "\"meetingType\":\"qa_disabled\",\"budget\":0}",
                userToken, "会议类别不在可选字典范围内");
    }

    @Test
    void unknownDocumentReportAndSealValuesAreRejected() throws Exception {
        String userToken = login("user");

        expectBadRequest("/api/documents",
                "{\"title\":\"Invalid document\",\"docType\":\"未知文种\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"body\",\"applicantId\":2}",
                userToken, "公文文种不在可选字典范围内");
        expectBadRequest("/api/reports",
                "{\"title\":\"Invalid report\",\"type\":\"批示\",\"secrecyLevel\":\"内部\","
                        + "\"content\":\"body\",\"applicantId\":2}",
                userToken, "请示报告类型不在可选字典范围内");
        expectBadRequest("/api/seals/applications",
                "{\"sealId\":2,\"applicantId\":2,\"purpose\":\"invalid matter\","
                        + "\"matterLevel\":\"特殊事项\"}",
                userToken, "事项等级不在可选字典范围内");
    }

    @Test
    void unknownTravelChoiceAndAttachmentSecrecyLevelAreRejected() throws Exception {
        String userToken = login("user");

        expectBadRequest("/api/travels",
                "{\"applicantId\":2,\"destination\":\"上海\",\"startDate\":\"2027-03-01\","
                        + "\"endDate\":\"2027-03-02\",\"reason\":\"test\",\"staffLevel\":\"三类\","
                        + "\"travelType\":\"未知出差\",\"transport\":\"高铁二等座\",\"budget\":100}",
                userToken, "出差类型不在可选字典范围内");

        long documentId = postJson("/api/documents",
                "{\"title\":\"Attachment host\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"body\",\"applicantId\":2}",
                userToken).get("data").get("id").asLong();
        expectBadRequest("/api/workflow/attachments",
                "{\"bizType\":\"document\",\"bizId\":" + documentId + ",\"fileName\":\"invalid.pdf\","
                        + "\"fileUrl\":\"/uploads/invalid.pdf\",\"secrecyLevel\":\"非密级\"}",
                userToken, "材料密级不在可选字典范围内");

        long sealId = postJson("/api/seals/applications",
                "{\"sealId\":2,\"applicantId\":2,\"purpose\":\"upload host\","
                        + "\"matterLevel\":\"常规事项\"}",
                userToken).get("data").get("id").asLong();
        MockMultipartFile file = new MockMultipartFile("file", "invalid.pdf",
                MediaType.APPLICATION_PDF_VALUE, "invalid".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/workflow/attachments/upload")
                        .file(file).param("bizType", "seal").param("bizId", String.valueOf(sealId))
                        .param("secrecyLevel", "非密级").header("Authorization", bearer(userToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("材料密级不在可选字典范围内")));
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
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    private JsonNode putJson(String url, String json, String token) throws Exception {
        String body = mockMvc.perform(put(url)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    private void expectBadRequest(String url, String json, String token, String message) throws Exception {
        mockMvc.perform(post(url)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(message)));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
