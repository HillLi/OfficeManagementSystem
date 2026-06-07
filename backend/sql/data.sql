USE office_management_system;

INSERT IGNORE INTO sys_dict_type (id, dict_type, dict_name, system_type, enabled) VALUES
(101, 'business_status', '业务状态', 1, 1),
(102, 'distribution_status', '分发状态', 1, 1),
(103, 'biz_type', '业务类型', 1, 1),
(104, 'flow_node', '流程节点', 1, 1),
(105, 'role_key', '角色', 1, 1),
(106, 'secrecy_level', '保密等级', 1, 1),
(107, 'document_type', '公文类型', 0, 1),
(108, 'matter_level', '事项级别', 0, 1),
(109, 'seal_type', '印章类型', 0, 1),
(110, 'seal_status', '印章状态', 0, 1),
(111, 'meeting_type', '会议类型', 0, 1),
(112, 'venue_type', '场地类型', 0, 1),
(113, 'report_type', '请示报告类型', 0, 1),
(114, 'staff_level', '人员等级', 0, 1),
(115, 'travel_type', '出差类型', 0, 1),
(116, 'transport_type', '交通工具', 0, 1);

INSERT IGNORE INTO sys_dict_item
  (id, dict_type, dict_code, dict_label, sort_order, enabled, system_item) VALUES
