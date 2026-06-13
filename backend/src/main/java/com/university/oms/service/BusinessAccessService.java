package com.university.oms.service;

import com.university.oms.common.ForbiddenException;
import com.university.oms.model.Document;
import com.university.oms.model.DocumentDistribution;
import com.university.oms.model.Meeting;
import com.university.oms.model.Report;
import com.university.oms.model.SealApplication;
import com.university.oms.model.Travel;
import com.university.oms.model.User;
import com.university.oms.repository.OmsRepository;
import com.university.oms.security.AuthContext;
import org.springframework.stereotype.Service;

/**
 * 业务权限校验服务，统一管理各业务模块的访问控制和操作权限判断
 */
@Service
public class BusinessAccessService {
    private final OmsRepository repo;

    public BusinessAccessService(OmsRepository repo) {
        this.repo = repo;
    }

    /** 校验当前用户是否有权提交该公文 */
    public void requireDocumentSubmit(Document document) {
        User user = AuthContext.requireUser();
        if (!document.getApplicantId().equals(user.getId()) && !hasRole(user, "admin")) {
            deny("无权提交该公文");
        }
    }

    /** 校验当前用户是否有权归档该公文 */
    public void requireDocumentArchive(Document document) {
        User user = AuthContext.requireUser();
        if (!hasRole(user, "office_admin") && !hasRole(user, "admin")) {
            deny("无权归档该公文");
        }
    }

    /** 校验当前用户是否有权分发该公文 */
    public void requireDocumentDistribute(Document document) {
        requireDocumentArchive(document);
    }

    /** 校验当前用户是否有权签收该公文分发记录 */
    public void requireDocumentReceipt(DocumentDistribution distribution) {
        User user = AuthContext.requireUser();
        if (!distribution.getReceiverId().equals(user.getId()) && !hasRole(user, "admin")) {
            deny("无权签收该公文");
        }
    }

    /** 校验当前用户是否有权催办该公文签收 */
    public void requireDocumentRemind(Document document) {
        requireDocumentArchive(document);
    }

    /** 校验当前用户是否有权归档该会议纪要 */
    public void requireMeetingMinutesArchive(Meeting meeting) {
        User user = AuthContext.requireUser();
        if (!meeting.getOrganizerId().equals(user.getId())
                && !hasRole(user, "office_admin")
                && !hasRole(user, "admin")) {
            deny("无权归档该会议纪要");
        }
    }

    /** 校验当前用户是否为会议记录员 */
    public void requireMeetingRecorder(Meeting meeting) {
        User user = AuthContext.requireUser();
        if (!user.getId().equals(meeting.getRecorderId())) {
            throw new ForbiddenException("只有记录员可以填写会议纪要");
        }
    }

    /** 校验当前用户是否为会议组织者 */
    public void requireMeetingOrganizer(Meeting meeting) {
        User user = AuthContext.requireUser();
        if (!user.getId().equals(meeting.getOrganizerId())) {
            throw new ForbiddenException("只有组织者可以操作");
        }
    }

    /** 校验当前用户是否有权批复归档请示报告 */
    public void requireReportReply(Report report) {
        User user = AuthContext.requireUser();
        if (!hasRole(user, "office_admin")
                && !hasRole(user, "school_leader")
                && !hasRole(user, "admin")) {
            deny("无权批复归档该请示报告");
        }
    }

    /** 校验当前用户是否有权提交该差旅报销 */
    public void requireTravelReimburse(Travel travel) {
        User user = AuthContext.requireUser();
        if (!travel.getApplicantId().equals(user.getId()) && !hasRole(user, "admin")) {
            deny("无权提交该差旅报销");
        }
    }

    /** 校验当前用户是否有权读取指定业务数据 */
    public void requireBusinessRead(String bizType, Long bizId) {
        if (!canReadBusiness(bizType, bizId)) {
            deny("无权访问该业务数据");
        }
    }

    /** 校验指定用户是否有权审批该业务 */
    public void requireBusinessApproval(User user, String bizType, Long bizId, String nodeKey, String approverRole) {
        if (!canHandleApproval(user, bizType, bizId, nodeKey, approverRole)) {
            deny("No permission to approve this business item");
        }
    }

    /** 校验当前用户是否有权上传指定业务的附件 */
    public void requireAttachmentUpload(String bizType, Long bizId) {
        if (!"seal".equals(bizType)) {
            deny("当前业务不支持文件上传");
        }
        SealApplication application = requireSealApplication(bizId);
        User user = AuthContext.requireUser();
        if (!application.getApplicantId().equals(user.getId()) && !hasRole(user, "admin")) {
            deny("无权上传该用印申请材料");
        }
        if (!"draft".equals(application.getStatus())) {
            deny("只有草稿用印申请可以上传材料");
        }
    }

    /** 校验当前用户是否有权编辑指定业务的附件 */
    public void requireAttachmentEdit(String bizType, Long bizId) {
        SealApplication application = requireSealMaterialApplication(bizType, bizId);
        User user = AuthContext.requireUser();
        if (!"draft".equals(application.getStatus()) || !application.getApplicantId().equals(user.getId())) {
            deny("只有申请人可在草稿阶段修改材料");
        }
    }

