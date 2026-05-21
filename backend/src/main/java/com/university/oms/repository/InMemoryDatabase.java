package com.university.oms.repository;

import com.university.oms.model.*;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryDatabase {
    private final AtomicLong ids = new AtomicLong(1000);
    private final Map<Long, User> users = new ConcurrentHashMap<Long, User>();
    private final Map<Long, Department> departments = new ConcurrentHashMap<Long, Department>();
    private final Map<Long, Document> documents = new ConcurrentHashMap<Long, Document>();
    private final Map<Long, Seal> seals = new ConcurrentHashMap<Long, Seal>();
    private final Map<Long, SealApplication> sealApplications = new ConcurrentHashMap<Long, SealApplication>();
    private final Map<Long, MeetingRoom> rooms = new ConcurrentHashMap<Long, MeetingRoom>();
    private final Map<Long, Meeting> meetings = new ConcurrentHashMap<Long, Meeting>();
    private final Map<Long, Travel> travels = new ConcurrentHashMap<Long, Travel>();
    private final Map<Long, Report> reports = new ConcurrentHashMap<Long, Report>();
    private final List<ApprovalRecord> approvals = Collections.synchronizedList(new ArrayList<ApprovalRecord>());
    private final List<Attachment> attachments = Collections.synchronizedList(new ArrayList<Attachment>());
    private final List<AuditLog> auditLogs = Collections.synchronizedList(new ArrayList<AuditLog>());
    private final List<Notification> notifications = Collections.synchronizedList(new ArrayList<Notification>());
    private final Map<String, FlowInstance> flowInstances = new ConcurrentHashMap<String, FlowInstance>();
    private final List<FlowTask> flowTasks = Collections.synchronizedList(new ArrayList<FlowTask>());
    private final Map<String, BigDecimal> meetingFeeStandards = new ConcurrentHashMap<String, BigDecimal>();

    public long nextId() {
        return ids.incrementAndGet();
    }

    public void ensureNextIdAtLeast(long id) {
        while (ids.get() < id) {
            ids.compareAndSet(ids.get(), id);
        }
    }

    @PostConstruct
    public void init() {
        addDept(1L, "党委办公室校长办公室", 0L);
        addDept(2L, "财务部", 0L);
        addDept(3L, "保卫部", 0L);
        addDept(4L, "信息科学技术学院", 0L);

        addUser(1L, "admin", "系统管理员", 1L, "admin");
        addUser(2L, "user", "普通办公人员", 4L, "office_user");
        addUser(3L, "head", "部门负责人", 4L, "dept_head");
        addUser(4L, "leader", "校级领导", 1L, "school_leader");
        addUser(5L, "office", "党办校办人员", 1L, "office_admin");
        addUser(6L, "finance", "财务人员", 2L, "finance_staff");
        addUser(7L, "security", "保卫人员", 3L, "security_staff");
        addUser(8L, "keeper", "印章保管人", 1L, "seal_keeper");

        addSeal(1L, "北京大学行政印章", "行政印章", 1L, 8L);
        addSeal(2L, "信息科学技术学院公章", "部门印章", 4L, 8L);

        addRoom(1L, "理科一号楼 101", 80, "投影仪,白板", "理科一号楼");
        addRoom(2L, "英杰交流中心阳光厅", 520, "视频会议,音响,投影仪", "英杰交流中心");
        addRoom(3L, "二教 201", 120, "投影仪", "第二教学楼");
        addRoom(4L, "百周年纪念讲堂", 2000, "视频会议,音响,投影仪,灯光", "百周年纪念讲堂");

        meetingFeeStandards.put("国内管理会议", new BigDecimal("550"));
        meetingFeeStandards.put("国内业务会议", new BigDecimal("950"));
        meetingFeeStandards.put("在华举办的国际会议", new BigDecimal("1200"));
    }

    private void addDept(Long id, String name, Long parentId) {
        Department dept = new Department();
        fill(dept, id);
        dept.setDeptName(name);
        dept.setParentId(parentId);
        departments.put(id, dept);
    }

    private void addUser(Long id, String username, String realName, Long deptId, String role) {
        User user = new User();
        fill(user, id);
        user.setUsername(username);
        user.setPassword("123456");
        user.setRealName(realName);
        user.setDeptId(deptId);
        user.setDeptName(departments.get(deptId).getDeptName());
        user.getRoleKeys().add(role);
        users.put(id, user);
    }

    private void addSeal(Long id, String name, String type, Long deptId, Long keeperId) {
        Seal seal = new Seal();
        fill(seal, id);
        seal.setSealName(name);
        seal.setSealType(type);
        seal.setDeptId(deptId);
        seal.setKeeperId(keeperId);
        seal.setStatus("in_store");
        seals.put(id, seal);
    }

    private void addRoom(Long id, String name, int capacity, String equipment, String location) {
        MeetingRoom room = new MeetingRoom();
        fill(room, id);
        room.setRoomName(name);
        room.setCapacity(capacity);
        room.setEquipment(equipment);
        room.setLocation(location);
        room.setEnabled(true);
        rooms.put(id, room);
    }

    public void fill(BaseEntity entity, Long id) {
        entity.setId(id);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
    }

    public Map<Long, User> users() { return users; }
    public Map<Long, Department> departments() { return departments; }
    public Map<Long, Document> documents() { return documents; }
    public Map<Long, Seal> seals() { return seals; }
    public Map<Long, SealApplication> sealApplications() { return sealApplications; }
    public Map<Long, MeetingRoom> rooms() { return rooms; }
    public Map<Long, Meeting> meetings() { return meetings; }
    public Map<Long, Travel> travels() { return travels; }
    public Map<Long, Report> reports() { return reports; }
    public List<ApprovalRecord> approvals() { return approvals; }
    public List<Attachment> attachments() { return attachments; }
    public List<AuditLog> auditLogs() { return auditLogs; }
    public List<Notification> notifications() { return notifications; }
    public Map<String, FlowInstance> flowInstances() { return flowInstances; }
    public List<FlowTask> flowTasks() { return flowTasks; }
    public Map<String, BigDecimal> meetingFeeStandards() { return meetingFeeStandards; }
}
