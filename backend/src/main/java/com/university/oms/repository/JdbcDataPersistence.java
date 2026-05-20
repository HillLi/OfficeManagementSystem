package com.university.oms.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.oms.model.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "oms.repository", havingValue = "mysql")
public class JdbcDataPersistence implements DataPersistence {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcDataPersistence(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveDocument(Document d) {
        jdbcTemplate.update("REPLACE INTO oa_document " +
                        "(id, doc_no, title, doc_type, urgency, secrecy_level, knowledge_scope, content, applicant_id, dept_id, status, ai_review_result, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                d.getId(), d.getDocNo(), d.getTitle(), d.getDocType(), d.getUrgency(), d.getSecrecyLevel(),
                d.getKnowledgeScope(), d.getContent(), d.getApplicantId(), d.getDeptId(), d.getStatus(),
                toJson(d.getAiReviewResult()), d.getCreatedAt(), d.getUpdatedAt());
    }

    @Override
    public void saveSealApplication(SealApplication a) {
        jdbcTemplate.update("REPLACE INTO oa_seal_log " +
                        "(id, seal_id, applicant_id, purpose, material_url, copies, take_out, matter_level, use_time, return_time, status, created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                a.getId(), a.getSealId(), a.getApplicantId(), a.getPurpose(), a.getMaterialUrl(), a.getCopies(),
                a.isTakeOut() ? 1 : 0, a.getMatterLevel(), a.getUseTime(), a.getReturnTime(), a.getStatus(), a.getCreatedAt());
    }

    @Override
    public void saveSeal(Seal s) {
        jdbcTemplate.update("REPLACE INTO oa_seal (id, seal_name, seal_type, dept_id, keeper_id, status) VALUES (?,?,?,?,?,?)",
                s.getId(), s.getSealName(), s.getSealType(), s.getDeptId(), s.getKeeperId(), s.getStatus());
    }

    @Override
    public void saveMeeting(Meeting m) {
        jdbcTemplate.update("REPLACE INTO oa_meeting " +
                        "(id, title, room_id, start_time, end_time, organizer_id, expected_count, venue_type, meeting_type, budget, risk_report_url, security_plan_url, emergency_plan_url, large_activity, status, created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                m.getId(), m.getTitle(), m.getRoomId(), m.getStartTime(), m.getEndTime(), m.getOrganizerId(),
                m.getExpectedCount(), m.getVenueType(), m.getMeetingType(), m.getBudget(), m.getRiskReportUrl(),
                m.getSecurityPlanUrl(), m.getEmergencyPlanUrl(), m.isLargeActivity() ? 1 : 0, m.getStatus(), m.getCreatedAt());
    }

    @Override
    public void saveTravel(Travel t) {
        jdbcTemplate.update("REPLACE INTO oa_travel " +
                        "(id, applicant_id, destination, start_date, end_date, reason, staff_level, travel_type, transport, budget, actual_expense, status, created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                t.getId(), t.getApplicantId(), t.getDestination(), t.getStartDate(), t.getEndDate(), t.getReason(),
                t.getStaffLevel(), t.getTravelType(), t.getTransport(), t.getBudget(), t.getActualExpense(), t.getStatus(), t.getCreatedAt());
    }

    @Override
    public void saveReport(Report r) {
        jdbcTemplate.update("REPLACE INTO oa_report " +
                        "(id, title, type, secrecy_level, applicant_id, dept_id, content, reply, status, created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)",
                r.getId(), r.getTitle(), r.getType(), r.getSecrecyLevel(), r.getApplicantId(), r.getDeptId(),
                r.getContent(), r.getReply(), r.getStatus(), r.getCreatedAt());
    }

    @Override
    public void saveApproval(ApprovalRecord r) {
        jdbcTemplate.update("REPLACE INTO oa_approval_history (id, biz_type, biz_id, operator_id, action, opinion, operated_at) VALUES (?,?,?,?,?,?,?)",
                r.getId(), r.getBizType(), r.getBizId(), r.getOperatorId(), r.getAction(), r.getOpinion(), r.getCreatedAt());
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON序列化失败", e);
        }
    }
}
