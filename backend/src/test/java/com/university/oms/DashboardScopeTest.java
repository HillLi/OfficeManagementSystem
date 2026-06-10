package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardScopeTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    @Test
    void dashboardIncludesOnlyVisibleCurrentMonthMeetingSchedules() throws Exception {
        String userToken = login("user");
        String adminToken = login("admin");
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(15);
        LocalDate nextMonth = currentMonth.plusMonths(1);

        long ownVisibleId = insertMeeting(9101L, "own-visible-monthly-activity", 2L,
                currentMonth.atTime(9, 0), currentMonth.atTime(11, 0), "approved", true);
        long hiddenOtherDeptId = insertMeeting(9102L, "hidden-other-dept-monthly-meeting", 6L,
                currentMonth.atTime(14, 0), currentMonth.atTime(15, 0), "approved", false);
        long rejectedOwnId = insertMeeting(9103L, "rejected-own-monthly-meeting", 2L,
                currentMonth.atTime(16, 0), currentMonth.atTime(17, 0), "rejected", false);
        long nextMonthOwnId = insertMeeting(9104L, "next-month-own-meeting", 2L,
                nextMonth.atTime(9, 0), nextMonth.atTime(10, 0), "approved", false);

        try {
            JsonNode userSchedules = dashboard(userToken).get("monthlyScheduleItems");
            assertNotNull(userSchedules, "dashboard monthlyScheduleItems field should exist for user");
            JsonNode userItem = findScheduleByTitle(userSchedules, "own-visible-monthly-activity");
            assertNotNull(userItem, "user schedules should include own visible current-month meeting");
            assertEquals("meeting", userItem.get("bizType").asText());
            assertEquals("大型活动", userItem.get("typeText").asText());
            assertTrue(userItem.get("largeActivity").asBoolean());
            assertEquals("理科一号楼 101", userItem.get("roomName").asText());
            assertFalse(containsScheduleTitle(userSchedules, "hidden-other-dept-monthly-meeting"));
            assertFalse(containsScheduleTitle(userSchedules, "rejected-own-monthly-meeting"));
            assertFalse(containsScheduleTitle(userSchedules, "next-month-own-meeting"));

            JsonNode adminSchedules = dashboard(adminToken).get("monthlyScheduleItems");
            assertNotNull(adminSchedules, "dashboard monthlyScheduleItems field should exist for admin");
            assertNotNull(findScheduleByTitle(adminSchedules, "own-visible-monthly-activity"),
                    "admin schedules should include own current-month meeting");
            assertNotNull(findScheduleByTitle(adminSchedules, "hidden-other-dept-monthly-meeting"),
                    "admin schedules should include other-department current-month meeting");
            assertFalse(containsScheduleTitle(adminSchedules, "rejected-own-monthly-meeting"));
            assertFalse(containsScheduleTitle(adminSchedules, "next-month-own-meeting"));
        } finally {
            jdbcTemplate.update("DELETE FROM oa_meeting WHERE id IN (9101, 9102, 9103, 9104)");
        }
    }

    private long insertMeeting(long id, String title, long organizerId,
                               LocalDateTime startTime, LocalDateTime endTime,
                               String status, boolean largeActivity) {
        jdbcTemplate.update(
                "INSERT INTO oa_meeting (id, title, room_id, start_time, end_time, organizer_id, " +
                        "expected_count, venue_type, meeting_type, status, large_activity, created_at) " +
                        "VALUES (?, ?, 1, ?, ?, ?, ?, '室内', '国内业务会议', ?, ?, CURRENT_TIMESTAMP)",
                id, title, startTime, endTime, organizerId,
                largeActivity ? 600 : 20, status, largeActivity ? 1 : 0);
        return id;
    }

    private void createDocument(String title, String token) throws Exception {
        postJson("/api/documents",
                "{\"title\":\"" + title + "\",\"docType\":\"通知\",\"secrecyLevel\":\"内部\","
                        + "\"content\":\"scope verification content\",\"applicantId\":2}", token);
    }

    private boolean containsScheduleTitle(JsonNode schedules, String title) {
        return findScheduleByTitle(schedules, title) != null;
    }

    private JsonNode findScheduleByTitle(JsonNode schedules, String title) {
        for (JsonNode schedule : schedules) {
            if (title.equals(schedule.path("title").asText())) {
                return schedule;
            }
        }
        return null;
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
