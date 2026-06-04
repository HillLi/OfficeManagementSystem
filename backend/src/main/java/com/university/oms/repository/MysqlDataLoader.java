package com.university.oms.repository;

import com.university.oms.model.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;

@Component
@ConditionalOnProperty(name = "oms.repository", havingValue = "mysql")
public class MysqlDataLoader {
    private final InMemoryDatabase db;
    private final JdbcTemplate jdbcTemplate;

    public MysqlDataLoader(InMemoryDatabase db, JdbcTemplate jdbcTemplate) {
        this.db = db;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void load() {
        db.dictionaryTypes().clear();
        db.dictionaryItems().clear();
        db.departments().clear();
        db.users().clear();
        db.seals().clear();
        db.rooms().clear();
        db.documents().clear();
        db.documentDistributions().clear();
        db.sealApplications().clear();
        db.sealTransfers().clear();
        db.meetings().clear();
        db.travels().clear();
        db.reports().clear();
        db.approvals().clear();
        db.attachments().clear();
        db.auditLogs().clear();
        db.notifications().clear();
        db.announcements().clear();
        db.flowInstances().clear();
        db.flowTasks().clear();

        loadDictionaryTypes();
        loadDictionaryItems();
        loadDepartments();
        loadUsers();
        loadSeals();
        loadRooms();
        loadDocuments();
        loadDocumentDistributions();
        loadSealApplications();
        loadSealTransfers();
        loadMeetings();
        loadTravels();
        loadReports();
        loadApprovals();
        loadAttachments();
        loadAuditLogs();
        loadNotifications();
        loadAnnouncements();
        loadFlowInstances();
        loadFlowTasks();
        db.ensureNextIdAtLeast(maxId());
    }

    private void loadDictionaryTypes() {
        jdbcTemplate.query("SELECT * FROM sys_dict_type", rs -> {
            DictionaryType type = new DictionaryType();
            type.setId(rs.getLong("id"));
            type.setCreatedAt(toLocalDateTime(rs, "created_at"));
            type.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
            type.setDictType(rs.getString("dict_type"));
            type.setDictName(rs.getString("dict_name"));
            type.setSystemType(rs.getInt("system_type") == 1);
            type.setEnabled(rs.getInt("enabled") == 1);
            type.setRemark(rs.getString("remark"));
            db.dictionaryTypes().put(type.getDictType(), type);
        });
    }

    private void loadDictionaryItems() {
        jdbcTemplate.query("SELECT * FROM sys_dict_item", rs -> {
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
            db.dictionaryItems().put(db.dictionaryItemKey(item.getDictType(), item.getDictCode()), item);
        });
    }

    private void loadDepartments() {
        jdbcTemplate.query("SELECT id, dept_name, parent_id FROM sys_dept", rs -> {
            Department dept = new Department();
            db.fill(dept, rs.getLong("id"));
            dept.setDeptName(rs.getString("dept_name"));
            dept.setParentId(rs.getLong("parent_id"));
            db.departments().put(dept.getId(), dept);
        });
    }

    private void loadUsers() {
        List<User> users = jdbcTemplate.query("SELECT u.id, u.username, u.password, u.real_name, u.email, u.dept_id, d.dept_name " +
                "FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id=d.id", (rs, rowNum) -> {
            User user = new User();
            db.fill(user, rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setRealName(rs.getString("real_name"));
            user.setEmail(rs.getString("email"));
            user.setDeptId(rs.getLong("dept_id"));
            user.setDeptName(rs.getString("dept_name"));
            user.setRoleKeys(new LinkedHashSet<String>());
            return user;
        });
        for (User user : users) {
            List<String> roles = jdbcTemplate.queryForList(
                    "SELECT r.role_key FROM sys_user_role ur JOIN sys_role r ON ur.role_id=r.id WHERE ur.user_id=?",
                    String.class, user.getId());
            user.getRoleKeys().addAll(roles);
            db.users().put(user.getId(), user);
        }
    }

    private void loadSeals() {
        jdbcTemplate.query("SELECT id, seal_name, seal_type, dept_id, keeper_id, status FROM oa_seal", rs -> {
            Seal seal = new Seal();
            db.fill(seal, rs.getLong("id"));
            seal.setSealName(rs.getString("seal_name"));
            seal.setSealType(rs.getString("seal_type"));
            seal.setDeptId(rs.getLong("dept_id"));
            seal.setKeeperId(rs.getLong("keeper_id"));
            seal.setStatus(rs.getString("status"));
            db.seals().put(seal.getId(), seal);
        });
    }

    private void loadRooms() {
        jdbcTemplate.query("SELECT id, room_name, capacity, equipment, location, status FROM oa_meeting_room", rs -> {
            MeetingRoom room = new MeetingRoom();
            db.fill(room, rs.getLong("id"));
            room.setRoomName(rs.getString("room_name"));
            room.setCapacity(rs.getInt("capacity"));
            room.setEquipment(rs.getString("equipment"));
            room.setLocation(rs.getString("location"));
            room.setEnabled(rs.getInt("status") == 1);
            db.rooms().put(room.getId(), room);
        });
    }

    private void loadDocuments() {
        jdbcTemplate.query("SELECT * FROM oa_document", rs -> {
            Document d = new Document();
            db.fill(d, rs.getLong("id"));
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
            db.documents().put(d.getId(), d);
        });
    }

    private void loadDocumentDistributions() {
        jdbcTemplate.query("SELECT * FROM oa_document_distribution", rs -> {
            DocumentDistribution d = new DocumentDistribution();
            db.fill(d, rs.getLong("id"));
            d.setDocumentId(rs.getLong("document_id"));
            d.setReceiverId(rs.getLong("receiver_id"));
            d.setReceiverDeptId(rs.getLong("receiver_dept_id"));
            d.setStatus(rs.getString("status"));
            d.setDistributedAt(toLocalDateTime(rs, "distributed_at"));
            d.setReceivedAt(toLocalDateTime(rs, "received_at"));
            d.setRemindedAt(toLocalDateTime(rs, "reminded_at"));
            db.documentDistributions().put(d.getId(), d);
        });
    }

    private void loadSealApplications() {
        jdbcTemplate.query("SELECT * FROM oa_seal_log", rs -> {
            SealApplication a = new SealApplication();
            db.fill(a, rs.getLong("id"));
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
            if (a.getRetentionUntil() == null) {
                a.setRetentionUntil(a.getCreatedAt().plusYears(10));
            }
            a.setUseTime(toLocalDateTime(rs, "use_time"));
            a.setReturnTime(toLocalDateTime(rs, "return_time"));
            a.setStatus(rs.getString("status"));
            db.sealApplications().put(a.getId(), a);
        });
    }

    private void loadSealTransfers() {
        jdbcTemplate.query("SELECT * FROM oa_seal_transfer", rs -> {
            SealTransfer t = new SealTransfer();
            db.fill(t, rs.getLong("id"));
            t.setSealId(rs.getLong("seal_id"));
            t.setTransferorId(rs.getLong("transferor_id"));
            t.setReceiverId(rs.getLong("receiver_id"));
            t.setSupervisorId(rs.getLong("supervisor_id"));
            t.setMaterialUrl(rs.getString("material_url"));
            t.setRemark(rs.getString("remark"));
            t.setTransferTime(toLocalDateTime(rs, "transfer_time"));
            db.sealTransfers().put(t.getId(), t);
        });
    }

    private void loadMeetings() {
        jdbcTemplate.query("SELECT * FROM oa_meeting", rs -> {
            Meeting m = new Meeting();
            db.fill(m, rs.getLong("id"));
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
            db.meetings().put(m.getId(), m);
        });
    }

    private void loadTravels() {
        jdbcTemplate.query("SELECT * FROM oa_travel", rs -> {
            Travel t = new Travel();
            db.fill(t, rs.getLong("id"));
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
            db.travels().put(t.getId(), t);
        });
    }

    private void loadReports() {
        jdbcTemplate.query("SELECT * FROM oa_report", rs -> {
            Report r = new Report();
            db.fill(r, rs.getLong("id"));
            r.setTitle(rs.getString("title"));
            r.setType(rs.getString("type"));
            r.setSecrecyLevel(rs.getString("secrecy_level"));
            r.setApplicantId(rs.getLong("applicant_id"));
            r.setDeptId(rs.getLong("dept_id"));
            r.setContent(rs.getString("content"));
            r.setReply(rs.getString("reply"));
            r.setStatus(rs.getString("status"));
            db.reports().put(r.getId(), r);
        });
    }

    private void loadApprovals() {
        jdbcTemplate.query("SELECT * FROM oa_approval_history", rs -> {
            ApprovalRecord r = new ApprovalRecord();
            db.fill(r, rs.getLong("id"));
            r.setBizType(rs.getString("biz_type"));
            r.setBizId(rs.getLong("biz_id"));
            r.setOperatorId(rs.getLong("operator_id"));
            r.setAction(rs.getString("action"));
            r.setOpinion(rs.getString("opinion"));
            db.approvals().add(r);
        });
    }

    private void loadAttachments() {
        jdbcTemplate.query("SELECT * FROM sys_attachment", rs -> {
            Attachment a = new Attachment();
            db.fill(a, rs.getLong("id"));
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
            db.attachments().add(a);
        });
    }

    private void loadAuditLogs() {
        jdbcTemplate.query("SELECT * FROM sys_operation_log", rs -> {
            AuditLog l = new AuditLog();
            db.fill(l, rs.getLong("id"));
            l.setOperatorId(rs.getLong("operator_id"));
            l.setModule(rs.getString("module"));
            l.setAction(rs.getString("action"));
            l.setBizType(rs.getString("biz_type"));
            l.setBizId(rs.getLong("biz_id"));
            l.setDetail(rs.getString("detail"));
            db.auditLogs().add(l);
        });
    }

    private void loadNotifications() {
        jdbcTemplate.query("SELECT * FROM sys_notification", rs -> {
            Notification n = new Notification();
            db.fill(n, rs.getLong("id"));
            n.setReceiverId(rs.getLong("receiver_id"));
            n.setTitle(rs.getString("title"));
            n.setContent(rs.getString("content"));
            n.setReadStatus(rs.getInt("read_status") == 1);
            n.setBizType(rs.getString("biz_type"));
            n.setBizId(rs.getLong("biz_id"));
            db.notifications().add(n);
        });
    }

    private void loadAnnouncements() {
        jdbcTemplate.query("SELECT * FROM sys_announcement", rs -> {
            Announcement a = new Announcement();
            db.fill(a, rs.getLong("id"));
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
            a.setUpdatedAt(toLocalDateTime(rs, "updated_at"));
            db.announcements().put(a.getId(), a);
        });
    }

    private void loadFlowInstances() {
        jdbcTemplate.query("SELECT * FROM oa_flow_instance", rs -> {
            FlowInstance i = new FlowInstance();
            db.fill(i, rs.getLong("id"));
            i.setBizType(rs.getString("biz_type"));
            i.setBizId(rs.getLong("biz_id"));
            i.setCurrentNodeKey(rs.getString("current_node_key"));
            i.setStatus(rs.getString("status"));
            i.setStarterId(rs.getLong("starter_id"));
            db.flowInstances().put(i.getBizType() + ":" + i.getBizId(), i);
        });
    }

    private void loadFlowTasks() {
        jdbcTemplate.query("SELECT * FROM oa_flow_task", rs -> {
            FlowTask t = new FlowTask();
            db.fill(t, rs.getLong("id"));
            t.setInstanceId(rs.getLong("instance_id"));
            t.setBizType(rs.getString("biz_type"));
            t.setBizId(rs.getLong("biz_id"));
            t.setNodeKey(rs.getString("node_key"));
            t.setApproverRole(rs.getString("approver_role"));
            t.setApproverId(rs.getLong("approver_id"));
            t.setStatus(rs.getString("status"));
            t.setDueTime(toLocalDateTime(rs, "due_time"));
            db.flowTasks().add(t);
        });
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
                        "COALESCE((SELECT MAX(id) FROM sys_announcement),0)," +
                        "COALESCE((SELECT MAX(id) FROM sys_dict_type),0)," +
                        "COALESCE((SELECT MAX(id) FROM sys_dict_item),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_flow_instance),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_flow_task),0)," +
                        "1000)",
                Long.class);
        return value == null ? 1000L : value;
    }

    private java.time.LocalDateTime toLocalDateTime(ResultSet rs, String column) throws SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
