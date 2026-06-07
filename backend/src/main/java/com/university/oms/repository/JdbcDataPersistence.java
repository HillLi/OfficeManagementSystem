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

    @Override
    public void deleteUser(Long id) {
        jdbcTemplate.update("DELETE FROM sys_user_role WHERE user_id=?", id);
        jdbcTemplate.update("DELETE FROM sys_user WHERE id=?", id);
    }

    @Override
    public void saveDepartment(Department d) {
        jdbcTemplate.update("REPLACE INTO sys_dept (id, dept_name, parent_id, status) VALUES (?,?,?,1)",
                d.getId(), d.getDeptName(), d.getParentId());
    }

    @Override
    public void deleteDepartment(Long id) {
        jdbcTemplate.update("DELETE FROM sys_dept WHERE id=?", id);
    }

    @Override
    public void saveDictionaryType(DictionaryType type) {
        jdbcTemplate.update("REPLACE INTO sys_dict_type " +
                        "(id, dict_type, dict_name, system_type, enabled, remark, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?)",
                type.getId(), type.getDictType(), type.getDictName(), type.isSystemType() ? 1 : 0,
                type.isEnabled() ? 1 : 0, type.getRemark(), type.getCreatedAt(), type.getUpdatedAt());
    }

    @Override
    public void saveDictionaryItem(DictionaryItem item) {
        jdbcTemplate.update("REPLACE INTO sys_dict_item " +
                        "(id, dict_type, dict_code, dict_label, sort_order, enabled, system_item, remark, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)",
                item.getId(), item.getDictType(), item.getDictCode(), item.getDictLabel(), item.getSortOrder(),
                item.isEnabled() ? 1 : 0, item.isSystemItem() ? 1 : 0, item.getRemark(),
                item.getCreatedAt(), item.getUpdatedAt());
    }

    @Override
    public void saveDocument(Document d) {
        jdbcTemplate.update("REPLACE INTO oa_document " +
                        "(id, doc_no, title, doc_type, urgency, secrecy_level, knowledge_scope, content, applicant_id, dept_id, status, version, distribution_status, ai_review_result, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                d.getId(), d.getDocNo(), d.getTitle(), d.getDocType(), d.getUrgency(), d.getSecrecyLevel(),
                d.getKnowledgeScope(), d.getContent(), d.getApplicantId(), d.getDeptId(), d.getStatus(),
                d.getVersion(), d.getDistributionStatus(), toJson(d.getAiReviewResult()), d.getCreatedAt(), d.getUpdatedAt());
    }

    @Override
    public void saveDocumentDistribution(DocumentDistribution d) {
        jdbcTemplate.update("REPLACE INTO oa_document_distribution " +
                        "(id, document_id, receiver_id, receiver_dept_id, status, distributed_at, received_at, reminded_at, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)",
                d.getId(), d.getDocumentId(), d.getReceiverId(), d.getReceiverDeptId(), d.getStatus(),
                d.getDistributedAt(), d.getReceivedAt(), d.getRemindedAt(), d.getCreatedAt(), d.getUpdatedAt());
    }

    @Override
    public void saveSealApplication(SealApplication a) {
        jdbcTemplate.update("REPLACE INTO oa_seal_log " +
                        "(id, seal_id, applicant_id, purpose, material_url, copies, take_out, matter_level, take_out_reason, take_out_location, supervisor_id, return_deadline, retention_until, use_time, return_time, status, created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                a.getId(), a.getSealId(), a.getApplicantId(), a.getPurpose(), a.getMaterialUrl(), a.getCopies(),
                a.isTakeOut() ? 1 : 0, a.getMatterLevel(), a.getTakeOutReason(), a.getTakeOutLocation(),
                a.getSupervisorId(), a.getReturnDeadline(), a.getRetentionUntil(), a.getUseTime(), a.getReturnTime(), a.getStatus(), a.getCreatedAt());
    }

    @Override
    public void saveSeal(Seal s) {
        jdbcTemplate.update("REPLACE INTO oa_seal (id, seal_name, seal_type, dept_id, keeper_id, status) VALUES (?,?,?,?,?,?)",
                s.getId(), s.getSealName(), s.getSealType(), s.getDeptId(), s.getKeeperId(), s.getStatus());
    }

    @Override
    public void saveSealTransfer(SealTransfer t) {
        jdbcTemplate.update("REPLACE INTO oa_seal_transfer " +
                        "(id, seal_id, transferor_id, receiver_id, supervisor_id, material_url, remark, transfer_time, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?)",
                t.getId(), t.getSealId(), t.getTransferorId(), t.getReceiverId(), t.getSupervisorId(),
                t.getMaterialUrl(), t.getRemark(), t.getTransferTime(), t.getCreatedAt(), t.getUpdatedAt());
    }

    @Override
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

    @Override
    public void saveTravel(Travel t) {
        jdbcTemplate.update("REPLACE INTO oa_travel " +
                        "(id, applicant_id, destination, start_date, end_date, reason, staff_level, travel_type, transport, budget, actual_expense, receipt_url, over_limit_reason, reimbursement_submitted, status, created_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                t.getId(), t.getApplicantId(), t.getDestination(), t.getStartDate(), t.getEndDate(), t.getReason(),
                t.getStaffLevel(), t.getTravelType(), t.getTransport(), t.getBudget(), t.getActualExpense(),
                t.getReceiptUrl(), t.getOverLimitReason(), t.isReimbursementSubmitted() ? 1 : 0, t.getStatus(), t.getCreatedAt());
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

    @Override
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

    @Override
    public void saveAuditLog(AuditLog l) {
        jdbcTemplate.update("REPLACE INTO sys_operation_log " +
                        "(id, operator_id, module, action, biz_type, biz_id, detail, created_at) VALUES (?,?,?,?,?,?,?,?)",
                l.getId(), l.getOperatorId(), l.getModule(), l.getAction(), l.getBizType(), l.getBizId(),
                l.getDetail(), l.getCreatedAt());
    }

    @Override
    public void saveNotification(Notification n) {
        jdbcTemplate.update("REPLACE INTO sys_notification " +
                        "(id, receiver_id, title, content, read_status, biz_type, biz_id, created_at) VALUES (?,?,?,?,?,?,?,?)",
                n.getId(), n.getReceiverId(), n.getTitle(), n.getContent(), n.isReadStatus() ? 1 : 0,
                n.getBizType(), n.getBizId(), n.getCreatedAt());
    }

    @Override
    public void saveMailMessage(MailMessage m) {
        jdbcTemplate.update("REPLACE INTO oa_mail_message " +
                        "(id, sender_id, subject, content, created_at, updated_at) VALUES (?,?,?,?,?,?)",
                m.getId(), m.getSenderId(), m.getSubject(), m.getContent(), m.getCreatedAt(), m.getUpdatedAt());
    }

    @Override
    public void saveMailRecipient(MailRecipient r) {
        jdbcTemplate.update("REPLACE INTO oa_mail_recipient " +
                        "(id, mail_id, user_id, recipient_type, read_status, read_at, email_status, email_error, email_sent_at, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                r.getId(), r.getMailId(), r.getUserId(), r.getRecipientType(), r.isReadStatus() ? 1 : 0,
                r.getReadAt(), r.getEmailStatus(), r.getEmailError(), r.getEmailSentAt(),
                r.getCreatedAt(), r.getUpdatedAt());
    }

    @Override
    public void saveAnnouncement(Announcement a) {
        jdbcTemplate.update("REPLACE INTO sys_announcement " +
                        "(id, title, content, category, target_type, target_dept_id, pinned, status, publisher_id, published_at, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                a.getId(), a.getTitle(), a.getContent(), a.getCategory(), a.getTargetType(), a.getTargetDeptId(),
                a.isPinned() ? 1 : 0, a.getStatus(), a.getPublisherId(), a.getPublishedAt(),
                a.getCreatedAt(), a.getUpdatedAt());
    }

    @Override
    public void saveFlowInstance(FlowInstance i) {
        jdbcTemplate.update("REPLACE INTO oa_flow_instance " +
                        "(id, biz_type, biz_id, current_node_key, status, starter_id, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?)",
                i.getId(), i.getBizType(), i.getBizId(), i.getCurrentNodeKey(), i.getStatus(), i.getStarterId(),
                i.getCreatedAt(), i.getUpdatedAt());
    }

    @Override
    public void saveFlowTask(FlowTask t) {
        jdbcTemplate.update("REPLACE INTO oa_flow_task " +
                        "(id, instance_id, biz_type, biz_id, node_key, approver_role, approver_id, status, due_time, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                t.getId(), t.getInstanceId(), t.getBizType(), t.getBizId(), t.getNodeKey(), t.getApproverRole(),
                t.getApproverId(), t.getStatus(), t.getDueTime(), t.getCreatedAt(), t.getUpdatedAt());
    }

    @Override
    public void saveMeetingParticipant(MeetingParticipant p) {
        jdbcTemplate.update(
            "REPLACE INTO oa_meeting_participant (id, meeting_id, user_id, is_recorder, minutes_confirmed, confirmed_at, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?)",
            p.getId(), p.getMeetingId(), p.getUserId(), p.isRecorder() ? 1 : 0,
            p.isMinutesConfirmed() ? 1 : 0, p.getConfirmedAt(), p.getCreatedAt(), p.getUpdatedAt());
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
