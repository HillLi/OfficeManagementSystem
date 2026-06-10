package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.dto.WorkflowGuideResponse;
import com.university.oms.model.*;
import com.university.oms.repository.OmsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkflowGuideService {
    private final OmsRepository repo;
    private final BusinessAccessService accessService;

    public WorkflowGuideService(OmsRepository repo, BusinessAccessService accessService) {
        this.repo = repo;
        this.accessService = accessService;
    }

    public WorkflowGuideResponse guide(String bizType, Long bizId) {
        accessService.requireBusinessRead(bizType, bizId);
        if ("document".equals(bizType)) {
            return documentGuide(bizId);
        }
        if ("seal".equals(bizType)) {
            return sealGuide(bizId);
        }
        if ("meeting".equals(bizType)) {
            return meetingGuide(bizId);
        }
        if ("report".equals(bizType)) {
            return reportGuide(bizId);
        }
        if ("travel".equals(bizType)) {
            return travelGuide(bizId);
        }
        throw new BusinessException("暂不支持该业务流程导览");
    }

    // --- Document guide ---

    private WorkflowGuideResponse documentGuide(Long id) {
        Document document = repo.findDocumentById(id);
        if (document == null) {
            throw new BusinessException("公文不存在");
        }
        WorkflowGuideResponse guide = base("document", id, document.getTitle(), document.getStatus());
        guide.setSteps(documentSteps(id, document.getStatus()));
        updateCurrentNodeKey(guide);
        return guide;
    }

    private List<WorkflowGuideResponse.Step> documentSteps(Long id, String businessStatus) {
        List<WorkflowGuideResponse.Step> steps = new ArrayList<WorkflowGuideResponse.Step>();
        steps.add(recordedStep("document", id, "create", "公文起草", "business", "create"));
        steps.add(auditStep("document", id, "ai_review", "AI格式校验", "system", "ai_review", "optional"));
        steps.add(recordedStep("document", id, "submit", "提交审批", "business", "submit"));
        steps.add(approvalStep("document", id, "pending_dept", "部门负责人审批"));
        steps.add(approvalStep("document", id, "pending_office", "党办校办审核"));
        steps.add(approvalStep("document", id, "pending_leader", "校级领导签发"));
        steps.add(documentDistributionStep(id));
        steps.add(documentReceiptStep(id));
        steps.add(recordedStep("document", id, "archive", "公文归档", "business", "archive"));
        markDocumentCurrent(steps, businessStatus);
        return steps;
    }

    private WorkflowGuideResponse.Step documentDistributionStep(Long id) {
        WorkflowGuideResponse.Step step = step("distribute", "公文分发", "business", "waiting");
        for (DocumentDistribution distribution : repo.findDocumentDistributionsByDocumentId(id)) {
            if (id.equals(distribution.getDocumentId())) {
                step.setStatus("done");
                step.setTime(distribution.getDistributedAt());
                step.setOpinion("已分发至用户#" + distribution.getReceiverId());
                return step;
            }
        }
        return step;
    }

    private WorkflowGuideResponse.Step documentReceiptStep(Long id) {
        WorkflowGuideResponse.Step step = step("receipt", "接收人签收", "business", "waiting");
        boolean found = false;
        boolean allReceived = true;
        for (DocumentDistribution distribution : repo.findDocumentDistributionsByDocumentId(id)) {
            if (id.equals(distribution.getDocumentId())) {
                found = true;
                if (!"received".equals(distribution.getStatus())) {
                    allReceived = false;
                } else {
                    step.setTime(distribution.getReceivedAt());
                }
            }
        }
        if (found) {
            step.setStatus(allReceived ? "done" : "current");
            step.setOpinion(allReceived ? "接收人已签收" : "存在待签收记录");
        }
        return step;
    }

    // --- Seal guide ---

    private WorkflowGuideResponse sealGuide(Long id) {
        SealApplication app = repo.findSealApplicationById(id);
        if (app == null) throw new BusinessException("用印申请不存在");
        WorkflowGuideResponse guide = base("seal", id, "用印申请 #" + id, app.getStatus());
        List<WorkflowGuideResponse.Step> steps = new ArrayList<WorkflowGuideResponse.Step>();
        steps.add(step("draft", "保存草稿", "business", "done"));
        steps.add(step("materials", "上传材料", "business", activeMaterialCount("seal", id) > 0 ? "done" : "waiting"));
        steps.add(recordedBusinessStep("seal", id, "submit", "提交申请", "submit"));
        steps.addAll(sealApprovalSteps(app));
        steps.add(step("approved", "审批通过", "approval", reached(app.getStatus(), "approved") ? "done" : "waiting"));
        steps.add(recordedBusinessStep("seal", id, "use", "用印登记", "use"));
        steps.add(recordedBusinessStep("seal", id, "return", "归还确认", "return"));
        markCurrentByBusinessStatus(steps, app.getStatus());
        markPostApprovalCurrent(steps, app.getStatus(), "approved", "use", "used", "return");
        guide.setSteps(steps);
        updateCurrentNodeKey(guide);
        return guide;
    }

    private List<WorkflowGuideResponse.Step> sealApprovalSteps(SealApplication app) {
        List<WorkflowGuideResponse.Step> steps = new ArrayList<WorkflowGuideResponse.Step>();
        Seal seal = repo.findSealById(app.getSealId());
        boolean schoolSeal = seal != null && seal.getSealName().contains("北京大学");
        boolean major = "重大事项".equals(app.getMatterLevel());
        if (!schoolSeal) {
            steps.add(approvalStep("seal", app.getId(), "pending_dept", "部门负责人审批"));
        }
        if (schoolSeal || major) {
            steps.add(approvalStep("seal", app.getId(), "pending_office", "党办校办审核"));
        }
        if (schoolSeal && major) {
            steps.add(approvalStep("seal", app.getId(), "pending_leader", "校级领导审批"));
        }
        return steps;
    }

    // --- Meeting guide ---

    private WorkflowGuideResponse meetingGuide(Long id) {
        Meeting meeting = repo.findMeetingById(id);
        if (meeting == null) throw new BusinessException("会议不存在");
        WorkflowGuideResponse guide = base("meeting", id, meeting.getTitle(), meeting.getStatus());
        List<WorkflowGuideResponse.Step> steps = new ArrayList<WorkflowGuideResponse.Step>();
        steps.add(recordedBusinessStep("meeting", id, "submit", "提交会议", "submit"));
        if (meeting.isLargeActivity()) {
            steps.add(approvalStep("meeting", id, "pending_security", "保卫部门审核"));
        }
        steps.add(approvalStep("meeting", id, "pending_dept", "部门负责人审批"));
        if (meeting.isLargeActivity()) {
            steps.add(approvalStep("meeting", id, "pending_leader", "校级领导审批"));
        }
        steps.add(step("approved", "审批通过", "approval", reached(meeting.getStatus(), "approved") ? "done" : "waiting"));
        WorkflowGuideResponse.Step archive = recordedBusinessStep("meeting", id,
                "archive_minutes", "纪要归档", "archive_minutes");
        if ("archived".equals(meeting.getStatus())) {
            archive.setStatus("done");
        }
        steps.add(archive);
        markCurrentByBusinessStatus(steps, meeting.getStatus());
        markPostApprovalCurrent(steps, meeting.getStatus(), "approved", "archive_minutes", null, null);
        guide.setSteps(steps);
        updateCurrentNodeKey(guide);
        return guide;
    }

    // --- Report guide ---

    private WorkflowGuideResponse reportGuide(Long id) {
        Report report = repo.findReportById(id);
        if (report == null) throw new BusinessException("请示报告不存在");
        WorkflowGuideResponse guide = base("report", id, report.getTitle(), report.getStatus());
        List<WorkflowGuideResponse.Step> steps = new ArrayList<WorkflowGuideResponse.Step>();
        steps.add(recordedBusinessStep("report", id, "submit", "提交请示报告", "submit"));
        steps.add(approvalStep("report", id, "pending_secret_review", "保密审查"));
        steps.add(approvalStep("report", id, "pending_dept", "部门负责人审批"));
        steps.add(approvalStep("report", id, "pending_leader", "校级领导审批"));
        steps.add(step("approved", "审批通过", "approval", reached(report.getStatus(), "approved") ? "done" : "waiting"));
        WorkflowGuideResponse.Step reply = recordedBusinessStep("report", id, "reply", "批复归档", "reply");
        if ("archived".equals(report.getStatus())) {
            reply.setStatus("done");
        }
        steps.add(reply);
        markCurrentByBusinessStatus(steps, report.getStatus());
        markPostApprovalCurrent(steps, report.getStatus(), "approved", "reply", null, null);
        guide.setSteps(steps);
        updateCurrentNodeKey(guide);
        return guide;
    }

    // --- Travel guide ---

    private WorkflowGuideResponse travelGuide(Long id) {
        Travel travel = repo.findTravelById(id);
        if (travel == null) throw new BusinessException("差旅申请不存在");
        WorkflowGuideResponse guide = base("travel", id, "差旅申请 #" + id, travel.getStatus());
        List<WorkflowGuideResponse.Step> steps = new ArrayList<WorkflowGuideResponse.Step>();
        steps.add(recordedBusinessStep("travel", id, "submit", "提交差旅", "submit"));
        steps.add(approvalStep("travel", id, "pending_dept", "部门负责人审批"));
        steps.add(approvalStep("travel", id, "pending_finance", "财务审核"));
        steps.add(step("approved", "审批通过", "approval", reached(travel.getStatus(), "approved") ? "done" : "waiting"));
        WorkflowGuideResponse.Step reimburse = recordedBusinessStep("travel", id,
                "submit_reimbursement", "提交报销", "reimburse");
        steps.add(reimburse);
        WorkflowGuideResponse.Step recheck = step("finance_recheck", "财务复核", "approval",
                "pending_finance".equals(travel.getStatus()) && travel.isReimbursementSubmitted() ? "current" : "waiting");
        applyTravelRecheckRecord(recheck, id, travel);
        steps.add(recheck);
        steps.add(step("archive", "归档", "business", "archived".equals(travel.getStatus()) ? "done" : "waiting"));
        markPostApprovalCurrent(steps, travel.getStatus(), "approved", "submit_reimbursement", null, null);
        guide.setSteps(steps);
        updateCurrentNodeKey(guide);
        return guide;
    }

    // --- Shared helpers ---

    private WorkflowGuideResponse base(String bizType, Long id, String title, String status) {
        WorkflowGuideResponse guide = new WorkflowGuideResponse();
        guide.setBizType(bizType);
        guide.setBizId(id);
        guide.setTitle(title);
        guide.setCurrentNodeKey(status);
        guide.setStatus(status);
        return guide;
    }

    private WorkflowGuideResponse.Step step(String key, String label, String type, String status) {
        return new WorkflowGuideResponse.Step(key, label, type, status);
    }

    private WorkflowGuideResponse.Step recordedStep(String bizType, Long id, String key,
                                                     String label, String type, String action) {
        WorkflowGuideResponse.Step step = step(key, label, type, "waiting");
        applyRecord(step, bizType, id, action);
        if (step.getTime() != null) {
            step.setStatus("done");
        }
        return step;
    }

    private WorkflowGuideResponse.Step recordedBusinessStep(String bizType, Long id,
                                                             String key, String label, String action) {
        WorkflowGuideResponse.Step step = step(key, label, "business", "waiting");
        for (ApprovalRecord record : repo.findApprovalsByBizTypeAndBizId(bizType, id)) {
            if (action.equals(record.getAction())) {
                step.setStatus("done");
                step.setOperatorId(record.getOperatorId());
                step.setOperatorName(userName(record.getOperatorId()));
                step.setOpinion(record.getOpinion());
                step.setTime(record.getCreatedAt());
            }
        }
        return step;
    }

    private WorkflowGuideResponse.Step approvalStep(String bizType, Long id, String key, String label) {
        WorkflowGuideResponse.Step step = step(key, label, "approval", "waiting");
        for (ApprovalRecord record : repo.findApprovalsByBizTypeAndBizId(bizType, id)) {
            if ("approve".equals(record.getAction()) || "reject".equals(record.getAction())) {
                if (matchesApprovalIndex(bizType, id, key, record)) {
                    step.setOperatorId(record.getOperatorId());
                    step.setOperatorName(userName(record.getOperatorId()));
                    step.setOpinion(record.getOpinion());
                    step.setTime(record.getCreatedAt());
                    step.setStatus("reject".equals(record.getAction()) ? "rejected" : "done");
                }
            }
        }
        applyTaskDetails(step, bizType, id);
        return step;
    }

    private boolean matchesApprovalIndex(String bizType, Long id, String key, ApprovalRecord target) {
        List<String> keys = approvalKeys(bizType, id);
        int approvalIndex = 0;
        for (ApprovalRecord record : repo.findApprovalsByBizTypeAndBizId(bizType, id)) {
            if ("approve".equals(record.getAction()) || "reject".equals(record.getAction())) {
                if (record.getId().equals(target.getId())) {
                    return approvalIndex < keys.size() && key.equals(keys.get(approvalIndex));
                }
                approvalIndex++;
            }
        }
        return false;
    }

    private List<String> approvalKeys(String bizType, Long id) {
        List<String> keys = new ArrayList<String>();
        if ("document".equals(bizType)) {
            keys.add("pending_dept");
            keys.add("pending_office");
            keys.add("pending_leader");
        } else if ("travel".equals(bizType)) {
            keys.add("pending_dept");
            keys.add("pending_finance");
        } else if ("report".equals(bizType)) {
            keys.add("pending_secret_review");
            keys.add("pending_dept");
            keys.add("pending_leader");
        } else if ("meeting".equals(bizType)) {
            Meeting meeting = repo.findMeetingById(id);
            if (meeting != null && meeting.isLargeActivity()) {
                keys.add("pending_security");
            }
            keys.add("pending_dept");
            if (meeting != null && meeting.isLargeActivity()) {
                keys.add("pending_leader");
            }
        } else if ("seal".equals(bizType)) {
            SealApplication app = repo.findSealApplicationById(id);
            if (app != null) {
                Seal seal = repo.findSealById(app.getSealId());
                boolean schoolSeal = seal != null && seal.getSealName().contains("北京大学");
                boolean major = "重大事项".equals(app.getMatterLevel());
                if (!schoolSeal) {
                    keys.add("pending_dept");
                }
                if (schoolSeal || major) {
                    keys.add("pending_office");
                }
                if (schoolSeal && major) {
                    keys.add("pending_leader");
                }
            }
        }
        return keys;
    }

    private WorkflowGuideResponse.Step auditStep(String bizType, Long id, String key, String label,
                                                  String type, String action, String emptyStatus) {
        WorkflowGuideResponse.Step step = step(key, label, type, emptyStatus);
        for (AuditLog log : repo.findAuditLogsByBizTypeAndBizId(bizType, id)) {
            if (action.equals(log.getAction())) {
                step.setStatus("done");
                step.setOperatorId(log.getOperatorId());
                step.setOperatorName(userName(log.getOperatorId()));
                step.setOpinion(log.getDetail());
                step.setTime(log.getCreatedAt());
            }
        }
        return step;
    }

    private void applyRecord(WorkflowGuideResponse.Step step, String bizType, Long id, String action) {
        for (ApprovalRecord record : repo.findApprovalsByBizTypeAndBizId(bizType, id)) {
            if (action.equals(record.getAction())) {
                step.setOperatorId(record.getOperatorId());
                step.setOperatorName(userName(record.getOperatorId()));
                step.setOpinion(record.getOpinion());
                step.setTime(record.getCreatedAt());
            }
        }
    }

    private void applyTaskDetails(WorkflowGuideResponse.Step step, String bizType, Long bizId) {
        for (FlowTask task : repo.findFlowTasksByBizTypeAndBizId(bizType, bizId)) {
            if (step.getKey().equals(task.getNodeKey())) {
                step.setRoleKey(task.getApproverRole());
                step.setRoleLabel(roleLabel(task.getApproverRole()));
                step.setDueTime(task.getDueTime());
                if ("pending".equals(task.getStatus()) && !"done".equals(step.getStatus())
                        && !"rejected".equals(step.getStatus())) {
                    step.setStatus("current");
                }
                if (task.getApproverId() != null && step.getOperatorId() == null) {
                    step.setOperatorId(task.getApproverId());
                    step.setOperatorName(userName(task.getApproverId()));
                }
            }
        }
    }

    private String roleLabel(String roleKey) {
        if ("dept_head".equals(roleKey)) return "部门负责人";
        if ("office_admin".equals(roleKey)) return "党办校办人员";
        if ("school_leader".equals(roleKey)) return "校级领导";
        if ("finance_staff".equals(roleKey)) return "财务人员";
        if ("security_staff".equals(roleKey)) return "保卫人员";
        if ("seal_keeper".equals(roleKey)) return "印章保管人";
        return roleKey;
    }

    private int activeMaterialCount(String bizType, Long bizId) {
        int count = 0;
        for (Attachment attachment : repo.findAttachmentsByBizTypeAndBizId(bizType, bizId)) {
            if (!attachment.isDeleted()) {
                count++;
            }
        }
        return count;
    }

    private boolean reached(String currentStatus, String targetStatus) {
        if (targetStatus.equals(currentStatus)) {
            return true;
        }
        if ("approved".equals(targetStatus)) {
            return "used".equals(currentStatus) || "returned".equals(currentStatus)
                    || "archived".equals(currentStatus) || "completed".equals(currentStatus);
        }
        return false;
    }

    private void markCurrentByBusinessStatus(List<WorkflowGuideResponse.Step> steps, String currentStatus) {
        for (WorkflowGuideResponse.Step step : steps) {
            if (currentStatus != null && currentStatus.equals(step.getKey()) && "waiting".equals(step.getStatus())) {
                step.setStatus("current");
            }
        }
    }

    private void markDocumentCurrent(List<WorkflowGuideResponse.Step> steps, String businessStatus) {
        if ("approved".equals(businessStatus)) {
            markWaitingStepCurrent(steps, "distribute");
        } else if ("archived".equals(businessStatus)) {
            markStepDone(steps, "archive");
        }
    }

    private void markPostApprovalCurrent(List<WorkflowGuideResponse.Step> steps, String businessStatus,
                                         String approvedStatus, String approvedNextKey,
                                         String usedStatus, String usedNextKey) {
        if (approvedStatus != null && approvedStatus.equals(businessStatus)) {
            markWaitingStepCurrent(steps, approvedNextKey);
        }
        if (usedStatus != null && usedStatus.equals(businessStatus)) {
            markWaitingStepCurrent(steps, usedNextKey);
        }
    }

    private void markWaitingStepCurrent(List<WorkflowGuideResponse.Step> steps, String key) {
        if (key == null || hasCurrentOrRejected(steps)) {
            return;
        }
        for (WorkflowGuideResponse.Step step : steps) {
            if (key.equals(step.getKey()) && "waiting".equals(step.getStatus())) {
                step.setStatus("current");
                return;
            }
        }
    }

    private void markStepDone(List<WorkflowGuideResponse.Step> steps, String key) {
        for (WorkflowGuideResponse.Step step : steps) {
            if (key.equals(step.getKey()) && !"rejected".equals(step.getStatus())) {
                step.setStatus("done");
                return;
            }
        }
    }

    private boolean hasCurrentOrRejected(List<WorkflowGuideResponse.Step> steps) {
        for (WorkflowGuideResponse.Step step : steps) {
            if ("current".equals(step.getStatus()) || "rejected".equals(step.getStatus())) {
                return true;
            }
        }
        return false;
    }

    private void updateCurrentNodeKey(WorkflowGuideResponse guide) {
        if (guide.getSteps() == null) {
            return;
        }
        for (WorkflowGuideResponse.Step step : guide.getSteps()) {
            if ("current".equals(step.getStatus()) || "rejected".equals(step.getStatus())) {
                guide.setCurrentNodeKey(step.getKey());
                return;
            }
        }
    }

    private void applyTravelRecheckRecord(WorkflowGuideResponse.Step step, Long id, Travel travel) {
        if (!travel.isReimbursementSubmitted()) {
            return;
        }
        ApprovalRecord latestFinanceApproval = null;
        for (ApprovalRecord record : repo.findApprovalsByBizTypeAndBizId("travel", id)) {
            if ("approve".equals(record.getAction()) || "reject".equals(record.getAction())) {
                latestFinanceApproval = record;
            }
        }
        if (latestFinanceApproval != null && "archived".equals(travel.getStatus())) {
            step.setOperatorId(latestFinanceApproval.getOperatorId());
            step.setOperatorName(userName(latestFinanceApproval.getOperatorId()));
            step.setOpinion(latestFinanceApproval.getOpinion());
            step.setTime(latestFinanceApproval.getCreatedAt());
            step.setStatus("reject".equals(latestFinanceApproval.getAction()) ? "rejected" : "done");
        }
    }

    private String userName(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = repo.findUserById(userId);
        return user == null ? null : user.getRealName();
    }
}