    /** 校验当前用户是否有权删除指定业务的附件 */
    public void requireAttachmentDelete(String bizType, Long bizId) {
        SealApplication application = requireSealMaterialApplication(bizType, bizId);
        User user = AuthContext.requireUser();
        if ("draft".equals(application.getStatus()) && application.getApplicantId().equals(user.getId())) {
            return;
        }
        if (hasRole(user, "office_admin") || hasRole(user, "admin")) {
            return;
        }
        deny("无权删除该用印材料");
    }

    /** 校验当前用户是否有权查看已删除材料记录 */
    public void requireViewDeletedAttachments() {
        User user = AuthContext.requireUser();
        if (!hasRole(user, "office_admin") && !hasRole(user, "admin")) {
            deny("无权查看已删除材料记录");
        }
    }

    /**
     * 判断当前用户是否可读取指定业务数据
     * 综合考虑用户角色、申请人身份、部门归属等因素
     */
    public boolean canReadBusiness(String bizType, Long bizId) {
        User user = AuthContext.requireUser();
        if (hasRole(user, "admin")) {
            return true;
        }
        if ("document".equals(bizType)) {
            Document document = repo.findDocumentById(bizId);
            return document != null && canReadForApplicant(user, document.getApplicantId(), document.getDeptId(),
                    hasRole(user, "office_admin") || hasRole(user, "school_leader"))
                    || isDocumentReceiver(user, bizId);
        }
        if ("meeting".equals(bizType)) {
            Meeting meeting = repo.findMeetingById(bizId);
            return meeting != null && canReadForApplicant(user, meeting.getOrganizerId(), departmentOf(meeting.getOrganizerId()),
                    hasRole(user, "office_admin") || hasRole(user, "school_leader") || hasRole(user, "security_staff"));
        }
        if ("seal".equals(bizType)) {
            SealApplication application = repo.findSealApplicationById(bizId);
            return application != null && canReadForApplicant(user, application.getApplicantId(), departmentOf(application.getApplicantId()),
                    hasRole(user, "office_admin") || hasRole(user, "seal_keeper"));
        }
        if ("travel".equals(bizType)) {
            Travel travel = repo.findTravelById(bizId);
            return travel != null && canReadForApplicant(user, travel.getApplicantId(), departmentOf(travel.getApplicantId()),
                    hasRole(user, "finance_staff") || hasRole(user, "school_leader"));
        }
        if ("report".equals(bizType)) {
            Report report = repo.findReportById(bizId);
            return report != null && canReadForApplicant(user, report.getApplicantId(), report.getDeptId(),
                    hasRole(user, "office_admin") || hasRole(user, "school_leader"));
        }
        return false;
    }

    /** 判断指定用户是否有权审批指定业务节点 */
    public boolean canHandleApproval(User user, String bizType, Long bizId, String nodeKey, String approverRole) {
        if (user == null) {
            return false;
        }
        if (hasRole(user, "admin")) {
            return true;
        }
        if (approverRole == null || !hasRole(user, approverRole)) {
            return false;
        }
        // 部门负责人只能审批本部门的业务
        if ("dept_head".equals(approverRole)) {
            Long deptId = businessDepartment(bizType, bizId);
            return deptId != null && deptId.equals(user.getDeptId());
        }
        return true;
    }

    /** 判断用户是否可读取（基于申请人、管理角色或部门负责人身份） */
    private boolean canReadForApplicant(User user, Long applicantId, Long deptId, boolean manager) {
        return user.getId().equals(applicantId)
                || manager
                || (hasRole(user, "dept_head") && deptId != null && deptId.equals(user.getDeptId()));
    }

    /** 获取指定业务所属的部门ID */
    private Long businessDepartment(String bizType, Long bizId) {
        if ("document".equals(bizType)) {
            Document document = repo.findDocumentById(bizId);
            return document == null ? null : document.getDeptId();
        }
        if ("meeting".equals(bizType)) {
            Meeting meeting = repo.findMeetingById(bizId);
            return meeting == null ? null : departmentOf(meeting.getOrganizerId());
        }
        if ("seal".equals(bizType)) {
            SealApplication application = repo.findSealApplicationById(bizId);
            return application == null ? null : departmentOf(application.getApplicantId());
        }
        if ("travel".equals(bizType)) {
            Travel travel = repo.findTravelById(bizId);
            return travel == null ? null : departmentOf(travel.getApplicantId());
        }
        if ("report".equals(bizType)) {
            Report report = repo.findReportById(bizId);
            return report == null ? null : report.getDeptId();
        }
        return null;
    }

    private Long departmentOf(Long userId) {
        User user = repo.findUserById(userId);
        return user == null ? null : user.getDeptId();
    }

    /** 判断用户是否为公文的分发接收人 */
    private boolean isDocumentReceiver(User user, Long documentId) {
        for (DocumentDistribution distribution : repo.findDocumentDistributionsByDocumentId(documentId)) {
            if (user.getId().equals(distribution.getReceiverId())) {
                return true;
            }
        }
        return false;
    }

    /** 校验并返回用印材料所属的用印申请 */
    private SealApplication requireSealMaterialApplication(String bizType, Long bizId) {
        if (!"seal".equals(bizType)) {
            deny("该业务暂不支持材料维护");
        }
        return requireSealApplication(bizId);
    }

    private SealApplication requireSealApplication(Long bizId) {
        SealApplication application = repo.findSealApplicationById(bizId);
        if (application == null) {
            deny("用印申请不存在");
        }
        return application;
    }

    private boolean hasRole(User user, String role) {
        return user.getRoleKeys().contains(role);
    }

    private void deny(String message) {
        throw new ForbiddenException(message);
    }
}
