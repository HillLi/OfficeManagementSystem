package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "oms.upload-dir=target/test-uploads/seal-material")
class SealMaterialManagementTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void creatingSealApplicationCreatesDraftWithReadableSealName() throws Exception {
        String token = login("user");
        JsonNode application = createDraft(token);

        assertEquals("draft", application.get("status").asText());
        assertEquals("信息科学技术学院公章", application.get("sealName").asText());
        assertEquals(0, application.get("materialCount").asInt());
    }

    @Test
    void draftCannotBeSubmittedWithoutUploadedMaterial() throws Exception {
        String token = login("user");
        long id = createDraft(token).get("id").asLong();

        mockMvc.perform(post("/api/seals/applications/" + id + "/submit")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadedMaterialCanBeDownloadedAndAllowsDraftSubmission() throws Exception {
        String token = login("user");
        long applicationId = createDraft(token).get("id").asLong();
        long attachmentId = uploadPdf(applicationId, token);

        mockMvc.perform(get("/api/workflow/attachments/" + attachmentId + "/download")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(content().bytes("seal-contract".getBytes(StandardCharsets.UTF_8)));

        JsonNode submitted = postJson("/api/seals/applications/" + applicationId + "/submit", "{}", token).get("data");
        assertEquals("pending_dept", submitted.get("status").asText());
        assertEquals(1, submitted.get("materialCount").asInt());
    }

    @Test
    void uploadRejectsDisallowedAndOversizeSealMaterial() throws Exception {
        String token = login("user");
        long applicationId = createDraft(token).get("id").asLong();
        MockMultipartFile executable = new MockMultipartFile("file", "script.exe",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "bad".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/workflow/attachments/upload")
                        .file(executable).param("bizType", "seal")
                        .param("bizId", String.valueOf(applicationId))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());

        MockMultipartFile oversized = new MockMultipartFile("file", "large.pdf",
                MediaType.APPLICATION_PDF_VALUE, new byte[20 * 1024 * 1024 + 1]);
        mockMvc.perform(multipart("/api/workflow/attachments/upload")
                        .file(oversized).param("bizType", "seal")
                        .param("bizId", String.valueOf(applicationId))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sealMaterialCannotBeAddedAsUnmanagedFileUrl() throws Exception {
        String token = login("user");
        long applicationId = createDraft(token).get("id").asLong();

        mockMvc.perform(post("/api/workflow/attachments")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bizType\":\"seal\",\"bizId\":" + applicationId
                                + ",\"fileName\":\"shortcut.pdf\",\"fileUrl\":\"/unmanaged/shortcut.pdf\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void draftOwnerCanEditAndLogicallyDeleteMaterialWithAuditTrail() throws Exception {
        String user = login("user");
        long applicationId = createDraft(user).get("id").asLong();
        long attachmentId = uploadPdf(applicationId, user);

        mockMvc.perform(put("/api/workflow/attachments/" + attachmentId)
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"合同定稿.pdf\",\"secrecyLevel\":\"内部\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/workflow/attachments/" + attachmentId)
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"上传版本错误\"}"))
                .andExpect(status().isOk());

        JsonNode visible = getJson("/api/workflow/attachments?bizType=seal&bizId=" + applicationId, user).get("data");
        assertEquals(0, visible.size());
        JsonNode logs = getJson("/api/workflow/audit-logs?bizType=seal&bizId=" + applicationId, login("admin")).get("data");
        assertTrue(containsAction(logs, "delete_attachment"));
    }

    @Test
    void applicantCannotDeleteSubmittedMaterialAndUnrelatedUserCannotDownloadIt() throws Exception {
        String owner = login("user");
        String finance = login("finance");
        long applicationId = createDraft(owner).get("id").asLong();
        long attachmentId = uploadPdf(applicationId, owner);
        postJson("/api/seals/applications/" + applicationId + "/submit", "{}", owner);

        mockMvc.perform(delete("/api/workflow/attachments/" + attachmentId)
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"试图删除审批依据\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/workflow/attachments/" + attachmentId + "/download")
                        .header("Authorization", bearer(finance)))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCanSeeDeletedMetadataButCannotDownloadRemovedMaterial() throws Exception {
        String owner = login("user");
        String admin = login("admin");
        long applicationId = createDraft(owner).get("id").asLong();
        long attachmentId = uploadPdf(applicationId, owner);

        mockMvc.perform(delete("/api/workflow/attachments/" + attachmentId)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"失效材料留档\"}"))
                .andExpect(status().isOk());
        JsonNode deleted = getJson("/api/workflow/attachments?bizType=seal&bizId=" + applicationId
                + "&includeDeleted=true", admin).get("data");
        assertTrue(deleted.get(0).get("deleted").asBoolean());
        mockMvc.perform(get("/api/workflow/attachments/" + attachmentId + "/download")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());
    }

    private JsonNode createDraft(String token) throws Exception {
        return postJson("/api/seals/applications",
                "{\"sealId\":2,\"applicantId\":2,\"purpose\":\"材料管理测试\",\"copies\":1,"
                        + "\"takeOut\":false,\"matterLevel\":\"常规事项\"}", token).get("data");
    }

    private long uploadPdf(long applicationId, String token) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", MediaType.APPLICATION_PDF_VALUE,
                "seal-contract".getBytes(StandardCharsets.UTF_8));
        String body = mockMvc.perform(multipart("/api/workflow/attachments/upload")
                        .file(file).param("bizType", "seal").param("bizId", String.valueOf(applicationId))
                        .param("secrecyLevel", "内部").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body).get("data").get("id").asLong();
    }

    private boolean containsAction(JsonNode rows, String action) {
        for (JsonNode row : rows) {
            if (action.equals(row.get("action").asText())) {
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
        MockHttpServletRequestBuilder request = post(url).contentType(MediaType.APPLICATION_JSON).content(json);
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