(201, 'business_status', 'draft', '草稿', 10, 1, 1),
(202, 'business_status', 'pending_dept', '部门负责人审批中', 20, 1, 1),
(203, 'business_status', 'pending_office', '党办校办审核中', 30, 1, 1),
(204, 'business_status', 'pending_leader', '校级领导审批中', 40, 1, 1),
(205, 'business_status', 'pending_security', '保卫部门审核中', 50, 1, 1),
(206, 'business_status', 'pending_finance', '财务审核中', 60, 1, 1),
(207, 'business_status', 'pending_secret_review', '保密审查中', 70, 1, 1),
(208, 'business_status', 'approved', '审批通过', 80, 1, 1),
(209, 'business_status', 'archived', '已归档', 90, 1, 1),
(210, 'business_status', 'rejected', '已退回', 100, 1, 1),
(211, 'business_status', 'used', '已用印', 110, 1, 1),
(212, 'business_status', 'returned', '已归还', 120, 1, 1),
(213, 'business_status', 'running', '办理中', 130, 1, 1),
(214, 'business_status', 'completed', '已完成', 140, 1, 1),
(215, 'business_status', 'pending', '待办理', 150, 1, 1),
(289, 'business_status', 'minutes_pending', '纪要待确认', 156, 1, 1),
(290, 'business_status', 'minutes_confirmed', '纪要已确认', 157, 1, 1),
(216, 'distribution_status', 'not_distributed', '未分发', 10, 1, 1),
(217, 'distribution_status', 'distributed', '待签收', 20, 1, 1),
(218, 'distribution_status', 'partially_received', '部分签收', 30, 1, 1),
(219, 'distribution_status', 'received', '已签收', 40, 1, 1),
(220, 'biz_type', 'document', '公文', 10, 1, 1),
(221, 'biz_type', 'seal', '用印', 20, 1, 1),
(222, 'biz_type', 'meeting', '会议', 30, 1, 1),
(223, 'biz_type', 'travel', '差旅', 40, 1, 1),
(224, 'biz_type', 'report', '请示报告', 50, 1, 1),
(225, 'flow_node', 'pending_dept', '部门负责人审批', 10, 1, 1),
(226, 'flow_node', 'pending_office', '党办校办审核', 20, 1, 1),
(227, 'flow_node', 'pending_leader', '校级领导审批', 30, 1, 1),
(228, 'flow_node', 'pending_security', '保卫部门审核', 40, 1, 1),
(229, 'flow_node', 'pending_finance', '财务审核', 50, 1, 1),
(230, 'flow_node', 'pending_secret_review', '保密审查', 60, 1, 1),
(231, 'flow_node', 'approved', '审批完成', 70, 1, 1),
(232, 'flow_node', 'archived', '归档完成', 80, 1, 1),
(233, 'role_key', 'admin', '系统管理员', 10, 1, 1),
(234, 'role_key', 'office_user', '普通办公人员', 20, 1, 1),
(235, 'role_key', 'dept_head', '部门负责人', 30, 1, 1),
(236, 'role_key', 'school_leader', '校级领导', 40, 1, 1),
(237, 'role_key', 'office_admin', '党办校办人员', 50, 1, 1),
(238, 'role_key', 'finance_staff', '财务人员', 60, 1, 1),
(239, 'role_key', 'security_staff', '保卫人员', 70, 1, 1),
(240, 'role_key', 'seal_keeper', '印章保管人', 80, 1, 1),
(241, 'secrecy_level', '公开', '公开', 10, 1, 1),
(242, 'secrecy_level', '内部', '内部', 20, 1, 1),
(243, 'secrecy_level', '秘密', '秘密', 30, 1, 1),
(244, 'secrecy_level', '机密', '机密', 40, 1, 1),
(245, 'secrecy_level', '绝密', '绝密', 50, 1, 1),
(246, 'document_type', '通知', '通知', 10, 1, 0),
(247, 'document_type', '决定', '决定', 20, 1, 0),
(248, 'document_type', '请示', '请示', 30, 1, 0),
(249, 'document_type', '批复', '批复', 40, 1, 0),
(250, 'document_type', '报告', '报告', 50, 1, 0),
(251, 'document_type', '函', '函', 60, 1, 0),
(252, 'document_type', '公告', '公告', 70, 1, 0),
(253, 'matter_level', '常规事项', '常规事项', 10, 1, 0),
(254, 'matter_level', '一般事项', '一般事项', 20, 1, 0),
(255, 'matter_level', '重大事项', '重大事项', 30, 1, 0),
(256, 'seal_type', '行政印章', '行政印章', 10, 1, 0),
(257, 'seal_type', '部门印章', '部门印章', 20, 1, 0),
(258, 'seal_type', '专用章', '专用章', 30, 1, 0),
(259, 'seal_type', '名章', '名章', 40, 1, 0),
(260, 'seal_status', 'in_store', '在库', 10, 1, 0),
(261, 'seal_status', 'in_use', '使用中', 20, 1, 0),
(262, 'seal_status', 'lent', '外带中', 30, 1, 0),
(263, 'seal_status', 'retired', '已停用', 40, 1, 0),
(264, 'meeting_type', '国内管理会议', '国内管理会议', 10, 1, 0),
(265, 'meeting_type', '国内管理会议（讲席教授3人及以上）', '国内管理会议（讲席教授3人及以上）', 20, 1, 0),
(266, 'meeting_type', '国内业务会议', '国内业务会议', 30, 1, 0),
(267, 'meeting_type', '在华举办的国际会议', '在华举办的国际会议', 40, 1, 0),
(268, 'venue_type', '室内', '室内', 10, 1, 0),
(269, 'venue_type', '室外', '室外', 20, 1, 0),
(270, 'report_type', '请示', '请示', 10, 1, 0),
(271, 'report_type', '报告', '报告', 20, 1, 0),
(272, 'staff_level', '一类', '一类人员', 10, 1, 0),
(273, 'staff_level', '二类', '二类人员', 20, 1, 0),
(274, 'staff_level', '三类', '三类人员', 30, 1, 0),
(275, 'travel_type', '教学科研业务', '教学科研业务', 10, 1, 0),
(276, 'travel_type', '行政管理业务', '行政管理业务', 20, 1, 0),
(277, 'travel_type', '学术交流', '学术交流', 30, 1, 0),
(278, 'travel_type', '其他业务', '其他业务', 40, 1, 0),
(279, 'transport_type', '飞机头等舱', '飞机头等舱', 10, 1, 0),
(280, 'transport_type', '飞机公务舱', '飞机公务舱', 20, 1, 0),
(281, 'transport_type', '飞机经济舱', '飞机经济舱', 30, 1, 0),
(282, 'transport_type', '飞机', '飞机', 40, 1, 0),
(283, 'transport_type', '高铁商务座', '高铁商务座', 50, 1, 0),
(284, 'transport_type', '高铁一等座', '高铁一等座', 60, 1, 0),
(285, 'transport_type', '高铁二等座', '高铁二等座', 70, 1, 0),
(286, 'transport_type', '火车软卧', '火车软卧', 80, 1, 0),
(287, 'transport_type', '火车硬卧', '火车硬卧', 90, 1, 0),
(288, 'transport_type', '火车硬座', '火车硬座', 100, 1, 0);

INSERT INTO sys_dept (id, dept_name, parent_id) VALUES
(1, '党委办公室校长办公室', 0),
(2, '财务部', 0),
(3, '保卫部', 0),
(4, '信息科学技术学院', 0)
ON DUPLICATE KEY UPDATE dept_name = VALUES(dept_name), parent_id = VALUES(parent_id);

