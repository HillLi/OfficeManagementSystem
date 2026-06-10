package com.university.oms.nlp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for NLP-enhanced features:
 * - AI review returns keywords, sensitiveWords, qualityScore, maxSimilarity
 * - AI draft returns NLP-enriched content with keyword footer
 * - Enhanced room recommendation returns scored rooms
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NlpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== AI Review with NLP ==========

    @Test
    void aiReviewReturnsNlpFields() throws Exception {
        String token = login("user");

        // Create a public document
        JsonNode doc = postJson("/api/documents",
                "{\"title\":\"关于教学改革的通知\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"为了提高教学质量，各学院应积极推进课程改革，加强师资队伍建设，"
                        + "完善教学评估体系，优化教学资源配置，提升人才培养质量。\","
                        + "\"applicantId\":2}", token).get("data");
        long docId = doc.get("id").asLong();

        // Run AI review
        JsonNode review = postJson("/api/documents/" + docId + "/ai-review", "{}", token).get("data");

        // Verify NLP fields exist
        assertTrue(review.has("keywords"), "Should have keywords field");
        assertTrue(review.has("sensitiveWords"), "Should have sensitiveWords field");
        assertTrue(review.has("qualityScore"), "Should have qualityScore field");
        assertTrue(review.has("maxSimilarity"), "Should have maxSimilarity field");

        // Quality score should be reasonable for a well-formed doc
        double score = review.get("qualityScore").asDouble();
        assertTrue(score > 0, "Quality score should be positive, got " + score);

        // Keywords should not be empty
        JsonNode keywords = review.get("keywords");
        assertTrue(keywords.isArray() && keywords.size() > 0, "Should extract keywords");
    }

    @Test
    void aiReviewDetectsSensitiveWords() throws Exception {
        String token = login("user");

        JsonNode doc = postJson("/api/documents",
                "{\"title\":\"关于保密工作的通知\",\"docType\":\"通知\",\"secrecyLevel\":\"公开\","
                        + "\"content\":\"本规定涉及国家秘密和涉密文件的管理，严禁泄露商业秘密和财务机密。\","
                        + "\"applicantId\":2}", token).get("data");
        long docId = doc.get("id").asLong();

        JsonNode review = postJson("/api/documents/" + docId + "/ai-review", "{}", token).get("data");

        JsonNode sensitiveWords = review.get("sensitiveWords");
        assertTrue(sensitiveWords.isArray() && sensitiveWords.size() > 0,
                "Should detect sensitive words");
    }

    @Test
    void aiReviewStillBlocksInternalDocuments() throws Exception {
        String token = login("user");

        JsonNode doc = postJson("/api/documents",
                "{\"title\":\"关于内部材料的通知\",\"docType\":\"通知\",\"secrecyLevel\":\"内部\","
                        + "\"content\":\"内部材料禁止使用AI审查。\","
                        + "\"applicantId\":2}", token).get("data");
        long docId = doc.get("id").asLong();

        JsonNode review = postJson("/api/documents/" + docId + "/ai-review", "{}", token).get("data");
        assertFalse(review.get("passed").asBoolean());
    }

    // ========== AI Draft with NLP ==========

    @Test
    void aiDraftReturnsNlpEnrichedContent() throws Exception {
        String token = login("user");

        JsonNode draft = postJson("/api/documents/ai-draft",
                "{\"docType\":\"通知\",\"topic\":\"教学改革\",\"keyPoints\":\"优化课程设置，提升教学质量\"}", token).get("data");

        String content = draft.asText();
        assertTrue(content.contains("教学改革"));
        assertTrue(content.contains("通知"));
        assertTrue(content.contains("智能提取关键词"),
                "NLP draft should include keyword footer");
        assertTrue(content.contains("一、背景与目标"));
        assertTrue(content.contains("二、工作内容"));
        assertTrue(content.contains("三、工作要求"));
    }

    // ========== Enhanced Room Recommendation ==========

    @Test
    void enhancedRecommendationReturnsScoredRooms() throws Exception {
        String token = login("user");

        JsonNode result = postJson("/api/meetings/recommend/enhanced",
                "{\"expectedCount\":50,\"equipment\":\"投影仪\","
                        + "\"startTime\":\"2026-09-01T09:00:00\",\"endTime\":\"2026-09-01T11:00:00\"}", token).get("data");

        assertTrue(result.isArray(), "Should return array of scored rooms");
        assertTrue(result.size() > 0, "Should return at least one room");

        // Verify first room has scoring fields
        JsonNode first = result.get(0);
        assertTrue(first.has("room"), "Should have room object");
        assertTrue(first.has("score"), "Should have score field");
        assertTrue(first.has("capacityFit"), "Should have capacityFit field");
        assertTrue(first.has("equipmentMatch"), "Should have equipmentMatch field");
        assertTrue(first.has("utilizationBalance"), "Should have utilizationBalance field");

        double topScore = first.get("score").asDouble();
        assertTrue(topScore > 0, "Top room should have positive score");
    }

    @Test
    void enhancedRecommendationScoresBestFitFirst() throws Exception {
        String token = login("user");

        // Request for 80 people — room 1 (capacity 80) should score highest
        JsonNode result = postJson("/api/meetings/recommend/enhanced",
                "{\"expectedCount\":80,\"startTime\":\"2026-09-15T09:00:00\",\"endTime\":\"2026-09-15T11:00:00\"}",
                token).get("data");

        assertTrue(result.isArray() && result.size() > 0);
        // Rooms are sorted by score descending
        double prevScore = Double.MAX_VALUE;
        for (JsonNode scored : result) {
            double score = scored.get("score").asDouble();
            assertTrue(score <= prevScore,
                    "Rooms should be sorted by score descending");
            prevScore = score;
        }
    }

    @Test
    void enhancedRecommendationFiltersConflictingRooms() throws Exception {
        String token = login("user");

        // Create a meeting in room 1
        postJson("/api/meetings",
                "{\"title\":\"冲突测试会议\",\"roomId\":1,\"organizerId\":2,"
                        + "\"startTime\":\"2026-09-20T09:00:00\",\"endTime\":\"2026-09-20T11:00:00\","
                        + "\"venueType\":\"室内\",\"meetingType\":\"国内管理会议\",\"budget\":0,"
                        + "\"participants\":[2,3],\"recorderId\":3}", token);

        // Recommend for the same time slot — room 1 should be excluded
        JsonNode result = postJson("/api/meetings/recommend/enhanced",
                "{\"expectedCount\":10,"
                        + "\"startTime\":\"2026-09-20T09:00:00\",\"endTime\":\"2026-09-20T11:00:00\"}",
                token).get("data");

        for (JsonNode scored : result) {
            long roomId = scored.get("room").get("id").asLong();
            assertNotEquals(1, roomId, "Room 1 should be excluded due to conflict");
        }
    }

    // ========== Helper methods ==========

    private String login(String username) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("data").get("token").asText();
    }

    private JsonNode postJson(String url, String json, String token) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        String body = mockMvc.perform(request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }
}
