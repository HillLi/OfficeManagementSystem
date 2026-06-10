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

@Service
public class AnnouncementService {
    private final OmsRepository repo;
    private final WorkflowService workflowService;

    public AnnouncementService(OmsRepository repo, WorkflowService workflowService) {
        this.repo = repo;
        this.workflowService = workflowService;
    }

    public List<Announcement> list(boolean includeDrafts) {
        User user = AuthContext.requireUser();
        boolean maintainer = canMaintain(user);
        return repo.findAllAnnouncements().stream()
                .filter(a -> includeDrafts && maintainer || canRead(user, a))
                .sorted(announcementOrder())
                .map(this::withTargetDeptName)
                .collect(Collectors.toList());
    }

    public List<Announcement> latest(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 10));
        return list(false).stream().limit(safeLimit).collect(Collectors.toList());
    }

    public Announcement get(Long id) {
        User user = AuthContext.requireUser();
        Announcement announcement = require(id);
        if (!canMaintain(user) && !canRead(user, announcement)) {
            throw new ForbiddenException("无权查看该公告");
        }
        return withTargetDeptName(announcement);
    }

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

    public Announcement update(Long id, AnnouncementRequest request) {
        requireMaintainer(AuthContext.requireUser());
        Announcement announcement = require(id);
        apply(announcement, request);
        announcement.setUpdatedAt(LocalDateTime.now());
        repo.saveAnnouncement(announcement);
        workflowService.audit("announcement", "update", "announcement", announcement.getId(), announcement.getTitle());
        return withTargetDeptName(announcement);
    }

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

    public Announcement withdraw(Long id) {
        requireMaintainer(AuthContext.requireUser());
        Announcement announcement = require(id);
        announcement.setStatus("withdrawn");
        announcement.setUpdatedAt(LocalDateTime.now());
        repo.saveAnnouncement(announcement);
        workflowService.audit("announcement", "withdraw", "announcement", announcement.getId(), announcement.getTitle());
        return withTargetDeptName(announcement);
    }

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

    private Announcement require(Long id) {
        Announcement announcement = repo.findAnnouncementById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        return announcement;
    }

    private boolean canRead(User user, Announcement announcement) {
        if (!"published".equals(announcement.getStatus())) {
            return false;
        }
        return "all".equals(announcement.getTargetType())
                || user.getDeptId().equals(announcement.getTargetDeptId())
                || canMaintain(user);
    }

    private void requireMaintainer(User user) {
        if (!canMaintain(user)) {
            throw new ForbiddenException("无公告维护权限");
        }
    }

    private boolean canMaintain(User user) {
        return user.getRoleKeys().contains("admin") || user.getRoleKeys().contains("office_admin");
    }

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