INSERT INTO sys_role (id, role_name, role_key, level, description) VALUES
(1, '系统管理员', 'admin', 100, '系统配置和基础数据维护'),
(2, '普通办公人员', 'office_user', 10, '发起日常办公流程'),
(3, '部门负责人', 'dept_head', 30, '审批本部门流程'),
(4, '校级领导', 'school_leader', 80, '审批校级重大事项'),
(5, '党办校办人员', 'office_admin', 60, '公文归档和校级印章审核'),
(6, '财务人员', 'finance_staff', 50, '费用预算和报销审核'),
(7, '保卫人员', 'security_staff', 50, '大型活动安全审核'),
(8, '印章保管人', 'seal_keeper', 40, '用印登记和归还确认')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), description = VALUES(description);

INSERT INTO sys_user (id, username, password, real_name, dept_id, email) VALUES
(1, 'admin', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '系统管理员', 1, 'admin@example.com'),
(2, 'user', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '普通办公人员', 4, 'user@example.com'),
(3, 'head', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '部门负责人', 4, 'head@example.com'),
(4, 'leader', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '校级领导', 1, 'leader@example.com'),
(5, 'office', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '党办校办人员', 1, 'office@example.com'),
(6, 'finance', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '财务人员', 2, 'finance@example.com'),
(7, 'security', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '保卫人员', 3, 'security@example.com'),
(8, 'keeper', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '印章保管人', 1, 'keeper@example.com')
ON DUPLICATE KEY UPDATE password = VALUES(password), real_name = VALUES(real_name), dept_id = VALUES(dept_id), email = VALUES(email);

INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6), (7, 7), (8, 8);

INSERT INTO oa_seal (id, seal_name, seal_type, dept_id, keeper_id, status) VALUES
(1, '北京大学行政印章', '行政印章', 1, 8, 'in_store'),
(2, '信息科学技术学院公章', '部门印章', 4, 8, 'in_store')
ON DUPLICATE KEY UPDATE seal_name = VALUES(seal_name), status = VALUES(status);

INSERT INTO oa_meeting_room (id, room_name, capacity, equipment, location, status) VALUES
(1, '理科一号楼 101', 80, '投影仪,白板', '理科一号楼', 1),
(2, '英杰交流中心阳光厅', 520, '视频会议,音响,投影仪', '英杰交流中心', 1),
(3, '二教 201', 120, '投影仪', '第二教学楼', 1),
(4, '百周年纪念讲堂', 2000, '视频会议,音响,投影仪,灯光', '百周年纪念讲堂', 1)
ON DUPLICATE KEY UPDATE room_name = VALUES(room_name), capacity = VALUES(capacity), equipment = VALUES(equipment);

INSERT INTO oa_travel_standard (staff_level, travel_type, city_level, hotel_limit, meal_subsidy, local_transport_subsidy) VALUES
('一类', '教学科研业务', '普通地区', 1440, 100, 80),
('二类', '教学科研业务', '普通地区', 1170, 100, 80),
('三类', '教学科研业务', '普通地区', 900, 100, 80),
('一类', '教学科研业务', '特殊地区', 1440, 120, 80),
('二类', '教学科研业务', '特殊地区', 1170, 120, 80),
('三类', '教学科研业务', '特殊地区', 900, 120, 80),
('一类', '其他业务', '普通地区', 800, 100, 80),
('二类', '其他业务', '普通地区', 650, 100, 80),
('三类', '其他业务', '普通地区', 500, 100, 80),
('一类', '其他业务', '特殊地区', 800, 120, 80),
('二类', '其他业务', '特殊地区', 650, 120, 80),
('三类', '其他业务', '特殊地区', 500, 120, 80)
ON DUPLICATE KEY UPDATE hotel_limit = VALUES(hotel_limit), meal_subsidy = VALUES(meal_subsidy), local_transport_subsidy = VALUES(local_transport_subsidy);

INSERT INTO oa_meeting_fee_standard (meeting_type, accommodation_limit, meal_limit, other_limit, total_limit) VALUES
('国内业务会议', 600, 200, 150, 950),
('国内管理会议', 340, 130, 80, 550),
('国内管理会议（讲席教授3人及以上）', 390, 130, 80, 600),
('在华举办的国际会议', 700, 200, 300, 1200)
ON DUPLICATE KEY UPDATE accommodation_limit = VALUES(accommodation_limit), meal_limit = VALUES(meal_limit), other_limit = VALUES(other_limit), total_limit = VALUES(total_limit);
