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
    private final Map<Long, DocumentDistribution> documentDistributions = new ConcurrentHashMap<Long, DocumentDistribution>();
    private final Map<Long, Seal> seals = new ConcurrentHashMap<Long, Seal>();
    private final Map<Long, SealApplication> sealApplications = new ConcurrentHashMap<Long, SealApplication>();
    private final Map<Long, SealTransfer> sealTransfers = new ConcurrentHashMap<Long, SealTransfer>();
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
    private final Map<String, DictionaryType> dictionaryTypes = new ConcurrentHashMap<String, DictionaryType>();
    private final Map<String, DictionaryItem> dictionaryItems = new ConcurrentHashMap<String, DictionaryItem>();

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
        seedDictionaries();
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
        meetingFeeStandards.put("国内管理会议（讲席教授3人及以上）", new BigDecimal("600"));
        meetingFeeStandards.put("国内业务会议", new BigDecimal("950"));
        meetingFeeStandards.put("在华举办的国际会议", new BigDecimal("1200"));
    }

    private void seedDictionaries() {
        addDictType(101L, "business_status", "业务状态", true);
        addDictType(102L, "distribution_status", "分发状态", true);
        addDictType(103L, "biz_type", "业务类型", true);
        addDictType(104L, "flow_node", "流程节点", true);
        addDictType(105L, "role_key", "角色", true);
        addDictType(106L, "secrecy_level", "保密等级", true);
        addDictType(107L, "document_type", "公文类型", false);
        addDictType(108L, "matter_level", "事项级别", false);
        addDictType(109L, "seal_type", "印章类型", false);
        addDictType(110L, "seal_status", "印章状态", false);
        addDictType(111L, "meeting_type", "会议类型", false);
        addDictType(112L, "venue_type", "场地类型", false);
        addDictType(113L, "report_type", "请示报告类型", false);
        addDictType(114L, "staff_level", "人员等级", false);
        addDictType(115L, "travel_type", "出差类型", false);
        addDictType(116L, "transport_type", "交通工具", false);

        addDictItem(201L, "business_status", "draft", "草稿", 10, true);
        addDictItem(202L, "business_status", "pending_dept", "部门负责人审批中", 20, true);
        addDictItem(203L, "business_status", "pending_office", "党办校办审核中", 30, true);
        addDictItem(204L, "business_status", "pending_leader", "校级领导审批中", 40, true);
        addDictItem(205L, "business_status", "pending_security", "保卫部门审核中", 50, true);
        addDictItem(206L, "business_status", "pending_finance", "财务审核中", 60, true);
        addDictItem(207L, "business_status", "pending_secret_review", "保密审查中", 70, true);
        addDictItem(208L, "business_status", "approved", "审批通过", 80, true);
        addDictItem(209L, "business_status", "archived", "已归档", 90, true);
        addDictItem(210L, "business_status", "rejected", "已退回", 100, true);
        addDictItem(211L, "business_status", "used", "已用印", 110, true);
        addDictItem(212L, "business_status", "returned", "已归还", 120, true);
        addDictItem(213L, "business_status", "running", "办理中", 130, true);
        addDictItem(214L, "business_status", "completed", "已完成", 140, true);
        addDictItem(215L, "business_status", "pending", "待办理", 150, true);
        addDictItem(216L, "distribution_status", "not_distributed", "未分发", 10, true);
        addDictItem(217L, "distribution_status", "distributed", "待签收", 20, true);
        addDictItem(218L, "distribution_status", "partially_received", "部分签收", 30, true);
        addDictItem(219L, "distribution_status", "received", "已签收", 40, true);
        addDictItem(220L, "biz_type", "document", "公文", 10, true);
        addDictItem(221L, "biz_type", "seal", "用印", 20, true);
        addDictItem(222L, "biz_type", "meeting", "会议", 30, true);
        addDictItem(223L, "biz_type", "travel", "差旅", 40, true);
        addDictItem(224L, "biz_type", "report", "请示报告", 50, true);
        addDictItem(225L, "flow_node", "pending_dept", "部门负责人审批", 10, true);
        addDictItem(226L, "flow_node", "pending_office", "党办校办审核", 20, true);
        addDictItem(227L, "flow_node", "pending_leader", "校级领导审批", 30, true);
        addDictItem(228L, "flow_node", "pending_security", "保卫部门审核", 40, true);
        addDictItem(229L, "flow_node", "pending_finance", "财务审核", 50, true);
        addDictItem(230L, "flow_node", "pending_secret_review", "保密审查", 60, true);
        addDictItem(231L, "flow_node", "approved", "审批完成", 70, true);
        addDictItem(232L, "flow_node", "archived", "归档完成", 80, true);
        addDictItem(233L, "role_key", "admin", "系统管理员", 10, true);
        addDictItem(234L, "role_key", "office_user", "普通办公人员", 20, true);
        addDictItem(235L, "role_key", "dept_head", "部门负责人", 30, true);
        addDictItem(236L, "role_key", "school_leader", "校级领导", 40, true);
        addDictItem(237L, "role_key", "office_admin", "党办校办人员", 50, true);
        addDictItem(238L, "role_key", "finance_staff", "财务人员", 60, true);
        addDictItem(239L, "role_key", "security_staff", "保卫人员", 70, true);
        addDictItem(240L, "role_key", "seal_keeper", "印章保管人", 80, true);
        addDictItem(241L, "secrecy_level", "公开", "公开", 10, true);
        addDictItem(242L, "secrecy_level", "内部", "内部", 20, true);
        addDictItem(243L, "secrecy_level", "秘密", "秘密", 30, true);
        addDictItem(244L, "secrecy_level", "机密", "机密", 40, true);
        addDictItem(245L, "secrecy_level", "绝密", "绝密", 50, true);
        addDictItem(246L, "document_type", "通知", "通知", 10, false);
        addDictItem(247L, "document_type", "决定", "决定", 20, false);
        addDictItem(248L, "document_type", "请示", "请示", 30, false);
        addDictItem(249L, "document_type", "批复", "批复", 40, false);
        addDictItem(250L, "document_type", "报告", "报告", 50, false);
        addDictItem(251L, "document_type", "函", "函", 60, false);
        addDictItem(252L, "document_type", "公告", "公告", 70, false);
        addDictItem(253L, "matter_level", "常规事项", "常规事项", 10, false);
        addDictItem(254L, "matter_level", "一般事项", "一般事项", 20, false);
        addDictItem(255L, "matter_level", "重大事项", "重大事项", 30, false);
        addDictItem(256L, "seal_type", "行政印章", "行政印章", 10, false);
        addDictItem(257L, "seal_type", "部门印章", "部门印章", 20, false);
        addDictItem(258L, "seal_type", "专用章", "专用章", 30, false);
        addDictItem(259L, "seal_type", "名章", "名章", 40, false);
        addDictItem(260L, "seal_status", "in_store", "在库", 10, false);
        addDictItem(261L, "seal_status", "in_use", "使用中", 20, false);
        addDictItem(262L, "seal_status", "lent", "外带中", 30, false);
        addDictItem(263L, "seal_status", "retired", "已停用", 40, false);
        addDictItem(264L, "meeting_type", "国内管理会议", "国内管理会议", 10, false);
        addDictItem(265L, "meeting_type", "国内管理会议（讲席教授3人及以上）", "国内管理会议（讲席教授3人及以上）", 20, false);
        addDictItem(266L, "meeting_type", "国内业务会议", "国内业务会议", 30, false);
        addDictItem(267L, "meeting_type", "在华举办的国际会议", "在华举办的国际会议", 40, false);
        addDictItem(268L, "venue_type", "室内", "室内", 10, false);
        addDictItem(269L, "venue_type", "室外", "室外", 20, false);
        addDictItem(270L, "report_type", "请示", "请示", 10, false);
        addDictItem(271L, "report_type", "报告", "报告", 20, false);
        addDictItem(272L, "staff_level", "一类", "一类人员", 10, false);
        addDictItem(273L, "staff_level", "二类", "二类人员", 20, false);
        addDictItem(274L, "staff_level", "三类", "三类人员", 30, false);
        addDictItem(275L, "travel_type", "教学科研业务", "教学科研业务", 10, false);
        addDictItem(276L, "travel_type", "行政管理业务", "行政管理业务", 20, false);
        addDictItem(277L, "travel_type", "学术交流", "学术交流", 30, false);
        addDictItem(278L, "travel_type", "其他业务", "其他业务", 40, false);
        addDictItem(279L, "transport_type", "飞机头等舱", "飞机头等舱", 10, false);
        addDictItem(280L, "transport_type", "飞机公务舱", "飞机公务舱", 20, false);
        addDictItem(281L, "transport_type", "飞机经济舱", "飞机经济舱", 30, false);
        addDictItem(282L, "transport_type", "飞机", "飞机", 40, false);
        addDictItem(283L, "transport_type", "高铁商务座", "高铁商务座", 50, false);
        addDictItem(284L, "transport_type", "高铁一等座", "高铁一等座", 60, false);
        addDictItem(285L, "transport_type", "高铁二等座", "高铁二等座", 70, false);
        addDictItem(286L, "transport_type", "火车软卧", "火车软卧", 80, false);
        addDictItem(287L, "transport_type", "火车硬卧", "火车硬卧", 90, false);
        addDictItem(288L, "transport_type", "火车硬座", "火车硬座", 100, false);
    }

    private void addDictType(Long id, String code, String name, boolean systemType) {
        DictionaryType type = new DictionaryType();
        fill(type, id);
        type.setDictType(code);
        type.setDictName(name);
        type.setSystemType(systemType);
        type.setEnabled(true);
        dictionaryTypes.put(code, type);
    }

    private void addDictItem(Long id, String type, String code, String label, int order, boolean systemItem) {
        DictionaryItem item = new DictionaryItem();
        fill(item, id);
        item.setDictType(type);
        item.setDictCode(code);
        item.setDictLabel(label);
        item.setSortOrder(order);
        item.setEnabled(true);
        item.setSystemItem(systemItem);
        dictionaryItems.put(dictionaryItemKey(type, code), item);
    }

    public String dictionaryItemKey(String type, String code) {
        return type.length() + ":" + type + code;
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
    public Map<Long, DocumentDistribution> documentDistributions() { return documentDistributions; }
    public Map<Long, Seal> seals() { return seals; }
    public Map<Long, SealApplication> sealApplications() { return sealApplications; }
    public Map<Long, SealTransfer> sealTransfers() { return sealTransfers; }
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
    public Map<String, DictionaryType> dictionaryTypes() { return dictionaryTypes; }
    public Map<String, DictionaryItem> dictionaryItems() { return dictionaryItems; }
}
