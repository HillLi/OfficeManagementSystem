package com.university.oms.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.oms.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class OmsRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public OmsRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        ensureNextIdAtLeast();
    }

    // ========== ID Generation ==========

    public synchronized long nextId() {
        Long next = jdbcTemplate.queryForObject("SELECT next_id FROM oms_sequence", Long.class);
        jdbcTemplate.update("UPDATE oms_sequence SET next_id = next_id + 1");
        return next;
    }

    private void ensureNextIdAtLeast() {
        long max = maxId();
        long target = max + 1;
        Long current = jdbcTemplate.queryForObject("SELECT MIN(next_id) FROM oms_sequence", Long.class);
        if (current == null) {
            jdbcTemplate.update("INSERT INTO oms_sequence (next_id) VALUES (?)", target);
        } else if (current < target) {
            jdbcTemplate.update("UPDATE oms_sequence SET next_id = ? WHERE next_id = ?", target, current);
        }
    }

    private long maxId() {
        Long value = jdbcTemplate.queryForObject(
                "SELECT GREATEST(" +
                        "COALESCE((SELECT MAX(id) FROM oa_document),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_document_distribution),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_seal_log),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_seal_transfer),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_meeting),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_travel),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_report),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_approval_history),0)," +
                        "COALESCE((SELECT MAX(id) FROM sys_attachment),0)," +
                        "COALESCE((SELECT MAX(id) FROM sys_operation_log),0)," +
                        "COALESCE((SELECT MAX(id) FROM sys_notification),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_mail_message),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_mail_recipient),0)," +
                        "COALESCE((SELECT MAX(id) FROM sys_announcement),0)," +
                        "COALESCE((SELECT MAX(id) FROM sys_dict_type),0)," +
                        "COALESCE((SELECT MAX(id) FROM sys_dict_item),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_flow_instance),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_flow_task),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_flow_node),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_meeting_participant),0)," +
                        "1000)",
                Long.class);
        return value == null ? 1000L : value;
    }

    // ========== Utility ==========

    public static void fillEntity(BaseEntity entity, Long id) {
        entity.setId(id);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
    }

    public static String dictionaryItemKey(String type, String code) {
        return type.length() + ":" + type + code;
    }

    private LocalDateTime toLocalDateTime(ResultSet rs, String column) throws SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON序列化失败", e);
        }
    }

    // ========== Write Methods: User ==========

    public void saveUser(User u) {
        jdbcTemplate.update("REPLACE INTO sys_user (id, username, password, real_name, dept_id, email, phone, status, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,1,?,?)",
                u.getId(), u.getUsername(), u.getPassword(), u.getRealName(), u.getDeptId(), u.getEmail(), null, u.getCreatedAt(), u.getUpdatedAt());
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id=?", u.getId());
        for (String roleKey : u.getRoleKeys()) {
            jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) SELECT ?, id FROM sys_role WHERE role_key=?",
                    u.getId(), roleKey);
        }
    }

    public void deleteUser(Long id) {
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id=?", id);
        jdbcTemplate.update("DELETE FROM sys_user WHERE id=?", id);
    }

    // ========== Write Methods: Department ==========

    public void saveDepartment(Department d) {
        jdbcTemplate.update("REPLACE INTO sys_dept (id, dept_name, parent_id, status) VALUES (?,?,?,1)",
                d.getId(), d.getDeptName(), d.getParentId());
    }

    public void deleteDepartment(Long id) {
        jdbcTemplate.update("DELETE FROM sys_dept WHERE id=?", id);
    }

    // ========== Write Methods: Dictionary ==========

    public void saveDictionaryType(DictionaryType type) {
        jdbcTemplate.update("REPLACE INTO sys_dict_type " +
                        "(id, dict_type, dict_name, system_type, enabled, remark, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?)",
                type.getId(), type.getDictType(), type.getDictName(), type.isSystemType() ? 1 : 0,
                type.isEnabled() ? 1 : 0, type.getRemark(), type.getCreatedAt(), type.getUpdatedAt());
    }

    public void saveDictionaryItem(DictionaryItem item) {
        jdbcTemplate.update("REPLACE INTO sys_dict_item " +
                        "(id, dict_type, dict_code, dict_label, sort_order, enabled, system_item, remark, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)",
                item.getId(), item.getDictType(), item.getDictCode(), item.getDictLabel(), item.getSortOrder(),
                item.isEnabled() ? 1 : 0, item.isSystemItem() ? 1 : 0, item.getRemark(),
                item.getCreatedAt(), item.getUpdatedAt());
    }

    // ========== Write Methods: Document ==========

    public void saveDocument(Document d) {
        jdbcTemplate.update("REPLACE INTO oa_document " +
                        "(id, doc_no, title, doc_type, urgency, secrecy_level, knowledge_scope, content, applicant_id, dept_id, status, version, distribution_status, ai_review_result, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                d.getId(), d.getDocNo(), d.getTitle(), d.getDocType(), d.getUrgency(), d.getSecrecyLevel(),
                d.getKnowledgeScope(), d.getContent(), d.getApplicantId(), d.getDeptId(), d.getStatus(),
                d.getVersion(), d.getDistributionStatus(), toJson(d.getAiReviewResult()), d.getCreatedAt(), d.getUpdatedAt());
    }

    public void saveDocumentDistribution(DocumentDistribution d) {
        jdbcTemplate.update("REPLACE INTO oa_document_distribution " +
                        "(id, document_id, receiver_id, receiver_dept_id, status, distributed_at, received_at, reminded_at, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)",
                d.getId(), d.getDocumentId(), d.getReceiverId(), d.getReceiverDeptId(), d.getStatus(),
                d.getDistributedAt(), d.getReceivedAt(), d.getRemindedAt(), d.getCreatedAt(), d.getUpdatedAt());
    }

    // ========== Write Methods: Seal ==========

    public void saveSealApplication(SealApplication a) {
        jdbcTemplate.update("REPLACE INTO oa_seal_log " +
                        "(id, seal_id, applicant_id, purpose, material_url, copies, take_out, matter_level, take_out_reason, take_out_location, supervisor_id, return_deadline, retention_until, use_time, return_time, status, created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                a.getId(), a.getSealId(), a.getApplicantId(), a.getPurpose(), a.getMaterialUrl(), a.getCopies(),
                a.isTakeOut() ? 1 : 0, a.getMatterLevel(), a.getTakeOutReason(), a.getTakeOutLocation(),
                a.getSupervisorId(), a.getReturnDeadline(), a.getRetentionUntil(), a.getUseTime(), a.getReturnTime(), a.getStatus(), a.getCreatedAt());
    }

    public void saveSeal(Seal s) {
        jdbcTemplate.update("REPLACE INTO oa_seal (id, seal_name, seal_type, dept_id, keeper_id, status) VALUES (?,?,?,?,?,?)",
                s.getId(), s.getSealName(), s.getSealType(), s.getDeptId(), s.getKeeperId(), s.getStatus());
    }

    public void saveSealTransfer(SealTransfer t) {
        jdbcTemplate.update("REPLACE INTO oa_seal_transfer " +
                        "(id, seal_id, transferor_id, receiver_id, supervisor_id, material_url, remark, transfer_time, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)",
                t.getId(), t.getSealId(), t.getTransferorId(), t.getReceiverId(), t.getSupervisorId(),
                t.getMaterialUrl(), t.getRemark(), t.getTransferTime(), t.getCreatedAt(), t.getUpdatedAt());
    }

    // ========== Write Methods: Meeting ==========

    public void saveMeeting(Meeting m) {
        jdbcTemplate.update("REPLACE INTO oa_meeting " +
                        "(id, title, room_id, start_time, end_time, organizer_id, expected_count, venue_type, meeting_type, budget, accommodation_fee, meal_fee, venue_fee, other_fee, risk_report_url, security_plan_url, emergency_plan_url, large_activity, sign_in_count, minutes, status, recorder_id, created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                m.getId(), m.getTitle(), m.getRoomId(), m.getStartTime(), m.getEndTime(), m.getOrganizerId(),
                m.getExpectedCount(), m.getVenueType(), m.getMeetingType(), m.getBudget(), m.getAccommodationFee(),
                m.getMealFee(), m.getVenueFee(), m.getOtherFee(), m.getRiskReportUrl(), m.getSecurityPlanUrl(),
                m.getEmergencyPlanUrl(), m.isLargeActivity() ? 1 : 0, m.getSignInCount(),
                m.getMinutes(), m.getStatus(), m.getRecorderId(), m.getCreatedAt());
    }

    public void saveMeetingParticipant(MeetingParticipant p) {
        jdbcTemplate.update(
                "REPLACE INTO oa_meeting_participant (id, meeting_id, user_id, is_recorder, minutes_confirmed, confirmed_at, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?)",
                p.getId(), p.getMeetingId(), p.getUserId(), p.isRecorder() ? 1 : 0,
                p.isMinutesConfirmed() ? 1 : 0, p.getConfirmedAt(), p.getCreatedAt(), p.getUpdatedAt());
    }

    // ========== Write Methods: Travel ==========

    public void saveTravel(Travel t) {
        jdbcTemplate.update("REPLACE INTO oa_travel " +
                        "(id, applicant_id, destination, start_date, end_date, reason, staff_level, travel_type, transport, budget, actual_expense, receipt_url, over_limit_reason, reimbursement_submitted, status, created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                t.getId(), t.getApplicantId(), t.getDestination(), t.getStartDate(), t.getEndDate(), t.getReason(),
                t.getStaffLevel(), t.getTravelType(), t.getTransport(), t.getBudget(), t.getActualExpense(),
                t.getReceiptUrl(), t.getOverLimitReason(), t.isReimbursementSubmitted() ? 1 : 0, t.getStatus(), t.getCreatedAt());
    }

    // ========== Write Methods: Report ==========

    public void saveReport(Report r) {
        jdbcTemplate.update("REPLACE INTO oa_report " +
                        "(id, title, type, secrecy_level, applicant_id, dept_id, content, reply, status, created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)",
                r.getId(), r.getTitle(), r.getType(), r.getSecrecyLevel(), r.getApplicantId(), r.getDeptId(),
                r.getContent(), r.getReply(), r.getStatus(), r.getCreatedAt());
    }

    // ========== Write Methods: Approval ==========

    public void saveApproval(ApprovalRecord r) {
        jdbcTemplate.update("REPLACE INTO oa_approval_history (id, biz_type, biz_id, operator_id, action, opinion, operated_at) VALUES (?,?,?,?,?,?,?)",
                r.getId(), r.getBizType(), r.getBizId(), r.getOperatorId(), r.getAction(), r.getOpinion(), r.getCreatedAt());
    }

    // ========== Write Methods: Attachment ==========

    public void saveAttachment(Attachment a) {
        jdbcTemplate.update("REPLACE INTO sys_attachment " +
                        "(id, biz_type, biz_id, file_name, original_name, file_url, storage_path, file_size, content_type, " +
                        "secrecy_level, uploader_id, deleted, deleted_by, deleted_at, delete_reason, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                a.getId(), a.getBizType(), a.getBizId(), a.getFileName(), a.getOriginalName(), a.getFileUrl(),
                a.getStoragePath(), a.getFileSize(), a.getContentType(), a.getSecrecyLevel(), a.getUploaderId(),
                a.isDeleted() ? 1 : 0, a.getDeletedBy(), a.getDeletedAt(), a.getDeleteReason(),
                a.getCreatedAt(), a.getUpdatedAt());
    }

    // ========== Write Methods: AuditLog ==========

    public void saveAuditLog(AuditLog l) {
        jdbcTemplate.update("REPLACE INTO sys_operation_log " +
                        "(id, operator_id, module, action, biz_type, biz_id, detail, created_at) VALUES (?,?,?,?,?,?,?,?)",
                l.getId(), l.getOperatorId(), l.getModule(), l.getAction(), l.getBizType(), l.getBizId(),
                l.getDetail(), l.getCreatedAt());
    }

    // ========== Write Methods: Notification ==========

    public void saveNotification(Notification n) {
        jdbcTemplate.update("REPLACE INTO sys_notification " +
                        "(id, receiver_id, title, content, read_status, biz_type, biz_id, created_at) VALUES (?,?,?,?,?,?,?,?)",
                n.getId(), n.getReceiverId(), n.getTitle(), n.getContent(), n.isReadStatus() ? 1 : 0,
                n.getBizType(), n.getBizId(), n.getCreatedAt());
    }

    // ========== Write Methods: Mail ==========

    public void saveMailMessage(MailMessage m) {
        jdbcTemplate.update("REPLACE INTO oa_mail_message " +
                        "(id, sender_id, subject, content, created_at, updated_at) VALUES (?,?,?,?,?,?)",
                m.getId(), m.getSenderId(), m.getSubject(), m.getContent(), m.getCreatedAt(), m.getUpdatedAt());
    }

    public void saveMailRecipient(MailRecipient r) {
        jdbcTemplate.update("REPLACE INTO oa_mail_recipient " +
                        "(id, mail_id, user_id, recipient_type, read_status, read_at, email_status, email_error, email_sent_at, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                r.getId(), r.getMailId(), r.getUserId(), r.getRecipientType(), r.isReadStatus() ? 1 : 0,
                r.getReadAt(), r.getEmailStatus(), r.getEmailError(), r.getEmailSentAt(),
                r.getCreatedAt(), r.getUpdatedAt());
    }

    // ========== Write Methods: Announcement ==========

    public void saveAnnouncement(Announcement a) {
        jdbcTemplate.update("REPLACE INTO sys_announcement " +
                        "(id, title, content, category, target_type, target_dept_id, pinned, status, publisher_id, published_at, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                a.getId(), a.getTitle(), a.getContent(), a.getCategory(), a.getTargetType(), a.getTargetDeptId(),
                a.isPinned() ? 1 : 0, a.getStatus(), a.getPublisherId(), a.getPublishedAt(),
                a.getCreatedAt(), a.getUpdatedAt());
    }

    // ========== Write Methods: Flow ==========

    public void saveFlowInstance(FlowInstance i) {
        jdbcTemplate.update("REPLACE INTO oa_flow_instance " +
                        "(id, biz_type, biz_id, current_node_key, status, starter_id, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?)",
                i.getId(), i.getBizType(), i.getBizId(), i.getCurrentNodeKey(), i.getStatus(), i.getStarterId(),
                i.getCreatedAt(), i.getUpdatedAt());
    }

    public void saveFlowTask(FlowTask t) {
        jdbcTemplate.update("REPLACE INTO oa_flow_task " +
                        "(id, instance_id, biz_type, biz_id, node_key, approver_role, approver_id, status, due_time, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                t.getId(), t.getInstanceId(), t.getBizType(), t.getBizId(), t.getNodeKey(), t.getApproverRole(),
                t.getApproverId(), t.getStatus(), t.getDueTime(), t.getCreatedAt(), t.getUpdatedAt());
    }

    // ========== Write Methods: FlowNode ==========

    /** 保存（新增或更新）一个审批流程节点 */
    public void saveFlowNode(FlowNode node) {
        jdbcTemplate.update("REPLACE INTO oa_flow_node " +
                        "(id, flow_key, sort_order, node_key, node_label, role_key, enabled, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?)",
                node.getId(), node.getFlowKey(), node.getSortOrder(), node.getNodeKey(), node.getNodeLabel(),
                node.getRoleKey(), node.isEnabled() ? 1 : 0, node.getCreatedAt(), node.getUpdatedAt());
    }

    /** 删除指定流程Key下的所有节点（用于整体替换保存） */
    public void deleteFlowNodesByFlowKey(String flowKey) {
        jdbcTemplate.update("DELETE FROM oa_flow_node WHERE flow_key=?", flowKey);
    }

    // ========== Read Methods: User ==========

    public User findUserById(Long id) {
        List<User> users = jdbcTemplate.query(
                "SELECT u.id, u.username, u.password, u.real_name, u.email, u.dept_id, d.dept_name " +
                        "FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id=d.id WHERE u.id=?",
                (rs, rowNum) -> mapUser(rs), id);
        if (users.isEmpty()) return null;
        User user = users.get(0);
        loadUserRoles(user);
        return user;
    }

    public User findUserByUsername(String username) {
        List<User> users = jdbcTemplate.query(
                "SELECT u.id, u.username, u.password, u.real_name, u.email, u.dept_id, d.dept_name " +
                        "FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id=d.id WHERE u.username=?",
                (rs, rowNum) -> mapUser(rs), username);
        if (users.isEmpty()) return null;
        User user = users.get(0);
        loadUserRoles(user);
        return user;
    }

    public List<User> findAllUsers() {
        List<User> users = jdbcTemplate.query(
                "SELECT u.id, u.username, u.password, u.real_name, u.email, u.dept_id, d.dept_name " +
                        "FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id=d.id",
                (rs, rowNum) -> mapUser(rs));
        for (User user : users) {
            loadUserRoles(user);
        }
        return users;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRealName(rs.getString("real_name"));
        user.setEmail(rs.getString("email"));
        user.setDeptId(rs.getLong("dept_id"));
        user.setDeptName(rs.getString("dept_name"));
        user.setRoleKeys(new LinkedHashSet<String>());
        return user;
    }

    private void loadUserRoles(User user) {
        List<String> roles = jdbcTemplate.queryForList(
                "SELECT r.role_key FROM sys_user_role ur JOIN sys_role r ON ur.role_id=r.id WHERE ur.user_id=?",
                String.class, user.getId());
        user.getRoleKeys().addAll(roles);
    }

    // ========== Read Methods: Department ==========

    public Department findDepartmentById(Long id) {
        List<Department> depts = jdbcTemplate.query("SELECT id, dept_name, parent_id FROM sys_dept WHERE id=?",
                (rs, rowNum) -> mapDepartment(rs), id);
        return depts.isEmpty() ? null : depts.get(0);
    }

    public List<Department> findAllDepartments() {
        return jdbcTemplate.query("SELECT id, dept_name, parent_id FROM sys_dept",
                (rs, rowNum) -> mapDepartment(rs));
    }

    private Department mapDepartment(ResultSet rs) throws SQLException {
        Department dept = new Department();
        dept.setId(rs.getLong("id"));
        dept.setDeptName(rs.getString("dept_name"));
        dept.setParentId(rs.getLong("parent_id"));
        return dept;
    }

    // ========== Read Methods: Dictionary ==========

    public DictionaryType findDictionaryTypeByType(String dictType) {
        List<DictionaryType> types = jdbcTemplate.query("SELECT * FROM sys_dict_type WHERE dict_type=?",
                (rs, rowNum) -> mapDictionaryType(rs), dictType);
        return types.isEmpty() ? null : types.get(0);
    }

    public List<DictionaryType> findAllDictionaryTypes() {
        return jdbcTemplate.query("SELECT * FROM sys_dict_type", (rs, rowNum) -> mapDictionaryType(rs));
    }

    private DictionaryType mapDictionaryType(ResultSet rs) throws SQLException {
        DictionaryType type = new DictionaryType();
        type.setId(rs.getLong("id"));
        type.setCreatedAt(toLocalDateTime(rs, "created_at"));
        type.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        type.setDictType(rs.getString("dict_type"));
        type.setDictName(rs.getString("dict_name"));
        type.setSystemType(rs.getInt("system_type") == 1);
        type.setEnabled(rs.getInt("enabled") == 1);
        type.setRemark(rs.getString("remark"));
        return type;
    }

    public DictionaryItem findDictionaryItemByTypeAndCode(String dictType, String dictCode) {
        List<DictionaryItem> items = jdbcTemplate.query("SELECT * FROM sys_dict_item WHERE dict_type=? AND dict_code=?",
                (rs, rowNum) -> mapDictionaryItem(rs), dictType, dictCode);
        return items.isEmpty() ? null : items.get(0);
    }

    public List<DictionaryItem> findDictionaryItemsByType(String dictType) {
        return jdbcTemplate.query("SELECT * FROM sys_dict_item WHERE dict_type=? ORDER BY sort_order",
                (rs, rowNum) -> mapDictionaryItem(rs), dictType);
    }

    public List<DictionaryItem> findAllDictionaryItems() {
        return jdbcTemplate.query("SELECT * FROM sys_dict_item ORDER BY dict_type, sort_order",
                (rs, rowNum) -> mapDictionaryItem(rs));
    }

    private DictionaryItem mapDictionaryItem(ResultSet rs) throws SQLException {
        DictionaryItem item = new DictionaryItem();
        item.setId(rs.getLong("id"));
        item.setCreatedAt(toLocalDateTime(rs, "created_at"));
        item.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        item.setDictType(rs.getString("dict_type"));
        item.setDictCode(rs.getString("dict_code"));
        item.setDictLabel(rs.getString("dict_label"));
        item.setSortOrder(rs.getInt("sort_order"));
        item.setEnabled(rs.getInt("enabled") == 1);
        item.setSystemItem(rs.getInt("system_item") == 1);
        item.setRemark(rs.getString("remark"));
        return item;
    }

    // ========== Read Methods: Document ==========

    public Document findDocumentById(Long id) {
        List<Document> docs = jdbcTemplate.query("SELECT * FROM oa_document WHERE id=?",
                (rs, rowNum) -> mapDocument(rs), id);
        return docs.isEmpty() ? null : docs.get(0);
    }

    public List<Document> findAllDocuments() {
        return jdbcTemplate.query("SELECT * FROM oa_document", (rs, rowNum) -> mapDocument(rs));
    }

    private Document mapDocument(ResultSet rs) throws SQLException {
        Document d = new Document();
        d.setId(rs.getLong("id"));
        d.setCreatedAt(toLocalDateTime(rs, "created_at"));
        d.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        d.setDocNo(rs.getString("doc_no"));
        d.setTitle(rs.getString("title"));
        d.setDocType(rs.getString("doc_type"));
        d.setUrgency(rs.getString("urgency"));
        d.setSecrecyLevel(rs.getString("secrecy_level"));
        d.setKnowledgeScope(rs.getString("knowledge_scope"));
        d.setContent(rs.getString("content"));
        d.setApplicantId(rs.getLong("applicant_id"));
        d.setDeptId(rs.getLong("dept_id"));
        d.setStatus(rs.getString("status"));
        int version = rs.getInt("version");
        d.setVersion(version > 0 ? version : 1);
        d.setDistributionStatus(rs.getString("distribution_status"));
        return d;
    }

    public DocumentDistribution findDocumentDistributionById(Long id) {
        List<DocumentDistribution> list = jdbcTemplate.query("SELECT * FROM oa_document_distribution WHERE id=?",
                (rs, rowNum) -> mapDocumentDistribution(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<DocumentDistribution> findDocumentDistributionsByDocumentId(Long documentId) {
        return jdbcTemplate.query("SELECT * FROM oa_document_distribution WHERE document_id=?",
                (rs, rowNum) -> mapDocumentDistribution(rs), documentId);
    }

    public List<DocumentDistribution> findAllDocumentDistributions() {
        return jdbcTemplate.query("SELECT * FROM oa_document_distribution",
                (rs, rowNum) -> mapDocumentDistribution(rs));
    }

    private DocumentDistribution mapDocumentDistribution(ResultSet rs) throws SQLException {
        DocumentDistribution d = new DocumentDistribution();
        d.setId(rs.getLong("id"));
        d.setCreatedAt(toLocalDateTime(rs, "created_at"));
        d.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        d.setDocumentId(rs.getLong("document_id"));
        d.setReceiverId(rs.getLong("receiver_id"));
        d.setReceiverDeptId(rs.getLong("receiver_dept_id"));
        d.setStatus(rs.getString("status"));
        d.setDistributedAt(toLocalDateTime(rs, "distributed_at"));
        d.setReceivedAt(toLocalDateTime(rs, "received_at"));
        d.setRemindedAt(toLocalDateTime(rs, "reminded_at"));
        return d;
    }

    // ========== Read Methods: Seal ==========

    public Seal findSealById(Long id) {
        List<Seal> seals = jdbcTemplate.query("SELECT id, seal_name, seal_type, dept_id, keeper_id, status FROM oa_seal WHERE id=?",
                (rs, rowNum) -> mapSeal(rs), id);
        return seals.isEmpty() ? null : seals.get(0);
    }

    public List<Seal> findAllSeals() {
        return jdbcTemplate.query("SELECT id, seal_name, seal_type, dept_id, keeper_id, status FROM oa_seal",
                (rs, rowNum) -> mapSeal(rs));
    }

    private Seal mapSeal(ResultSet rs) throws SQLException {
        Seal seal = new Seal();
        seal.setId(rs.getLong("id"));
        seal.setSealName(rs.getString("seal_name"));
        seal.setSealType(rs.getString("seal_type"));
        seal.setDeptId(rs.getLong("dept_id"));
        seal.setKeeperId(rs.getLong("keeper_id"));
        seal.setStatus(rs.getString("status"));
        return seal;
    }

    public SealApplication findSealApplicationById(Long id) {
        List<SealApplication> list = jdbcTemplate.query("SELECT * FROM oa_seal_log WHERE id=?",
                (rs, rowNum) -> mapSealApplication(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<SealApplication> findAllSealApplications() {
        return jdbcTemplate.query("SELECT * FROM oa_seal_log", (rs, rowNum) -> mapSealApplication(rs));
    }

    private SealApplication mapSealApplication(ResultSet rs) throws SQLException {
        SealApplication a = new SealApplication();
        a.setId(rs.getLong("id"));
        a.setCreatedAt(toLocalDateTime(rs, "created_at"));
        a.setSealId(rs.getLong("seal_id"));
        a.setApplicantId(rs.getLong("applicant_id"));
        a.setPurpose(rs.getString("purpose"));
        a.setMaterialUrl(rs.getString("material_url"));
        a.setCopies(rs.getInt("copies"));
        a.setTakeOut(rs.getInt("take_out") == 1);
        a.setMatterLevel(rs.getString("matter_level"));
        a.setTakeOutReason(rs.getString("take_out_reason"));
        a.setTakeOutLocation(rs.getString("take_out_location"));
        a.setSupervisorId((Long) rs.getObject("supervisor_id"));
        a.setReturnDeadline(toLocalDateTime(rs, "return_deadline"));
        a.setRetentionUntil(toLocalDateTime(rs, "retention_until"));
        if (a.getRetentionUntil() == null && a.getCreatedAt() != null) {
            a.setRetentionUntil(a.getCreatedAt().plusYears(10));
        }
        a.setUseTime(toLocalDateTime(rs, "use_time"));
        a.setReturnTime(toLocalDateTime(rs, "return_time"));
        a.setStatus(rs.getString("status"));
        return a;
    }

    public List<SealTransfer> findAllSealTransfers() {
        return jdbcTemplate.query("SELECT * FROM oa_seal_transfer", (rs, rowNum) -> mapSealTransfer(rs));
    }

    private SealTransfer mapSealTransfer(ResultSet rs) throws SQLException {
        SealTransfer t = new SealTransfer();
        t.setId(rs.getLong("id"));
        t.setCreatedAt(toLocalDateTime(rs, "created_at"));
        t.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        t.setSealId(rs.getLong("seal_id"));
        t.setTransferorId(rs.getLong("transferor_id"));
        t.setReceiverId(rs.getLong("receiver_id"));
        t.setSupervisorId(rs.getLong("supervisor_id"));
        t.setMaterialUrl(rs.getString("material_url"));
        t.setRemark(rs.getString("remark"));
        t.setTransferTime(toLocalDateTime(rs, "transfer_time"));
        return t;
    }

    // ========== Read Methods: Meeting ==========

    public MeetingRoom findRoomById(Long id) {
        List<MeetingRoom> rooms = jdbcTemplate.query("SELECT id, room_name, capacity, equipment, location, status FROM oa_meeting_room WHERE id=?",
                (rs, rowNum) -> mapRoom(rs), id);
        return rooms.isEmpty() ? null : rooms.get(0);
    }

    public List<MeetingRoom> findAllRooms() {
        return jdbcTemplate.query("SELECT id, room_name, capacity, equipment, location, status FROM oa_meeting_room",
                (rs, rowNum) -> mapRoom(rs));
    }

    private MeetingRoom mapRoom(ResultSet rs) throws SQLException {
        MeetingRoom room = new MeetingRoom();
        room.setId(rs.getLong("id"));
        room.setRoomName(rs.getString("room_name"));
        room.setCapacity(rs.getInt("capacity"));
        room.setEquipment(rs.getString("equipment"));
        room.setLocation(rs.getString("location"));
        room.setEnabled(rs.getInt("status") == 1);
        return room;
    }

    public Meeting findMeetingById(Long id) {
        List<Meeting> meetings = jdbcTemplate.query("SELECT * FROM oa_meeting WHERE id=?",
                (rs, rowNum) -> mapMeeting(rs), id);
        return meetings.isEmpty() ? null : meetings.get(0);
    }

    public List<Meeting> findAllMeetings() {
        return jdbcTemplate.query("SELECT * FROM oa_meeting", (rs, rowNum) -> mapMeeting(rs));
    }

    private Meeting mapMeeting(ResultSet rs) throws SQLException {
        Meeting m = new Meeting();
        m.setId(rs.getLong("id"));
        m.setCreatedAt(toLocalDateTime(rs, "created_at"));
        m.setTitle(rs.getString("title"));
        m.setRoomId(rs.getLong("room_id"));
        m.setStartTime(toLocalDateTime(rs, "start_time"));
        m.setEndTime(toLocalDateTime(rs, "end_time"));
        m.setOrganizerId(rs.getLong("organizer_id"));
        m.setExpectedCount(rs.getInt("expected_count"));
        m.setVenueType(rs.getString("venue_type"));
        m.setMeetingType(rs.getString("meeting_type"));
        m.setBudget(rs.getBigDecimal("budget"));
        m.setAccommodationFee(rs.getBigDecimal("accommodation_fee"));
        m.setMealFee(rs.getBigDecimal("meal_fee"));
        m.setVenueFee(rs.getBigDecimal("venue_fee"));
        m.setOtherFee(rs.getBigDecimal("other_fee"));
        m.setRiskReportUrl(rs.getString("risk_report_url"));
        m.setSecurityPlanUrl(rs.getString("security_plan_url"));
        m.setEmergencyPlanUrl(rs.getString("emergency_plan_url"));
        m.setLargeActivity(rs.getInt("large_activity") == 1);
        m.setSignInCount(rs.getInt("sign_in_count"));
        m.setMinutes(rs.getString("minutes"));
        m.setStatus(rs.getString("status"));
        m.setRecorderId(rs.getObject("recorder_id") != null ? rs.getLong("recorder_id") : null);
        return m;
    }

    public MeetingParticipant findParticipantById(Long id) {
        List<MeetingParticipant> list = jdbcTemplate.query("SELECT * FROM oa_meeting_participant WHERE id=?",
                (rs, rowNum) -> mapParticipant(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<MeetingParticipant> findParticipantsByMeetingId(Long meetingId) {
        return jdbcTemplate.query("SELECT * FROM oa_meeting_participant WHERE meeting_id=?",
                (rs, rowNum) -> mapParticipant(rs), meetingId);
    }

    public MeetingParticipant findParticipantByMeetingIdAndUserId(Long meetingId, Long userId) {
        List<MeetingParticipant> list = jdbcTemplate.query(
                "SELECT * FROM oa_meeting_participant WHERE meeting_id=? AND user_id=?",
                (rs, rowNum) -> mapParticipant(rs), meetingId, userId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<MeetingParticipant> findParticipantsByUserId(Long userId) {
        return jdbcTemplate.query("SELECT * FROM oa_meeting_participant WHERE user_id=?",
                (rs, rowNum) -> mapParticipant(rs), userId);
    }

    public List<MeetingParticipant> findAllParticipants() {
        return jdbcTemplate.query("SELECT * FROM oa_meeting_participant",
                (rs, rowNum) -> mapParticipant(rs));
    }

    private MeetingParticipant mapParticipant(ResultSet rs) throws SQLException {
        MeetingParticipant p = new MeetingParticipant();
        p.setId(rs.getLong("id"));
        p.setCreatedAt(toLocalDateTime(rs, "created_at"));
        p.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        p.setMeetingId(rs.getLong("meeting_id"));
        p.setUserId(rs.getLong("user_id"));
        p.setRecorder(rs.getInt("is_recorder") == 1);
        p.setMinutesConfirmed(rs.getInt("minutes_confirmed") == 1);
        java.sql.Timestamp confirmedAt = rs.getTimestamp("confirmed_at");
        p.setConfirmedAt(confirmedAt != null ? confirmedAt.toLocalDateTime() : null);
        return p;
    }

    public Map<String, BigDecimal> findMeetingFeeStandards() {
        return jdbcTemplate.query("SELECT meeting_type, total_limit FROM oa_meeting_fee_standard",
                rs -> {
                    Map<String, BigDecimal> map = new HashMap<>();
                    while (rs.next()) {
                        map.put(rs.getString("meeting_type"), rs.getBigDecimal("total_limit"));
                    }
                    return map;
                });
    }

    // ========== Read Methods: Travel ==========

    public Travel findTravelById(Long id) {
        List<Travel> list = jdbcTemplate.query("SELECT * FROM oa_travel WHERE id=?",
                (rs, rowNum) -> mapTravel(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Travel> findAllTravels() {
        return jdbcTemplate.query("SELECT * FROM oa_travel", (rs, rowNum) -> mapTravel(rs));
    }

    private Travel mapTravel(ResultSet rs) throws SQLException {
        Travel t = new Travel();
        t.setId(rs.getLong("id"));
        t.setCreatedAt(toLocalDateTime(rs, "created_at"));
        t.setApplicantId(rs.getLong("applicant_id"));
        t.setDestination(rs.getString("destination"));
        t.setStartDate(rs.getDate("start_date").toLocalDate());
        t.setEndDate(rs.getDate("end_date").toLocalDate());
        t.setReason(rs.getString("reason"));
        t.setStaffLevel(rs.getString("staff_level"));
        t.setTravelType(rs.getString("travel_type"));
        t.setTransport(rs.getString("transport"));
        t.setBudget(rs.getBigDecimal("budget"));
        t.setActualExpense(rs.getBigDecimal("actual_expense"));
        t.setReceiptUrl(rs.getString("receipt_url"));
        t.setOverLimitReason(rs.getString("over_limit_reason"));
        t.setReimbursementSubmitted(rs.getInt("reimbursement_submitted") == 1);
        t.setStatus(rs.getString("status"));
        return t;
    }

    // ========== Read Methods: Report ==========

    public Report findReportById(Long id) {
        List<Report> list = jdbcTemplate.query("SELECT * FROM oa_report WHERE id=?",
                (rs, rowNum) -> mapReport(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Report> findAllReports() {
        return jdbcTemplate.query("SELECT * FROM oa_report", (rs, rowNum) -> mapReport(rs));
    }

    private Report mapReport(ResultSet rs) throws SQLException {
        Report r = new Report();
        r.setId(rs.getLong("id"));
        r.setCreatedAt(toLocalDateTime(rs, "created_at"));
        r.setTitle(rs.getString("title"));
        r.setType(rs.getString("type"));
        r.setSecrecyLevel(rs.getString("secrecy_level"));
        r.setApplicantId(rs.getLong("applicant_id"));
        r.setDeptId(rs.getLong("dept_id"));
        r.setContent(rs.getString("content"));
        r.setReply(rs.getString("reply"));
        r.setStatus(rs.getString("status"));
        return r;
    }

    // ========== Read Methods: Approval ==========

    public List<ApprovalRecord> findApprovalsByBizTypeAndBizId(String bizType, Long bizId) {
        return jdbcTemplate.query("SELECT * FROM oa_approval_history WHERE biz_type=? AND biz_id=?",
                (rs, rowNum) -> mapApproval(rs), bizType, bizId);
    }

    public List<ApprovalRecord> findAllApprovals() {
        return jdbcTemplate.query("SELECT * FROM oa_approval_history", (rs, rowNum) -> mapApproval(rs));
    }

    private ApprovalRecord mapApproval(ResultSet rs) throws SQLException {
        ApprovalRecord r = new ApprovalRecord();
        r.setId(rs.getLong("id"));
        r.setCreatedAt(toLocalDateTime(rs, "operated_at"));
        r.setBizType(rs.getString("biz_type"));
        r.setBizId(rs.getLong("biz_id"));
        r.setOperatorId(rs.getLong("operator_id"));
        r.setAction(rs.getString("action"));
        r.setOpinion(rs.getString("opinion"));
        return r;
    }

    // ========== Read Methods: Attachment ==========

    public Attachment findAttachmentById(Long id) {
        List<Attachment> list = jdbcTemplate.query("SELECT * FROM sys_attachment WHERE id=?",
                (rs, rowNum) -> mapAttachment(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Attachment> findAttachmentsByBizTypeAndBizId(String bizType, Long bizId) {
        return jdbcTemplate.query("SELECT * FROM sys_attachment WHERE biz_type=? AND biz_id=?",
                (rs, rowNum) -> mapAttachment(rs), bizType, bizId);
    }

    public List<Attachment> findAllAttachments() {
        return jdbcTemplate.query("SELECT * FROM sys_attachment", (rs, rowNum) -> mapAttachment(rs));
    }

    private Attachment mapAttachment(ResultSet rs) throws SQLException {
        Attachment a = new Attachment();
        a.setId(rs.getLong("id"));
        a.setCreatedAt(toLocalDateTime(rs, "created_at"));
        a.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        a.setBizType(rs.getString("biz_type"));
        a.setBizId(rs.getLong("biz_id"));
        a.setFileName(rs.getString("file_name"));
        a.setOriginalName(rs.getString("original_name"));
        a.setFileUrl(rs.getString("file_url"));
        a.setStoragePath(rs.getString("storage_path"));
        Number fileSize = (Number) rs.getObject("file_size");
        a.setFileSize(fileSize == null ? null : fileSize.longValue());
        a.setContentType(rs.getString("content_type"));
        a.setSecrecyLevel(rs.getString("secrecy_level"));
        a.setUploaderId(rs.getLong("uploader_id"));
        a.setDeleted(rs.getInt("deleted") == 1);
        Number deletedBy = (Number) rs.getObject("deleted_by");
        a.setDeletedBy(deletedBy == null ? null : deletedBy.longValue());
        a.setDeletedAt(toLocalDateTime(rs, "deleted_at"));
        a.setDeleteReason(rs.getString("delete_reason"));
        return a;
    }

    // ========== Read Methods: AuditLog ==========

    public List<AuditLog> findAuditLogsByBizTypeAndBizId(String bizType, Long bizId) {
        return jdbcTemplate.query("SELECT * FROM sys_operation_log WHERE biz_type=? AND biz_id=?",
                (rs, rowNum) -> mapAuditLog(rs), bizType, bizId);
    }

    public List<AuditLog> findAllAuditLogs() {
        return jdbcTemplate.query("SELECT * FROM sys_operation_log", (rs, rowNum) -> mapAuditLog(rs));
    }

    private AuditLog mapAuditLog(ResultSet rs) throws SQLException {
        AuditLog l = new AuditLog();
        l.setId(rs.getLong("id"));
        l.setCreatedAt(toLocalDateTime(rs, "created_at"));
        l.setOperatorId(rs.getLong("operator_id"));
        l.setModule(rs.getString("module"));
        l.setAction(rs.getString("action"));
        l.setBizType(rs.getString("biz_type"));
        l.setBizId(rs.getLong("biz_id"));
        l.setDetail(rs.getString("detail"));
        return l;
    }

    // ========== Read Methods: Notification ==========

    public List<Notification> findNotificationsByReceiverId(Long receiverId) {
        return jdbcTemplate.query("SELECT * FROM sys_notification WHERE receiver_id=?",
                (rs, rowNum) -> mapNotification(rs), receiverId);
    }

    public List<Notification> findAllNotifications() {
        return jdbcTemplate.query("SELECT * FROM sys_notification", (rs, rowNum) -> mapNotification(rs));
    }

    private Notification mapNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setCreatedAt(toLocalDateTime(rs, "created_at"));
        n.setReceiverId(rs.getLong("receiver_id"));
        n.setTitle(rs.getString("title"));
        n.setContent(rs.getString("content"));
        n.setReadStatus(rs.getInt("read_status") == 1);
        n.setBizType(rs.getString("biz_type"));
        n.setBizId(rs.getLong("biz_id"));
        return n;
    }

    // ========== Read Methods: Mail ==========

    public MailMessage findMailMessageById(Long id) {
        List<MailMessage> list = jdbcTemplate.query("SELECT * FROM oa_mail_message WHERE id=?",
                (rs, rowNum) -> mapMailMessage(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<MailMessage> findAllMailMessages() {
        return jdbcTemplate.query("SELECT * FROM oa_mail_message", (rs, rowNum) -> mapMailMessage(rs));
    }

    private MailMessage mapMailMessage(ResultSet rs) throws SQLException {
        MailMessage m = new MailMessage();
        m.setId(rs.getLong("id"));
        m.setSenderId(rs.getLong("sender_id"));
        m.setSubject(rs.getString("subject"));
        m.setContent(rs.getString("content"));
        m.setCreatedAt(toLocalDateTime(rs, "created_at"));
        m.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        return m;
    }

    public MailRecipient findMailRecipientByMailIdAndUserId(Long mailId, Long userId) {
        List<MailRecipient> list = jdbcTemplate.query(
                "SELECT * FROM oa_mail_recipient WHERE mail_id=? AND user_id=?",
                (rs, rowNum) -> mapMailRecipient(rs), mailId, userId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<MailRecipient> findMailRecipientsByMailId(Long mailId) {
        return jdbcTemplate.query("SELECT * FROM oa_mail_recipient WHERE mail_id=?",
                (rs, rowNum) -> mapMailRecipient(rs), mailId);
    }

    public List<MailRecipient> findMailRecipientsByUserId(Long userId) {
        return jdbcTemplate.query("SELECT * FROM oa_mail_recipient WHERE user_id=?",
                (rs, rowNum) -> mapMailRecipient(rs), userId);
    }

    public List<MailRecipient> findAllMailRecipients() {
        return jdbcTemplate.query("SELECT * FROM oa_mail_recipient",
                (rs, rowNum) -> mapMailRecipient(rs));
    }

    private MailRecipient mapMailRecipient(ResultSet rs) throws SQLException {
        MailRecipient r = new MailRecipient();
        r.setId(rs.getLong("id"));
        r.setMailId(rs.getLong("mail_id"));
        r.setUserId(rs.getLong("user_id"));
        r.setRecipientType(rs.getString("recipient_type"));
        r.setReadStatus(rs.getInt("read_status") == 1);
        r.setReadAt(toLocalDateTime(rs, "read_at"));
        r.setEmailStatus(rs.getString("email_status"));
        r.setEmailError(rs.getString("email_error"));
        r.setEmailSentAt(toLocalDateTime(rs, "email_sent_at"));
        r.setCreatedAt(toLocalDateTime(rs, "created_at"));
        r.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        return r;
    }

    // ========== Read Methods: Announcement ==========

    public Announcement findAnnouncementById(Long id) {
        List<Announcement> list = jdbcTemplate.query("SELECT * FROM sys_announcement WHERE id=?",
                (rs, rowNum) -> mapAnnouncement(rs), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Announcement> findAllAnnouncements() {
        return jdbcTemplate.query("SELECT * FROM sys_announcement", (rs, rowNum) -> mapAnnouncement(rs));
    }

    private Announcement mapAnnouncement(ResultSet rs) throws SQLException {
        Announcement a = new Announcement();
        a.setId(rs.getLong("id"));
        a.setCreatedAt(toLocalDateTime(rs, "created_at"));
        a.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setCategory(rs.getString("category"));
        a.setTargetType(rs.getString("target_type"));
        Number targetDeptId = (Number) rs.getObject("target_dept_id");
        a.setTargetDeptId(targetDeptId == null ? null : targetDeptId.longValue());
        a.setPinned(rs.getInt("pinned") == 1);
        a.setStatus(rs.getString("status"));
        Number publisherId = (Number) rs.getObject("publisher_id");
        a.setPublisherId(publisherId == null ? null : publisherId.longValue());
        a.setPublishedAt(toLocalDateTime(rs, "published_at"));
        return a;
    }

    // ========== Read Methods: Flow ==========

    public FlowInstance findFlowInstanceByBizTypeAndBizId(String bizType, Long bizId) {
        List<FlowInstance> list = jdbcTemplate.query(
                "SELECT * FROM oa_flow_instance WHERE biz_type=? AND biz_id=?",
                (rs, rowNum) -> mapFlowInstance(rs), bizType, bizId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<FlowInstance> findAllFlowInstances() {
        return jdbcTemplate.query("SELECT * FROM oa_flow_instance",
                (rs, rowNum) -> mapFlowInstance(rs));
    }

    private FlowInstance mapFlowInstance(ResultSet rs) throws SQLException {
        FlowInstance i = new FlowInstance();
        i.setId(rs.getLong("id"));
        i.setCreatedAt(toLocalDateTime(rs, "created_at"));
        i.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        i.setBizType(rs.getString("biz_type"));
        i.setBizId(rs.getLong("biz_id"));
        i.setCurrentNodeKey(rs.getString("current_node_key"));
        i.setStatus(rs.getString("status"));
        i.setStarterId(rs.getLong("starter_id"));
        return i;
    }

    public List<FlowTask> findFlowTasksByInstanceId(Long instanceId) {
        return jdbcTemplate.query("SELECT * FROM oa_flow_task WHERE instance_id=?",
                (rs, rowNum) -> mapFlowTask(rs), instanceId);
    }

    public List<FlowTask> findFlowTasksByBizTypeAndBizId(String bizType, Long bizId) {
        return jdbcTemplate.query("SELECT * FROM oa_flow_task WHERE biz_type=? AND biz_id=?",
                (rs, rowNum) -> mapFlowTask(rs), bizType, bizId);
    }

    public List<FlowTask> findPendingFlowTasksByApproverId(Long approverId) {
        return jdbcTemplate.query("SELECT * FROM oa_flow_task WHERE approver_id=? AND status='pending'",
                (rs, rowNum) -> mapFlowTask(rs), approverId);
    }

    public List<FlowTask> findPendingFlowTasksByApproverRole(String approverRole) {
        return jdbcTemplate.query("SELECT * FROM oa_flow_task WHERE approver_role=? AND status='pending' AND approver_id=0",
                (rs, rowNum) -> mapFlowTask(rs), approverRole);
    }

    public List<FlowTask> findAllFlowTasks() {
        return jdbcTemplate.query("SELECT * FROM oa_flow_task",
                (rs, rowNum) -> mapFlowTask(rs));
    }

    private FlowTask mapFlowTask(ResultSet rs) throws SQLException {
        FlowTask t = new FlowTask();
        t.setId(rs.getLong("id"));
        t.setCreatedAt(toLocalDateTime(rs, "created_at"));
        t.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        t.setInstanceId(rs.getLong("instance_id"));
        t.setBizType(rs.getString("biz_type"));
        t.setBizId(rs.getLong("biz_id"));
        t.setNodeKey(rs.getString("node_key"));
        t.setApproverRole(rs.getString("approver_role"));
        t.setApproverId(rs.getLong("approver_id"));
        t.setStatus(rs.getString("status"));
        t.setDueTime(toLocalDateTime(rs, "due_time"));
        return t;
    }

    // ========== Read Methods: FlowNode ==========

    /** 查询所有审批流程节点，按流程Key和顺序排序 */
    public List<FlowNode> findAllFlowNodes() {
        return jdbcTemplate.query("SELECT * FROM oa_flow_node ORDER BY flow_key, sort_order",
                (rs, rowNum) -> mapFlowNode(rs));
    }

    /** 查询指定流程Key的所有节点（按顺序排序） */
    public List<FlowNode> findFlowNodesByFlowKey(String flowKey) {
        return jdbcTemplate.query("SELECT * FROM oa_flow_node WHERE flow_key=? ORDER BY sort_order",
                (rs, rowNum) -> mapFlowNode(rs), flowKey);
    }

    private FlowNode mapFlowNode(ResultSet rs) throws SQLException {
        FlowNode node = new FlowNode();
        node.setId(rs.getLong("id"));
        node.setCreatedAt(toLocalDateTime(rs, "created_at"));
        node.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
        node.setFlowKey(rs.getString("flow_key"));
        node.setSortOrder(rs.getInt("sort_order"));
        node.setNodeKey(rs.getString("node_key"));
        node.setNodeLabel(rs.getString("node_label"));
        node.setRoleKey(rs.getString("role_key"));
        node.setEnabled(rs.getInt("enabled") == 1);
        return node;
    }
}
