USE office_management_system;

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

INSERT INTO sys_user (id, username, password, real_name, dept_id) VALUES
(1, 'admin', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '系统管理员', 1),
(2, 'user', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '普通办公人员', 4),
(3, 'head', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '部门负责人', 4),
(4, 'leader', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '校级领导', 1),
(5, 'office', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '党办校办人员', 1),
(6, 'finance', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '财务人员', 2),
(7, 'security', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '保卫人员', 3),
(8, 'keeper', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '印章保管人', 1)
ON DUPLICATE KEY UPDATE password = VALUES(password), real_name = VALUES(real_name), dept_id = VALUES(dept_id);

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
('三类', '其他业务', '普通地区', 500, 100, 80)
ON DUPLICATE KEY UPDATE hotel_limit = VALUES(hotel_limit), meal_subsidy = VALUES(meal_subsidy), local_transport_subsidy = VALUES(local_transport_subsidy);

INSERT INTO oa_meeting_fee_standard (meeting_type, accommodation_limit, meal_limit, other_limit, total_limit) VALUES
('国内业务会议', 600, 200, 150, 950),
('国内管理会议', 340, 130, 80, 550),
('在华举办的国际会议', 700, 200, 300, 1200)
ON DUPLICATE KEY UPDATE accommodation_limit = VALUES(accommodation_limit), meal_limit = VALUES(meal_limit), other_limit = VALUES(other_limit), total_limit = VALUES(total_limit);
