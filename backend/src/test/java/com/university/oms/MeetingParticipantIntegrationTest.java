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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MeetingParticipantIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createMeetingWithParticipants() throws Exception {
        String userToken = login("user");

        String json = "{" +
                "\"title\":\"参会人测试会议\"," +
                "\"roomId\":1," +
                "\"organizerId\":2," +
                "\"startTime\":\"2026-07-01T09:00:00\"," +
                "\"endTime\":\"2026-07-01T11:00:00\"," +
                "\"venueType\":\"室内\"," +
                "\"meetingType\":\"国内管理会议\"," +
                "\"budget\":0," +
                "\"expectedCount\":2," +
                "\"participants\":[2,3]," +
                "\"recorderId\":3" +
                "}";

        JsonNode created = postJson("/api/meetings", json, userToken).get("data");
        assertEquals(2, created.get("expectedCount").asInt());
        assertEquals(3, created.get("recorderId").asLong());
    }

    @Test
    void createMeetingDerivesExpectedCountFromParticipants() throws Exception {
        String userToken = login("user");

        String json = "{" +
                "\"title\":\"自动人数测试会议\"," +
                "\"roomId\":1," +
                "\"organizerId\":2," +
                "\"startTime\":\"2026-07-09T09:00:00\"," +
                "\"endTime\":\"2026-07-09T11:00:00\"," +
                "\"venueType\":\"室内\"," +
                "\"meetingType\":\"国内管理会议\"," +
                "\"budget\":0," +
                "\"expectedCount\":99," +
                "\"participants\":[2,3]," +
                "\"recorderId\":3" +
                "}";

        JsonNode created = postJson("/api/meetings", json, userToken).get("data");
        assertEquals(2, created.get("expectedCount").asInt());
    }

    @Test
    void participantsEndpointReturnsRecorderAndConfirmationFields() throws Exception {
        String userToken = login("user");

        String createJson = meetingJson("参会人字段测试", 9, "2026-07-09T14:00:00", "2026-07-09T16:00:00");
        JsonNode created = postJson("/api/meetings", createJson, userToken).get("data");
        long meetingId = created.get("id").asLong();

        JsonNode participants = getJson("/api/meetings/" + meetingId + "/participants", userToken).get("data");
        JsonNode recorder = findParticipant(participants, 3);
        assertTrue(recorder.get("recorder").asBoolean());
        assertFalse(recorder.get("minutesConfirmed").asBoolean());
    }

    @Test
    void createMeetingWithoutParticipantsFails() throws Exception {
        String userToken = login("user");

        String json = "{" +
                "\"title\":\"无参会人会议\"," +
                "\"roomId\":1," +
                "\"organizerId\":2," +
                "\"startTime\":\"2026-07-02T09:00:00\"," +
                "\"endTime\":\"2026-07-02T11:00:00\"," +
                "\"venueType\":\"室内\"," +
                "\"meetingType\":\"国内管理会议\"," +
                "\"budget\":0," +
                "\"recorderId\":3" +
                "}";

        mockMvc.perform(post("/api/meetings")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMeetingRecorderNotInParticipantsFails() throws Exception {
        String userToken = login("user");

        String json = "{" +
                "\"title\":\"记录员不在参会人中\"," +
                "\"roomId\":1," +
                "\"organizerId\":2," +
                "\"startTime\":\"2026-07-03T09:00:00\"," +
                "\"endTime\":\"2026-07-03T11:00:00\"," +
                "\"venueType\":\"室内\"," +
                "\"meetingType\":\"国内管理会议\"," +
                "\"budget\":0," +
                "\"participants\":[2,3]," +
                "\"recorderId\":999" +
                "}";

        mockMvc.perform(post("/api/meetings")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recorderCanArchiveMinutes() throws Exception {
        String userToken = login("user");
        String headToken = login("head");

        // Create meeting as user (participants [2,3], recorderId=3)
        String createJson = meetingJson("记录员归档纪要测试", 4, "2026-07-04T09:00:00", "2026-07-04T11:00:00");
        JsonNode created = postJson("/api/meetings", createJson, userToken).get("data");
        long meetingId = created.get("id").asLong();

        // Approve as head (dept head, user id=3)
        postJson("/api/approvals/meeting/" + meetingId, "{\"action\":\"approve\",\"opinion\":\"同意\"}", headToken);

        // Archive minutes as head (user id=3 is the recorder)
        String minutesJson = "{\"minutes\":\"这是会议纪要内容\",\"signInCount\":2}";
        JsonNode result = postJson("/api/meetings/" + meetingId + "/minutes", minutesJson, headToken).get("data");
        assertEquals("minutes_pending", result.get("status").asText());
    }

    @Test
    void nonRecorderCannotArchiveMinutes() throws Exception {
        String userToken = login("user");
        String headToken = login("head");

        // Create meeting with recorderId=3 (head)
        String createJson = meetingJson("非记录员归档测试", 5, "2026-07-05T09:00:00", "2026-07-05T11:00:00");
        JsonNode created = postJson("/api/meetings", createJson, userToken).get("data");
        long meetingId = created.get("id").asLong();

        // Approve as head
        postJson("/api/approvals/meeting/" + meetingId, "{\"action\":\"approve\",\"opinion\":\"同意\"}", headToken);

        // Try to archive minutes as user (id=2, not recorder) — should fail
        String minutesJson = "{\"minutes\":\"不应成功的纪要\",\"signInCount\":1}";
        mockMvc.perform(post("/api/meetings/" + meetingId + "/minutes")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(minutesJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void participantCanConfirmMinutes() throws Exception {
        String userToken = login("user");
        String headToken = login("head");

        // Create meeting (participants [2,3], recorderId=3)
        String createJson = meetingJson("参会人确认纪要测试", 1, "2026-07-06T09:00:00", "2026-07-06T11:00:00");
        JsonNode created = postJson("/api/meetings", createJson, userToken).get("data");
        long meetingId = created.get("id").asLong();

        // Approve
        postJson("/api/approvals/meeting/" + meetingId, "{\"action\":\"approve\",\"opinion\":\"同意\"}", headToken);

        // Record minutes as head (recorder id=3)
        postJson("/api/meetings/" + meetingId + "/minutes",
                "{\"minutes\":\"确认测试纪要\",\"signInCount\":2}", headToken);

        // Confirm as user (participant id=2)
        JsonNode confirmed = postJson("/api/meetings/" + meetingId + "/confirm-minutes", "{}", userToken).get("data");
        // After one participant confirms, status should still be minutes_pending
        // (recorder/head id=3 still needs to confirm too)
        assertEquals("minutes_pending", confirmed.get("status").asText());
    }

    @Test
    void participatedMeetingsIncludeCurrentUserConfirmationState() throws Exception {
        String userToken = login("user");
        String headToken = login("head");

        String createJson = meetingJson("参与会议确认状态测试", 10, "2026-07-10T09:00:00", "2026-07-10T11:00:00");
        JsonNode created = postJson("/api/meetings", createJson, userToken).get("data");
        long meetingId = created.get("id").asLong();

        postJson("/api/approvals/meeting/" + meetingId, "{\"action\":\"approve\",\"opinion\":\"同意\"}", headToken);
        postJson("/api/meetings/" + meetingId + "/minutes",
                "{\"minutes\":\"参与会议状态纪要\",\"signInCount\":2}", headToken);

        JsonNode before = findParticipated(getJson("/api/meetings/participated", userToken).get("data"), meetingId);
        assertEquals(meetingId, before.get("meetingId").asLong());
        assertFalse(before.get("minutesConfirmed").asBoolean());

        postJson("/api/meetings/" + meetingId + "/confirm-minutes", "{}", userToken);

        JsonNode after = findParticipated(getJson("/api/meetings/participated", userToken).get("data"), meetingId);
        assertTrue(after.get("minutesConfirmed").asBoolean());
    }

    @Test
    void publishCreatesAnnouncement() throws Exception {
        String userToken = login("user");
        String headToken = login("head");

        // Create meeting
        String createJson = meetingJson("发布创建公告测试", 2, "2026-07-07T09:00:00", "2026-07-07T11:00:00");
        JsonNode created = postJson("/api/meetings", createJson, userToken).get("data");
        long meetingId = created.get("id").asLong();

        // Approve
        postJson("/api/approvals/meeting/" + meetingId, "{\"action\":\"approve\",\"opinion\":\"同意\"}", headToken);

        // Record minutes as head (recorder)
        postJson("/api/meetings/" + meetingId + "/minutes",
                "{\"minutes\":\"发布测试纪要\",\"signInCount\":2}", headToken);

        // All participants confirm: user (id=2) and head (id=3)
        postJson("/api/meetings/" + meetingId + "/confirm-minutes", "{}", userToken);
        postJson("/api/meetings/" + meetingId + "/confirm-minutes", "{}", headToken);

        // Publish as organizer (user)
        JsonNode published = postJson("/api/meetings/" + meetingId + "/publish", "{}", userToken).get("data");
        assertEquals("archived", published.get("status").asText());

        // Verify announcement was created
        JsonNode announcements = getJson("/api/announcements", userToken).get("data");
        boolean found = false;
        for (JsonNode ann : announcements) {
            if (ann.get("title").asText().contains("会议纪要公示")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Should find an announcement with title containing '会议纪要公示'");
    }

    @Test
    void archiveDirectlySkipsAnnouncement() throws Exception {
        String userToken = login("user");
        String headToken = login("head");

        // Create meeting
        String createJson = meetingJson("直接归档测试", 3, "2026-07-08T09:00:00", "2026-07-08T11:00:00");
        JsonNode created = postJson("/api/meetings", createJson, userToken).get("data");
        long meetingId = created.get("id").asLong();

        // Approve
        postJson("/api/approvals/meeting/" + meetingId, "{\"action\":\"approve\",\"opinion\":\"同意\"}", headToken);

        // Record minutes as head (recorder)
        postJson("/api/meetings/" + meetingId + "/minutes",
                "{\"minutes\":\"归档测试纪要\",\"signInCount\":2}", headToken);

        // All participants confirm
        postJson("/api/meetings/" + meetingId + "/confirm-minutes", "{}", userToken);
        postJson("/api/meetings/" + meetingId + "/confirm-minutes", "{}", headToken);

        // Archive directly as organizer (user)
        JsonNode archived = postJson("/api/meetings/" + meetingId + "/archive", "{}", userToken).get("data");
        assertEquals("archived", archived.get("status").asText());

        // Verify NO announcement with title containing this meeting's name was created
        JsonNode announcements = getJson("/api/announcements", userToken).get("data");
        for (JsonNode ann : announcements) {
            assertFalse(
                    ann.get("title").asText().contains("直接归档测试"),
                    "Archive directly should NOT create an announcement"
            );
        }
    }

    private String meetingJson(String title, int dayOffset, String startTime, String endTime) {
        return "{" +
                "\"title\":\"" + title + "\"," +
                "\"roomId\":1," +
                "\"organizerId\":2," +
                "\"startTime\":\"" + startTime + "\"," +
                "\"endTime\":\"" + endTime + "\"," +
                "\"venueType\":\"室内\"," +
                "\"meetingType\":\"国内管理会议\"," +
                "\"budget\":0," +
                "\"participants\":[2,3]," +
                "\"recorderId\":3" +
                "}";
    }

    private boolean contains(JsonNode rows, long id) {
        return find(rows, id) != null;
    }

    private JsonNode find(JsonNode rows, long id) {
        for (JsonNode row : rows) {
            if (row.get("id").asLong() == id) {
                return row;
            }
        }
        return null;
    }

    private JsonNode findParticipant(JsonNode rows, long userId) {
        for (JsonNode row : rows) {
            if (row.get("userId").asLong() == userId) {
                return row;
            }
        }
        return null;
    }

    private JsonNode findParticipated(JsonNode rows, long meetingId) {
        for (JsonNode row : rows) {
            if (row.get("meetingId").asLong() == meetingId) {
                return row;
            }
        }
        return null;
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

    private String login(String username) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode response = objectMapper.readTree(body);
        return response.get("data").get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
