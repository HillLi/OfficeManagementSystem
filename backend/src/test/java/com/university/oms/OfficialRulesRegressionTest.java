package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.oms.model.Travel;
import com.university.oms.model.TravelCheckResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OfficialRulesRegressionTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void largeActivityRequiresSchoolLeaderApprovalAfterSecurityAndDepartment() throws Exception {
        String userToken = login("user");
        String securityToken = login("security");
        String headToken = login("head");
        String leaderToken = login("leader");
        LocalDate day = LocalDate.now().plusDays(35);

        JsonNode meeting = postJson("/api/meetings",
                "{\"title\":\"large activity approval\",\"roomId\":4,\"startTime\":\"" + day
                        + "T09:00:00\",\"endTime\":\"" + day
                        + "T11:00:00\",\"organizerId\":2,\"expectedCount\":510,\"venueType\":\"\u5ba4\u5185\","
                        + "\"riskReportUrl\":\"/risk.pdf\",\"securityPlanUrl\":\"/security.pdf\","
                        + "\"emergencyPlanUrl\":\"/emergency.pdf\","
                        + "\"participants\":[2,3],\"recorderId\":2}", userToken).get("data");
        long id = meeting.get("id").asLong();
        assertEquals("pending_security", meeting.get("status").asText());

        JsonNode afterSecurity = postJson("/api/approvals/meeting/" + id,
                "{\"action\":\"approve\",\"opinion\":\"security ok\"}", securityToken).get("data");
        assertEquals("pending_dept", afterSecurity.get("status").asText());
        JsonNode afterDepartment = postJson("/api/approvals/meeting/" + id,
                "{\"action\":\"approve\",\"opinion\":\"department ok\"}", headToken).get("data");
        assertEquals("pending_leader", afterDepartment.get("status").asText());
        JsonNode approved = postJson("/api/approvals/meeting/" + id,
                "{\"action\":\"approve\",\"opinion\":\"leader ok\"}", leaderToken).get("data");
        assertEquals("approved", approved.get("status").asText());
    }

    @Test
    void largeActivityGuideShowsSchoolLeaderApprovalNode() throws Exception {
        String userToken = login("user");
        LocalDate day = LocalDate.now().plusDays(35);

        JsonNode meeting = postJson("/api/meetings",
                "{\"title\":\"large activity guide\",\"roomId\":4,\"startTime\":\"" + day
                        + "T13:00:00\",\"endTime\":\"" + day
                        + "T15:00:00\",\"organizerId\":2,\"expectedCount\":510,\"venueType\":\"\u5ba4\u5185\","
                        + "\"riskReportUrl\":\"/risk.pdf\",\"securityPlanUrl\":\"/security.pdf\","
                        + "\"emergencyPlanUrl\":\"/emergency.pdf\","
                        + "\"participants\":[2,3],\"recorderId\":2}", userToken).get("data");

        JsonNode guide = getJson("/api/workflow/guide?bizType=meeting&bizId=" + meeting.get("id").asLong(), userToken)
                .get("data");

        assertTrue(containsStep(guide.get("steps"), "pending_security"));
        assertTrue(containsStep(guide.get("steps"), "pending_dept"));
        assertTrue(containsStep(guide.get("steps"), "pending_leader"));
    }

    @Test
    void reportRequiresSchoolLeaderApprovalBeforeReply() throws Exception {
        String userToken = login("user");
        String officeToken = login("office");
        String headToken = login("head");
        String leaderToken = login("leader");

        JsonNode report = postJson("/api/reports",
                "{\"title\":\"major report\",\"type\":\"\u8bf7\u793a\",\"secrecyLevel\":\"\u5185\u90e8\","
                        + "\"content\":\"major matter\",\"applicantId\":2}", userToken).get("data");
        long id = report.get("id").asLong();

        JsonNode afterSecretReview = postJson("/api/approvals/report/" + id,
                "{\"action\":\"approve\",\"opinion\":\"secret ok\"}", officeToken).get("data");
        assertEquals("pending_dept", afterSecretReview.get("status").asText());
        JsonNode afterDepartment = postJson("/api/approvals/report/" + id,
                "{\"action\":\"approve\",\"opinion\":\"department ok\"}", headToken).get("data");
        assertEquals("pending_leader", afterDepartment.get("status").asText());

        mockMvc.perform(post("/api/reports/" + id + "/reply")
                        .header("Authorization", bearer(officeToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .content("{\"reply\":\"not yet\"}".getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest());

        JsonNode approved = postJson("/api/approvals/report/" + id,
                "{\"action\":\"approve\",\"opinion\":\"leader ok\"}", leaderToken).get("data");
        assertEquals("approved", approved.get("status").asText());
    }

    @Test
    void specialDomesticManagementMeetingUsesSixHundredLimit() throws Exception {
        String userToken = login("user");

        JsonNode meeting = postJson("/api/meetings",
                "{\"title\":\"special management meeting\",\"roomId\":1,\"startTime\":\"2026-12-10T09:00:00\","
                        + "\"endTime\":\"2026-12-10T10:00:00\",\"organizerId\":2,"
                        + "\"venueType\":\"\u5ba4\u5185\",\"meetingType\":\"\u56fd\u5185\u7ba1\u7406\u4f1a\u8bae\uff08\u8bb2\u5e2d\u6559\u63883\u4eba\u53ca\u4ee5\u4e0a\uff09\","
                        + "\"budget\":600,\"accommodationFee\":390,\"mealFee\":130,\"otherFee\":80,"
                        + "\"participants\":[2,3],\"recorderId\":2}", userToken)
                .get("data");

        assertEquals("pending_dept", meeting.get("status").asText());
    }

    @Test
    void travelRulesDistinguishTeachingResearchAndOtherBusinessFlightClass() throws Exception {
        String userToken = login("user");

        mockMvc.perform(post("/api/travels")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .content(("{\"applicantId\":2,\"destination\":\"\u4e0a\u6d77\",\"startDate\":\"2026-12-01\","
                                + "\"endDate\":\"2026-12-02\",\"reason\":\"other business\",\"staffLevel\":\"\u4e8c\u7c7b\","
                                + "\"travelType\":\"\u5176\u4ed6\u4e1a\u52a1\",\"transport\":\"\u98de\u673a\u516c\u52a1\u8231\","
                                + "\"budget\":1200}").getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest());

        JsonNode teachingResearch = postJson("/api/travels",
                "{\"applicantId\":2,\"destination\":\"\u4e0a\u6d77\",\"startDate\":\"2026-12-03\","
                        + "\"endDate\":\"2026-12-04\",\"reason\":\"research\",\"staffLevel\":\"\u4e8c\u7c7b\","
                        + "\"travelType\":\"\u6559\u5b66\u79d1\u7814\u4e1a\u52a1\",\"transport\":\"\u98de\u673a\u516c\u52a1\u8231\","
                        + "\"budget\":1200}", userToken).get("data");
        assertEquals("pending_dept", teachingResearch.get("status").asText());

        JsonNode otherBusiness = postJson("/api/travels",
                "{\"applicantId\":2,\"destination\":\"\u4e0a\u6d77\",\"startDate\":\"2026-12-05\","
                        + "\"endDate\":\"2026-12-06\",\"reason\":\"other business\",\"staffLevel\":\"\u4e8c\u7c7b\","
                        + "\"travelType\":\"\u5176\u4ed6\u4e1a\u52a1\",\"transport\":\"\u98de\u673a\u7ecf\u6d4e\u8231\","
                        + "\"budget\":1200}", userToken).get("data");
        assertEquals("pending_dept", otherBusiness.get("status").asText());
    }

    @Test
    void travelExpenseStrategyUsesConfiguredOfficialStandardRows() {
        Travel travel = new Travel();
        travel.setDestination("\u4e0a\u6d77");
        travel.setStartDate(LocalDate.of(2026, 12, 1));
        travel.setEndDate(LocalDate.of(2026, 12, 2));
        travel.setStaffLevel("\u4e8c\u7c7b");
        travel.setTravelType("\u5176\u4ed6\u4e1a\u52a1");
        travel.setBudget(java.math.BigDecimal.valueOf(1600));

        TravelCheckResult result = new com.university.oms.design.StandardTravelExpenseStrategy().check(travel);

        assertEquals(new java.math.BigDecimal("1660"), result.getStandardAmount());
        assertFalse(result.isOverLimit());
    }

    @Test
    void sealApplicationExposesTenYearRetentionDeadline() throws Exception {
        String userToken = login("user");

        JsonNode application = postJson("/api/seals/applications",
                "{\"sealId\":2,\"applicantId\":2,\"purpose\":\"retention policy\",\"copies\":1,"
                        + "\"takeOut\":false,\"matterLevel\":\"\u5e38\u89c4\u4e8b\u9879\"}", userToken)
                .get("data");

        LocalDateTime createdAt = LocalDateTime.parse(application.get("createdAt").asText());
        LocalDateTime retentionUntil = LocalDateTime.parse(application.get("retentionUntil").asText());
        assertEquals(createdAt.plusYears(10), retentionUntil);
    }

    private boolean containsStep(JsonNode steps, String key) {
        for (JsonNode step : steps) {
            if (key.equals(step.get("key").asText())) {
                return true;
            }
        }
        return false;
    }

    private String login(String username) throws Exception {
        return postJson("/api/auth/login",
                "{\"username\":\"" + username + "\",\"password\":\"123456\"}", null)
                .get("data").get("token").asText();
    }

    private JsonNode postJson(String url, String json, String token) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(json.getBytes(StandardCharsets.UTF_8));
        if (token != null) {
            request.header("Authorization", bearer(token));
        }
        String body = mockMvc.perform(request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    private JsonNode getJson(String url, String token) throws Exception {
        String body = mockMvc.perform(get(url).header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
