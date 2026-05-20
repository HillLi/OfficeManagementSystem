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
        db.departments().clear();
        db.users().clear();
        db.seals().clear();
        db.rooms().clear();
        db.documents().clear();
        db.sealApplications().clear();
        db.meetings().clear();
        db.travels().clear();
        db.reports().clear();
        db.approvals().clear();

        loadDepartments();
        loadUsers();
        loadSeals();
        loadRooms();
        loadDocuments();
        loadSealApplications();
        loadMeetings();
        loadTravels();
        loadReports();
        loadApprovals();
        db.ensureNextIdAtLeast(maxId());
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
        List<User> users = jdbcTemplate.query("SELECT u.id, u.username, u.password, u.real_name, u.dept_id, d.dept_name " +
                "FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id=d.id", (rs, rowNum) -> {
            User user = new User();
            db.fill(user, rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setRealName(rs.getString("real_name"));
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
            db.documents().put(d.getId(), d);
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
            a.setUseTime(toLocalDateTime(rs, "use_time"));
            a.setReturnTime(toLocalDateTime(rs, "return_time"));
            a.setStatus(rs.getString("status"));
            db.sealApplications().put(a.getId(), a);
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
            m.setRiskReportUrl(rs.getString("risk_report_url"));
            m.setSecurityPlanUrl(rs.getString("security_plan_url"));
            m.setEmergencyPlanUrl(rs.getString("emergency_plan_url"));
            m.setLargeActivity(rs.getInt("large_activity") == 1);
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

    private long maxId() {
        Long value = jdbcTemplate.queryForObject(
                "SELECT GREATEST(" +
                        "COALESCE((SELECT MAX(id) FROM oa_document),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_seal_log),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_meeting),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_travel),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_report),0)," +
                        "COALESCE((SELECT MAX(id) FROM oa_approval_history),0)," +
                        "1000)",
                Long.class);
        return value == null ? 1000L : value;
    }

    private java.time.LocalDateTime toLocalDateTime(ResultSet rs, String column) throws SQLException {
        java.sql.Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
