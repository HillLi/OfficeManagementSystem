package com.university.oms.service;

import com.university.oms.common.BusinessException;
import com.university.oms.common.ForbiddenException;
import com.university.oms.dto.AnnouncementRequest;
import com.university.oms.model.Announcement;
import com.university.oms.model.Department;
import com.university.oms.model.User;
import com.university.oms.repository.OmsRepository;
import com.university.oms.security.AuthContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告管理服务，提供公告的创建、发布、撤回和查询功能
 */
@Service
public class AnnouncementService {
    private final OmsRepository repo;
    private final WorkflowService workflowService;

    public AnnouncementService(OmsRepository repo, WorkflowService workflowService) {
        this.repo = repo;
        this.workflowService = workflowService;
    }

    /** 查询公告列表，支持是否包含草稿 */
    public List<Announcement> list(boolean includeDrafts) {
        User user = AuthContext.requireUser();
        boolean maintainer = canMaintain(user);
        return repo.findAllAnnouncements().stream()
                .filter(a -> includeDrafts && maintainer || canRead(user, a))
                .sorted(announcementOrder())
                .map(this::withTargetDeptName)
                .collect(Collectors.toList());
    }

    /** 获取最新的N条已发布公告 */
    public List<Announcement> latest(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 10));
        return list(false).stream().limit(safeLimit).collect(Collectors.toList());
    }

    /** 获取单条公告详情 */
    public Announcement get(Long id) {
        User user = AuthContext.requireUser();
        Announcement announcement = require(id);
        if (!canMaintain(user) && !canRead(user, announcement)) {
            throw new ForbiddenException("无权查看该公告");
        }
        return withTargetDeptName(announcement);
    }

    /** 创建新公告（草稿状态） */
    public Announcement create(AnnouncementRequest request) {
        User user = AuthContext.requireUser();
        requireMaintainer(user);
        Announcement announcement = new Announcement();
        OmsRepository.fillEntity(announcement, repo.nextId());
        apply(announcement, request);
        announcement.setStatus("draft");
        announcement.setPublisherId(user.getId());
        repo.saveAnnouncement(announcement);
        workflowService.audit("announcement", "create", "announcement", announcement.getId(), announcement.getTitle());
        return withTargetDeptName(announcement);
    }

    /** 更新公告内容 */
    public Announcement update(Long id, AnnouncementRequest request) {
        requireMaintainer(AuthContext.requireUser());
        Announcement announcement = require(id);
        apply(announcement, request);
        announcement.setUpdatedAt(LocalDateTime.now());
        repo.saveAnnouncement(announcement);
        workflowService.audit("announcement", "update", "announcement", announcement.getId(), announcement.getTitle());
        return withTargetDeptName(announcement);
    }

    /** 发布公告 */
    public Announcement publish(Long id) {
        User user = AuthContext.requireUser();
        requireMaintainer(user);
        Announcement announcement = require(id);
        announcement.setStatus("published");
        announcement.setPublisherId(user.getId());
        announcement.setPublishedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());
        repo.saveAnnouncement(announcement);
        workflowService.audit("announcement", "publish", "announcement", announcement.getId(), announcement.getTitle());
        return withTargetDeptName(announcement);
    }

    /** 撤回公告 */
    public Announcement withdraw(Long id) {
        requireMaintainer(AuthContext.requireUser());
        Announcement announcement = require(id);
        announcement.setStatus("withdrawn");
        announcement.setUpdatedAt(LocalDateTime.now());
        repo.saveAnnouncement(announcement);
        workflowService.audit("announcement", "withdraw", "announcement", announcement.getId(), announcement.getTitle());
        return withTargetDeptName(announcement);
    }

    /** 将请求参数填充到公告对象中，并校验发布范围 */
    private void apply(Announcement announcement, AnnouncementRequest request) {
        announcement.setTitle(trimRequired(request.getTitle(), "公告标题不能为空"));
        announcement.setContent(trimRequired(request.getContent(), "公告内容不能为空"));
        announcement.setCategory(blankToDefault(request.getCategory(), "notice"));
        String targetType = blankToDefault(request.getTargetType(), "all");
        if (!"all".equals(targetType) && !"dept".equals(targetType)) {
            throw new BusinessException("公告范围仅支持 all 或 dept");
        }
        if ("dept".equals(targetType) && request.getTargetDeptId() == null) {
            throw new BusinessException("部门公告必须选择发布部门");
        }
        if ("dept".equals(targetType) && repo.findDepartmentById(request.getTargetDeptId()) == null) {
            throw new BusinessException("发布部门不存在");
        }
        announcement.setTargetType(targetType);
        announcement.setTargetDeptId("dept".equals(targetType) ? request.getTargetDeptId() : null);
        announcement.setTargetDeptName("dept".equals(targetType) ? resolveDeptName(request.getTargetDeptId()) : null);
        announcement.setPinned(Boolean.TRUE.equals(request.getPinned()));
    }

    /** 补充公告的目标部门名称 */
    private Announcement withTargetDeptName(Announcement announcement) {
        announcement.setTargetDeptName("dept".equals(announcement.getTargetType())
                ? resolveDeptName(announcement.getTargetDeptId())
                : null);
        return announcement;
    }

    private String resolveDeptName(Long deptId) {
        Department dept = deptId == null ? null : repo.findDepartmentById(deptId);
        return dept == null ? null : dept.getDeptName();
    }

    /** 根据ID查询公告，不存在则抛异常 */
    private Announcement require(Long id) {
        Announcement announcement = repo.findAnnouncementById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        return announcement;
    }

    /** 判断用户是否可阅读该公告（已发布且范围匹配） */
    private boolean canRead(User user, Announcement announcement) {
        if (!"published".equals(announcement.getStatus())) {
            return false;
        }
        return "all".equals(announcement.getTargetType())
                || user.getDeptId().equals(announcement.getTargetDeptId())
                || canMaintain(user);
    }

    /** 校验当前用户是否具有公告维护权限 */
    private void requireMaintainer(User user) {
        if (!canMaintain(user)) {
            throw new ForbiddenException("无公告维护权限");
        }
    }

    /** 判断用户是否为管理员或党办校办人员 */
    private boolean canMaintain(User user) {
        return user.getRoleKeys().contains("admin") || user.getRoleKeys().contains("office_admin");
    }

    /** 公告排序规则：置顶优先，然后按发布/更新时间倒序 */
    private Comparator<Announcement> announcementOrder() {
        return Comparator.comparing(Announcement::isPinned).reversed()
                .thenComparing((Announcement a) -> timeOrCreated(a), Comparator.reverseOrder());
    }

    private LocalDateTime timeOrCreated(Announcement announcement) {
        if (announcement.getPublishedAt() != null) {
            return announcement.getPublishedAt();
        }
        if (announcement.getUpdatedAt() != null) {
            return announcement.getUpdatedAt();
        }
        return announcement.getCreatedAt();
    }

    private String trimRequired(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(message);
        }
        return value.trim();
    }

    private String blankToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }
}
