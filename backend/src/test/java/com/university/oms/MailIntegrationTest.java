package com.university.oms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.oms.model.Department;
import com.university.oms.model.MailRecipient;
import com.university.oms.model.Notification;
import com.university.oms.model.User;
import com.university.oms.repository.InMemoryDatabase;
import com.university.oms.service.EmailSenderService;
import com.university.oms.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "oms.mail.external-enabled=true")
@AutoConfigureMockMvc
class MailIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryDatabase db;

    @SpyBean
    private WorkflowService workflowService;

    @SpyBean
    private EmailSenderService emailSenderService;

    @Test
    void adminCreateUserRequiresEmail() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"missingemail\",\"password\":\"123456\",\"realName\":\"Missing Email\",\"deptId\":4,\"roleKeys\":\"office_user\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCreateUserRejectsInvalidEmail() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"invalidemail\",\"password\":\"123456\",\"realName\":\"Invalid Email\",\"deptId\":4,\"roleKeys\":\"office_user\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCreateUserStoresEmail() throws Exception {
        String token = loginAdmin();

        JsonNode created = postJson("/api/admin/users",
                "{\"username\":\"mailuser\",\"password\":\"123456\",\"realName\":\"Mail User\",\"deptId\":4,\"roleKeys\":\"office_user\",\"email\":\"mailuser@example.com\"}",
                token);
        long id = created.get("data").get("id").asLong();

        assertEquals("mailuser@example.com", db.users().get(id).getEmail());

        mockMvc.perform(get("/api/admin/users/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("mailuser@example.com"));
    }

    @Test
    void adminUpdateUserRejectsInvalidEmail() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(put("/api/admin/users/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"普通办公人员\",\"deptId\":4,\"roleKeys\":\"office_user\",\"email\":\"bad-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminUpdateUserRejectsBlankEmail() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(put("/api/admin/users/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"Office User\",\"deptId\":4,\"roleKeys\":\"office_user\",\"email\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminUpdateUserStoresEmail() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(put("/api/admin/users/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"普通办公人员\",\"deptId\":4,\"roleKeys\":\"office_user\",\"email\":\"new-user@example.com\"}"))
                .andExpect(status().isOk());

        assertEquals("new-user@example.com", db.users().get(2L).getEmail());
    }

    @Test
    void organizationTreeContainsDepartmentsAndUsers() throws Exception {
        String token = loginAdmin();
        Department parent = addDepartment(900001L, "Tree Parent", 0L);
        Department child = addDepartment(900002L, "Tree Child", parent.getId());
        User user = addUser(900003L, "treeuser", "Tree User", child.getId());

        try {
            JsonNode tree = getOrganizationTree(token);
            JsonNode parentNode = findDirectNode(tree, "dept-" + parent.getId());
            assertNotNull(parentNode);
            JsonNode childNode = findDirectNode(parentNode.get("children"), "dept-" + child.getId());
            assertNotNull(childNode);
            JsonNode userNode = findDirectNode(childNode.get("children"), "user-" + user.getId());

            assertEquals("dept", parentNode.get("type").asText());
            assertEquals("dept", childNode.get("type").asText());
            assertNotNull(userNode);
            assertEquals("user", userNode.get("type").asText());
            assertEquals(user.getId().longValue(), userNode.get("userId").asLong());
            assertEquals(user.getEmail(), userNode.get("email").asText());
        } finally {
            db.users().remove(user.getId());
            db.departments().remove(child.getId());
            db.departments().remove(parent.getId());
        }
    }

    @Test
    void organizationTreeFallsBackToRootsForMalformedAndCyclicDepartments() throws Exception {
        String token = loginAdmin();
        Department missingParent = addDepartment(900011L, "Missing Parent", 999999L);
        Department selfParent = addDepartment(900012L, "Self Parent", 900012L);
        Department cycleA = addDepartment(900013L, "Cycle A", 900014L);
        Department cycleB = addDepartment(900014L, "Cycle B", 900013L);
        Department cycleChild = addDepartment(900015L, "Cycle Child", 900013L);

        try {
            JsonNode tree = getOrganizationTree(token);

            assertRootNode(tree, missingParent);
            assertRootNode(tree, selfParent);
            assertRootNode(tree, cycleA);
            assertRootNode(tree, cycleB);
            assertRootNode(tree, cycleChild);
            assertEquals(1, countNodes(tree, "dept-" + missingParent.getId()));
            assertEquals(1, countNodes(tree, "dept-" + selfParent.getId()));
            assertEquals(1, countNodes(tree, "dept-" + cycleA.getId()));
            assertEquals(1, countNodes(tree, "dept-" + cycleB.getId()));
            assertEquals(1, countNodes(tree, "dept-" + cycleChild.getId()));
        } finally {
            db.departments().remove(cycleChild.getId());
            db.departments().remove(cycleB.getId());
            db.departments().remove(cycleA.getId());
            db.departments().remove(selfParent.getId());
            db.departments().remove(missingParent.getId());
        }
    }

    @Test
    void organizationTreeSortsDepartmentsAndUsersByLabelThenId() throws Exception {
        String token = loginAdmin();
        Department zDepartment = addDepartment(900021L, "Z Department", 0L);
        Department aDepartment = addDepartment(900022L, "A Department", 0L);
        User zUser = addUser(900023L, "zuser", "Z User", aDepartment.getId());
        User aUser = addUser(900024L, "auser", "A User", aDepartment.getId());

        try {
            JsonNode tree = getOrganizationTree(token);
            JsonNode aDepartmentNode = findDirectNode(tree, "dept-" + aDepartment.getId());
            assertNotNull(aDepartmentNode);

            assertBefore(tree, "dept-" + aDepartment.getId(), "dept-" + zDepartment.getId());
            assertBefore(aDepartmentNode.get("children"), "user-" + aUser.getId(), "user-" + zUser.getId());
        } finally {
            db.users().remove(aUser.getId());
            db.users().remove(zUser.getId());
            db.departments().remove(aDepartment.getId());
            db.departments().remove(zDepartment.getId());
        }
    }

    @Test
    void sendMailAppearsInInboxAndSentAndNotifiesRecipients() throws Exception {
        String adminToken = login("admin");
        String userToken = login("user");
        String subject = "Mail happy path " + System.nanoTime();

        JsonNode sent = postJson("/api/mails",
                "{\"subject\":\"  " + subject + "  \",\"content\":\"  Please review  \",\"toUserIds\":[2],\"ccUserIds\":[3]}",
                adminToken).get("data");
        long mailId = sent.get("id").asLong();

        assertEquals(subject, sent.get("subject").asText());
        assertEquals("Please review", sent.get("content").asText());
        assertEquals(1L, sent.get("senderId").asLong());
        assertEquals(2, sent.get("recipients").size());
        assertFalse(sent.get("recipients").get(0).has("email"));
        assertTrue(hasRecipient(sent.get("recipients"), 2L, "to", "skipped"));
        assertTrue(hasRecipient(sent.get("recipients"), 3L, "cc", "skipped"));

        JsonNode inbox = getJson("/api/mails/inbox", userToken).get("data");
        JsonNode received = findMail(inbox, mailId);
        assertNotNull(received);
        assertEquals(mailId, inbox.get(0).get("id").asLong());
        assertEquals("to", received.get("currentUserRecipientType").asText());
        assertFalse(received.get("currentUserRead").asBoolean());

        JsonNode sentList = getJson("/api/mails/sent", adminToken).get("data");
        JsonNode senderCopy = findMail(sentList, mailId);
        assertNotNull(senderCopy);
        assertEquals(mailId, sentList.get(0).get("id").asLong());
        assertEquals("sender", senderCopy.get("currentUserRecipientType").asText());
        assertTrue(senderCopy.get("currentUserRead").asBoolean());
        assertTrue(db.notifications().stream().anyMatch(n -> isMailNotification(n, 2L, mailId)));
        assertTrue(db.notifications().stream().anyMatch(n -> isMailNotification(n, 3L, mailId)));
    }

    @Test
    void mailDetailIsVisibleOnlyToSenderOrRecipient() throws Exception {
        String adminToken = login("admin");
        String userToken = login("user");
        String financeToken = login("finance");
        long mailId = sendMail(adminToken, "Detail access " + System.nanoTime(), "[2]", "[]");

        mockMvc.perform(get("/api/mails/" + mailId).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(mailId));
        mockMvc.perform(get("/api/mails/" + mailId).header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentUserRecipientType").value("to"));
        mockMvc.perform(get("/api/mails/" + mailId).header("Authorization", "Bearer " + financeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void recipientCanMarkMailReadButOtherUsersCannot() throws Exception {
        String adminToken = login("admin");
        String userToken = login("user");
        String financeToken = login("finance");
        long mailId = sendMail(adminToken, "Mark read " + System.nanoTime(), "[2]", "[]");

        mockMvc.perform(post("/api/mails/" + mailId + "/read")
                        .header("Authorization", "Bearer " + financeToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/mails/" + mailId + "/read")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());

        JsonNode marked = postJson("/api/mails/" + mailId + "/read", "{}", userToken).get("data");
        assertTrue(marked.get("currentUserRead").asBoolean());
        MailRecipient recipient = findRecipient(mailId, 2L);
        assertNotNull(recipient);
        assertTrue(recipient.isReadStatus());
        assertNotNull(recipient.getReadAt());
    }

    @Test
    void sendMailRequiresToRecipientsAndKnownUsers() throws Exception {
        String adminToken = login("admin");

        mockMvc.perform(post("/api/mails")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subject\":\"No recipient\",\"content\":\"Body\",\"toUserIds\":[],\"ccUserIds\":[2]}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/mails")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subject\":\"Unknown recipient\",\"content\":\"Body\",\"toUserIds\":[999999],\"ccUserIds\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendMailDeduplicatesRecipientsWithToWinningOverCc() throws Exception {
        String adminToken = login("admin");
        long mailId = sendMail(adminToken, "Dedupe " + System.nanoTime(), "[2,2,3]", "[2,3,4,4]");

        assertEquals(3, mailRecipientsSnapshot().stream().filter(r -> mailId == r.getMailId()).count());
        assertEquals("to", findRecipient(mailId, 2L).getRecipientType());
        assertEquals("to", findRecipient(mailId, 3L).getRecipientType());
        assertEquals("cc", findRecipient(mailId, 4L).getRecipientType());
    }

    @Test
    void mailNotificationUsesBoundedSummaryForLongMail() throws Exception {
        String adminToken = login("admin");
        String subject = repeat("S", 255);
        String content = repeat("Long body ", 300);

        long mailId = sendMailWithContent(adminToken, subject, content, "[2]", "[]");
        Notification notification = findMailNotification(2L, mailId);

        assertNotNull(notification);
        assertTrue(notification.getTitle().length() <= 255);
        assertTrue(notification.getContent().length() <= 1000);
        assertFalse(notification.getContent().contains(content));
    }

    @Test
    void notificationFailureDoesNotAbortInternalMailRecipients() throws Exception {
        String adminToken = login("admin");
        doThrow(new RuntimeException("notification unavailable"))
                .when(workflowService)
                .notifyUser(anyLong(), anyString(), anyString(), eq("mail"), anyLong());

        long mailId = sendMail(adminToken, "Notification failure " + System.nanoTime(), "[2,3]", "[4]");

        assertEquals(3, mailRecipientsSnapshot().stream().filter(r -> r.getMailId().equals(mailId)).count());
        assertNotNull(db.mailMessages().get(mailId));
    }

    @Test
    void recipientCannotSeeOtherRecipientsDeliveryOrReadStatus() throws Exception {
        String adminToken = login("admin");
        String userToken = login("user");
        long mailId = sendMail(adminToken, "Private statuses " + System.nanoTime(), "[2,3]", "[]");
        MailRecipient otherRecipient = findRecipient(mailId, 3L);
        otherRecipient.setReadStatus(true);
        otherRecipient.setEmailStatus("failed");
        otherRecipient.setEmailError("private delivery error");
        otherRecipient.setEmailSentAt(LocalDateTime.now());

        JsonNode recipientDetail = getJson("/api/mails/" + mailId, userToken).get("data");
        JsonNode senderDetail = getJson("/api/mails/" + mailId, adminToken).get("data");

        assertFalse(hasRecipientUser(recipientDetail.get("recipients"), 3L));
        JsonNode senderOtherRecipient = findRecipientNode(senderDetail.get("recipients"), 3L);
        assertNotNull(senderOtherRecipient);
        assertTrue(senderOtherRecipient.get("readStatus").asBoolean());
        assertEquals("failed", senderOtherRecipient.get("emailStatus").asText());
        assertEquals("private delivery error", senderOtherRecipient.get("emailError").asText());
        assertNotNull(senderOtherRecipient.get("emailSentAt"));
    }

    @Test
    void selfSentMailKeepsSenderSemanticsOutsideInbox() throws Exception {
        String adminToken = login("admin");
        long mailId = sendMail(adminToken, "Self sent " + System.nanoTime(), "[1]", "[]");

        JsonNode sent = findMail(getJson("/api/mails/sent", adminToken).get("data"), mailId);
        JsonNode detail = getJson("/api/mails/" + mailId, adminToken).get("data");
        JsonNode inbox = findMail(getJson("/api/mails/inbox", adminToken).get("data"), mailId);

        assertEquals("sender", sent.get("currentUserRecipientType").asText());
        assertTrue(sent.get("currentUserRead").asBoolean());
        assertEquals("sender", detail.get("currentUserRecipientType").asText());
        assertTrue(detail.get("currentUserRead").asBoolean());
        assertEquals("to", inbox.get("currentUserRecipientType").asText());
        assertFalse(inbox.get("currentUserRead").asBoolean());
    }

    @Test
    void externalEmailDisabledMarksRecipientsSkipped() throws Exception {
        String adminToken = login("admin");

        JsonNode sent = postJson("/api/mails",
                "{\"subject\":\"External disabled " + System.nanoTime()
                        + "\",\"content\":\"Body\",\"toUserIds\":[2],\"ccUserIds\":[]}",
                adminToken).get("data");
        long mailId = sent.get("id").asLong();
        JsonNode recipient = sent.get("recipients").get(0);

        assertEquals("skipped", recipient.get("emailStatus").asText());
        assertTrue(recipient.get("emailError").asText().contains("configuration"));
        MailRecipient stored = findRecipient(mailId, 2L);
        assertNotNull(stored);
        assertEquals("skipped", stored.getEmailStatus());
        assertTrue(stored.getEmailError().contains("configuration"));
    }

    @Test
    void externalFailureMarksFailedWithoutLosingInternalMail() throws Exception {
        String adminToken = login("admin");
        doReturn(true).when(emailSenderService).isEnabled();
        doThrow(new RuntimeException("smtp down"))
                .when(emailSenderService)
                .sendMail(anyString(), anyString(), anyString());

        long mailId = sendMail(adminToken, "External failure " + System.nanoTime(), "[2]", "[]");

        MailRecipient recipient = findRecipient(mailId, 2L);
        assertNotNull(db.mailMessages().get(mailId));
        assertNotNull(recipient);
        assertEquals("failed", recipient.getEmailStatus());
        assertTrue(recipient.getEmailError().contains("smtp down"));
    }

    @Test
    void senderCanRetryFailedOrSkippedEmail() throws Exception {
        String adminToken = login("admin");
        long mailId = sendMail(adminToken, "Retry sender " + System.nanoTime(), "[2]", "[]");
        MailRecipient recipient = findRecipient(mailId, 2L);
        recipient.setEmailStatus("failed");
        recipient.setEmailError("smtp unavailable");

        JsonNode retried = postJson("/api/mails/" + mailId + "/retry-email", "{}", adminToken).get("data");

        assertTrue(hasRecipient(retried.get("recipients"), 2L, "to", "skipped"));
        assertEquals("skipped", findRecipient(mailId, 2L).getEmailStatus());
    }

    @Test
    void nonSenderNonAdminCannotRetryEmail() throws Exception {
        String adminToken = login("admin");
        String financeToken = login("finance");
        long mailId = sendMail(adminToken, "Retry forbidden " + System.nanoTime(), "[2]", "[]");

        mockMvc.perform(post("/api/mails/" + mailId + "/retry-email")
                        .header("Authorization", "Bearer " + financeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanRetryEmail() throws Exception {
        String userToken = login("user");
        String adminToken = login("admin");
        long mailId = sendMail(userToken, "Retry admin " + System.nanoTime(), "[3]", "[]");
        MailRecipient recipient = findRecipient(mailId, 3L);
        recipient.setEmailStatus("failed");
        recipient.setEmailError("smtp unavailable");

        JsonNode retried = postJson("/api/mails/" + mailId + "/retry-email", "{}", adminToken).get("data");

        assertEquals(0, retried.get("recipients").size());
        assertTrue(retried.get("content").isNull());
        assertEquals("skipped", findRecipient(mailId, 3L).getEmailStatus());
    }

    @Test
    void adminRetryDoesNotExposeRecipientPrivateStatuses() throws Exception {
        String userToken = login("user");
        String adminToken = login("admin");
        long mailId = sendMail(userToken, "Retry admin privacy " + System.nanoTime(), "[3,4]", "[]");
        MailRecipient first = findRecipient(mailId, 3L);
        MailRecipient second = findRecipient(mailId, 4L);
        first.setReadStatus(true);
        first.setEmailStatus("failed");
        first.setEmailError("private smtp error one");
        second.setEmailStatus("failed");
        second.setEmailError("private smtp error two");

        JsonNode retried = postJson("/api/mails/" + mailId + "/retry-email", "{}", adminToken).get("data");

        assertTrue(retried.get("content").isNull());
        assertEquals(0, retried.get("recipients").size());
        assertEquals("skipped", findRecipient(mailId, 3L).getEmailStatus());
        assertEquals("skipped", findRecipient(mailId, 4L).getEmailStatus());
    }

    private JsonNode getOrganizationTree(String token) throws Exception {
        String body = mockMvc.perform(get("/api/org/tree")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("data");
    }

    private Department addDepartment(Long id, String name, Long parentId) {
        Department department = new Department();
        db.fill(department, id);
        department.setDeptName(name);
        department.setParentId(parentId);
        db.departments().put(id, department);
        return department;
    }

    private User addUser(Long id, String username, String realName, Long deptId) {
        User user = new User();
        db.fill(user, id);
        user.setUsername(username);
        user.setRealName(realName);
        user.setEmail(username + "@example.com");
        user.setDeptId(deptId);
        user.setDeptName(db.departments().get(deptId).getDeptName());
        db.users().put(id, user);
        return user;
    }

    private void assertRootNode(JsonNode tree, Department department) {
        assertNotNull(findDirectNode(tree, "dept-" + department.getId()));
    }

    private JsonNode findDirectNode(JsonNode nodes, String id) {
        if (nodes == null || !nodes.isArray()) {
            return null;
        }
        for (JsonNode node : nodes) {
            if (id.equals(node.path("id").asText())) {
                return node;
            }
        }
        return null;
    }

    private int countNodes(JsonNode nodes, String id) {
        if (nodes == null || !nodes.isArray()) {
            return 0;
        }
        int count = 0;
        for (JsonNode node : nodes) {
            if (id.equals(node.path("id").asText())) {
                count++;
            }
            count += countNodes(node.get("children"), id);
        }
        return count;
    }

    private int indexOfNode(JsonNode nodes, String id) {
        if (nodes == null || !nodes.isArray()) {
            return -1;
        }
        for (int i = 0; i < nodes.size(); i++) {
            if (id.equals(nodes.get(i).path("id").asText())) {
                return i;
            }
        }
        return -1;
    }

    private void assertBefore(JsonNode nodes, String firstId, String secondId) {
        int firstIndex = indexOfNode(nodes, firstId);
        int secondIndex = indexOfNode(nodes, secondId);
        assertTrue(firstIndex >= 0);
        assertTrue(secondIndex >= 0);
        assertTrue(firstIndex < secondIndex);
    }

    private long sendMail(String token, String subject, String toUserIds, String ccUserIds) throws Exception {
        return sendMailWithContent(token, subject, "Body", toUserIds, ccUserIds);
    }

    private long sendMailWithContent(String token, String subject, String content, String toUserIds, String ccUserIds)
            throws Exception {
        JsonNode response = postJson("/api/mails",
                "{\"subject\":\"" + subject + "\",\"content\":\"" + content + "\",\"toUserIds\":" + toUserIds
                        + ",\"ccUserIds\":" + ccUserIds + "}",
                token);
        return response.get("data").get("id").asLong();
    }

    private JsonNode getJson(String url, String token) throws Exception {
        String body = mockMvc.perform(get(url).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private JsonNode findMail(JsonNode mails, long mailId) {
        if (mails == null || !mails.isArray()) {
            return null;
        }
        for (JsonNode mail : mails) {
            if (mail.path("id").asLong() == mailId) {
                return mail;
            }
        }
        return null;
    }

    private boolean hasRecipient(JsonNode recipients, long userId, String recipientType, String emailStatus) {
        if (recipients == null || !recipients.isArray()) {
            return false;
        }
        for (JsonNode recipient : recipients) {
            if (recipient.path("userId").asLong() == userId
                    && recipientType.equals(recipient.path("recipientType").asText())
                    && emailStatus.equals(recipient.path("emailStatus").asText())) {
                return true;
            }
        }
        return false;
    }

    private MailRecipient findRecipient(long mailId, long userId) {
        return mailRecipientsSnapshot().stream()
                .filter(r -> r.getMailId().equals(mailId) && r.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    private List<MailRecipient> mailRecipientsSnapshot() {
        synchronized (db.mailRecipients()) {
            return new ArrayList<MailRecipient>(db.mailRecipients());
        }
    }

    private boolean hasRecipientUser(JsonNode recipients, long userId) {
        return findRecipientNode(recipients, userId) != null;
    }

    private JsonNode findRecipientNode(JsonNode recipients, long userId) {
        if (recipients == null || !recipients.isArray()) {
            return null;
        }
        for (JsonNode recipient : recipients) {
            if (recipient.path("userId").asLong() == userId) {
                return recipient;
            }
        }
        return null;
    }

    private Notification findMailNotification(long receiverId, long mailId) {
        return db.notifications().stream()
                .filter(notification -> isMailNotification(notification, receiverId, mailId))
                .findFirst()
                .orElse(null);
    }

    private String repeat(String value, int count) {
        StringBuilder result = new StringBuilder(value.length() * count);
        for (int i = 0; i < count; i++) {
            result.append(value);
        }
        return result.toString();
    }

    private boolean isMailNotification(Notification notification, long receiverId, long mailId) {
        return notification.getReceiverId().equals(receiverId)
                && "mail".equals(notification.getBizType())
                && notification.getBizId().equals(mailId)
                && notification.getTitle().contains("新邮件");
    }

    private String loginAdmin() throws Exception {
        return login("admin");
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
}
