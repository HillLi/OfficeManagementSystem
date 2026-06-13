/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80039
 Source Host           : localhost:3306
 Source Schema         : office_management_system

 Target Server Type    : MySQL
 Target Server Version : 80039
 File Encoding         : 65001

 Date: 09/06/2026 07:51:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oa_approval_history
-- ----------------------------
DROP TABLE IF EXISTS `oa_approval_history`;
CREATE TABLE `oa_approval_history`  (
  `id` bigint(0) NOT NULL,
  `biz_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `biz_id` bigint(0) NOT NULL,
  `operator_id` bigint(0) NOT NULL,
  `action` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `opinion` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `operated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_approval_history
-- ----------------------------
INSERT INTO `oa_approval_history` VALUES (1002, 'document', 1001, 2, 'create', '起草公文', '2026-05-21 00:38:37');
INSERT INTO `oa_approval_history` VALUES (1004, 'document', 1003, 2, 'create', '起草公文', '2026-05-25 02:23:35');
INSERT INTO `oa_approval_history` VALUES (1009, 'meeting', 1008, 2, 'submit', '提交会议申请', '2026-05-25 07:42:01');
INSERT INTO `oa_approval_history` VALUES (1015, 'document', 1014, 2, 'create', '起草公文', '2026-05-25 07:42:01');
INSERT INTO `oa_approval_history` VALUES (1017, 'document', 1014, 2, 'submit', '提交审批', '2026-05-25 07:42:01');
INSERT INTO `oa_approval_history` VALUES (1022, 'document', 1014, 3, 'approve', 'ok', '2026-05-25 07:42:01');
INSERT INTO `oa_approval_history` VALUES (1026, 'document', 1014, 5, 'approve', 'ok', '2026-05-25 07:42:01');
INSERT INTO `oa_approval_history` VALUES (1030, 'document', 1014, 4, 'approve', 'ok', '2026-05-25 07:42:01');
INSERT INTO `oa_approval_history` VALUES (1040, 'travel', 1039, 2, 'submit', '提交差旅申请', '2026-05-25 07:42:01');
INSERT INTO `oa_approval_history` VALUES (1045, 'travel', 1039, 3, 'approve', 'ok', '2026-05-25 07:42:01');
INSERT INTO `oa_approval_history` VALUES (1049, 'travel', 1039, 6, 'approve', 'ok', '2026-05-25 07:42:01');
INSERT INTO `oa_approval_history` VALUES (1054, 'travel', 1039, 2, 'reimburse', '提交报销', '2026-05-25 07:42:01');
INSERT INTO `oa_approval_history` VALUES (1059, 'travel', 1058, 2, 'submit', '提交差旅申请', '2026-05-25 08:06:19');
INSERT INTO `oa_approval_history` VALUES (1064, 'travel', 1058, 3, 'approve', 'UI QA approval', '2026-05-25 08:06:19');
INSERT INTO `oa_approval_history` VALUES (1068, 'travel', 1058, 6, 'approve', 'UI QA approval', '2026-05-25 08:06:19');
INSERT INTO `oa_approval_history` VALUES (1075, 'seal', 1071, 2, 'submit', '提交用印申请', '2026-05-26 01:04:52');

-- ----------------------------
-- Table structure for oa_document
-- ----------------------------
DROP TABLE IF EXISTS `oa_document`;
CREATE TABLE `oa_document`  (
  `id` bigint(0) NOT NULL,
  `doc_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `doc_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `urgency` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '普通',
  `secrecy_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '公开',
  `knowledge_scope` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `applicant_id` bigint(0) NOT NULL,
  `dept_id` bigint(0) NOT NULL,
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `ai_review_result` json NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `version` int(0) NULL DEFAULT 1,
  `distribution_status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'not_distributed',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_document
-- ----------------------------
INSERT INTO `oa_document` VALUES (1003, '校发〔2026〕1003号', 'mysql-persistence-smoke', 'notice', '普通', 'public', 'all', 'persistence-smoke-body', 2, 4, 'draft', NULL, '2026-05-25 02:23:35', '2026-05-25 02:23:35', 1, 'not_distributed');
INSERT INTO `oa_document` VALUES (1014, '校发〔2026〕1014号', 'mysql-distribution-smoke', 'notice', '普通', 'public', '全校', 'public persistence workflow content', 2, 4, 'approved', NULL, '2026-05-25 07:42:01', '2026-05-25 07:42:01', 1, 'received');

-- ----------------------------
-- Table structure for oa_document_distribution
-- ----------------------------
DROP TABLE IF EXISTS `oa_document_distribution`;
CREATE TABLE `oa_document_distribution`  (
  `id` bigint(0) NOT NULL,
  `document_id` bigint(0) NOT NULL,
  `receiver_id` bigint(0) NOT NULL,
  `receiver_dept_id` bigint(0) NOT NULL,
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `distributed_at` datetime(0) NULL DEFAULT NULL,
  `received_at` datetime(0) NULL DEFAULT NULL,
  `reminded_at` datetime(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_document_distribution_doc`(`document_id`) USING BTREE,
  INDEX `idx_document_distribution_receiver`(`receiver_id`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_document_distribution
-- ----------------------------
INSERT INTO `oa_document_distribution` VALUES (1033, 1014, 2, 4, 'received', '2026-05-25 07:42:01', '2026-05-25 07:42:01', NULL, '2026-05-25 07:42:01', '2026-05-25 07:42:01');

-- ----------------------------
-- Table structure for oa_flow_instance
-- ----------------------------
DROP TABLE IF EXISTS `oa_flow_instance`;
CREATE TABLE `oa_flow_instance`  (
  `id` bigint(0) NOT NULL,
  `biz_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `biz_id` bigint(0) NOT NULL,
  `current_node_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `starter_id` bigint(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_flow_biz`(`biz_type`, `biz_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_flow_instance
-- ----------------------------
INSERT INTO `oa_flow_instance` VALUES (1010, 'meeting', 1008, 'pending_dept', 'running', 2, '2026-05-25 07:42:01', '2026-05-25 07:42:01');
INSERT INTO `oa_flow_instance` VALUES (1018, 'document', 1014, 'approved', 'approved', 2, '2026-05-25 07:42:01', '2026-05-25 07:42:01');
INSERT INTO `oa_flow_instance` VALUES (1041, 'travel', 1039, 'pending_finance', 'running', 2, '2026-05-25 07:42:01', '2026-05-25 07:42:01');
INSERT INTO `oa_flow_instance` VALUES (1060, 'travel', 1058, 'approved', 'approved', 2, '2026-05-25 08:06:19', '2026-05-25 08:06:19');
INSERT INTO `oa_flow_instance` VALUES (1076, 'seal', 1071, 'pending_dept', 'running', 2, '2026-05-26 01:04:52', '2026-05-26 01:04:52');

-- ----------------------------
-- Table structure for oa_flow_task
-- ----------------------------
DROP TABLE IF EXISTS `oa_flow_task`;
CREATE TABLE `oa_flow_task`  (
  `id` bigint(0) NOT NULL,
  `instance_id` bigint(0) NOT NULL,
  `biz_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `biz_id` bigint(0) NOT NULL,
  `node_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `approver_role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approver_id` bigint(0) NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `due_time` datetime(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_flow_task_biz`(`biz_type`, `biz_id`) USING BTREE,
  INDEX `idx_flow_task_status`(`status`, `approver_role`, `approver_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_flow_task
-- ----------------------------
INSERT INTO `oa_flow_task` VALUES (1011, 1010, 'meeting', 1008, 'pending_dept', 'dept_head', NULL, 'pending', '2026-05-28 07:42:01', '2026-05-25 07:42:01', '2026-05-25 07:42:01');
INSERT INTO `oa_flow_task` VALUES (1019, 1018, 'document', 1014, 'pending_dept', 'dept_head', 3, 'completed', '2026-05-28 07:42:01', '2026-05-25 07:42:01', '2026-05-25 07:42:01');
INSERT INTO `oa_flow_task` VALUES (1023, 1018, 'document', 1014, 'pending_office', 'office_admin', 5, 'completed', '2026-05-28 07:42:01', '2026-05-25 07:42:01', '2026-05-25 07:42:01');
INSERT INTO `oa_flow_task` VALUES (1027, 1018, 'document', 1014, 'pending_leader', 'school_leader', 4, 'completed', '2026-05-28 07:42:01', '2026-05-25 07:42:01', '2026-05-25 07:42:01');
INSERT INTO `oa_flow_task` VALUES (1042, 1041, 'travel', 1039, 'pending_dept', 'dept_head', 3, 'completed', '2026-05-28 07:42:01', '2026-05-25 07:42:01', '2026-05-25 07:42:01');
INSERT INTO `oa_flow_task` VALUES (1046, 1041, 'travel', 1039, 'pending_finance', 'finance_staff', 6, 'completed', '2026-05-28 07:42:01', '2026-05-25 07:42:01', '2026-05-25 07:42:01');
INSERT INTO `oa_flow_task` VALUES (1055, 1041, 'travel', 1039, 'pending_finance', 'finance_staff', NULL, 'pending', '2026-05-28 07:42:01', '2026-05-25 07:42:01', '2026-05-25 07:42:01');
INSERT INTO `oa_flow_task` VALUES (1061, 1060, 'travel', 1058, 'pending_dept', 'dept_head', 3, 'completed', '2026-05-28 08:06:19', '2026-05-25 08:06:19', '2026-05-25 08:06:19');
INSERT INTO `oa_flow_task` VALUES (1065, 1060, 'travel', 1058, 'pending_finance', 'finance_staff', 6, 'completed', '2026-05-28 08:06:19', '2026-05-25 08:06:19', '2026-05-25 08:06:19');
INSERT INTO `oa_flow_task` VALUES (1077, 1076, 'seal', 1071, 'pending_dept', 'dept_head', NULL, 'pending', '2026-05-29 01:04:52', '2026-05-26 01:04:52', '2026-05-26 01:04:52');

-- ----------------------------
-- Table structure for oa_meeting
-- ----------------------------
DROP TABLE IF EXISTS `oa_meeting`;
CREATE TABLE `oa_meeting`  (
  `id` bigint(0) NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `room_id` bigint(0) NOT NULL,
  `start_time` datetime(0) NOT NULL,
  `end_time` datetime(0) NOT NULL,
  `organizer_id` bigint(0) NOT NULL,
  `expected_count` int(0) NULL DEFAULT NULL,
  `venue_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `meeting_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `budget` decimal(10, 2) NULL DEFAULT NULL,
  `risk_report_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `security_plan_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `emergency_plan_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `large_activity` tinyint(0) NULL DEFAULT 0,
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `accommodation_fee` decimal(10, 2) NULL DEFAULT NULL,
  `meal_fee` decimal(10, 2) NULL DEFAULT NULL,
  `venue_fee` decimal(10, 2) NULL DEFAULT NULL,
  `other_fee` decimal(10, 2) NULL DEFAULT NULL,
  `sign_in_count` int(0) NULL DEFAULT 0,
  `minutes` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_meeting
-- ----------------------------
INSERT INTO `oa_meeting` VALUES (1008, 'mysql-fee-smoke-ok', 1, '2026-10-14 09:00:00', '2026-10-14 10:00:00', 2, 8, 'indoor', 'department', 300.00, NULL, NULL, NULL, 0, 'pending_dept', '2026-05-25 07:42:01', NULL, 100.00, 200.00, NULL, 0, NULL);

-- ----------------------------
-- Table structure for oa_meeting_fee_standard
-- ----------------------------
DROP TABLE IF EXISTS `oa_meeting_fee_standard`;
CREATE TABLE `oa_meeting_fee_standard`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `meeting_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `accommodation_limit` decimal(10, 2) NOT NULL,
  `meal_limit` decimal(10, 2) NOT NULL,
  `other_limit` decimal(10, 2) NOT NULL,
  `total_limit` decimal(10, 2) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_meeting_fee_standard
-- ----------------------------
INSERT INTO `oa_meeting_fee_standard` VALUES (1, '国内业务会议', 600.00, 200.00, 150.00, 950.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (2, '国内管理会议', 340.00, 130.00, 80.00, 550.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (3, '在华举办的国际会议', 700.00, 200.00, 300.00, 1200.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (4, '国内业务会议', 600.00, 200.00, 150.00, 950.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (5, '国内管理会议', 340.00, 130.00, 80.00, 550.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (6, '在华举办的国际会议', 700.00, 200.00, 300.00, 1200.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (7, '国内业务会议', 600.00, 200.00, 150.00, 950.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (8, '国内管理会议', 340.00, 130.00, 80.00, 550.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (9, '在华举办的国际会议', 700.00, 200.00, 300.00, 1200.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (10, '国内业务会议', 600.00, 200.00, 150.00, 950.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (11, '国内管理会议', 340.00, 130.00, 80.00, 550.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (12, '在华举办的国际会议', 700.00, 200.00, 300.00, 1200.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (13, '国内业务会议', 600.00, 200.00, 150.00, 950.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (14, '国内管理会议', 340.00, 130.00, 80.00, 550.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (15, '在华举办的国际会议', 700.00, 200.00, 300.00, 1200.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (16, '国内业务会议', 600.00, 200.00, 150.00, 950.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (17, '国内管理会议', 340.00, 130.00, 80.00, 550.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (18, '在华举办的国际会议', 700.00, 200.00, 300.00, 1200.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (19, '国内业务会议', 600.00, 200.00, 150.00, 950.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (20, '国内管理会议', 340.00, 130.00, 80.00, 550.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (21, '在华举办的国际会议', 700.00, 200.00, 300.00, 1200.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (22, '国内业务会议', 600.00, 200.00, 150.00, 950.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (23, '国内管理会议', 340.00, 130.00, 80.00, 550.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (24, '在华举办的国际会议', 700.00, 200.00, 300.00, 1200.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (25, '国内业务会议', 600.00, 200.00, 150.00, 950.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (26, '国内管理会议', 340.00, 130.00, 80.00, 550.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (27, '在华举办的国际会议', 700.00, 200.00, 300.00, 1200.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (28, '国内业务会议', 600.00, 200.00, 150.00, 950.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (29, '国内管理会议', 340.00, 130.00, 80.00, 550.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (30, '在华举办的国际会议', 700.00, 200.00, 300.00, 1200.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (31, '国内业务会议', 600.00, 200.00, 150.00, 950.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (32, '国内管理会议', 340.00, 130.00, 80.00, 550.00);
INSERT INTO `oa_meeting_fee_standard` VALUES (33, '在华举办的国际会议', 700.00, 200.00, 300.00, 1200.00);

-- ----------------------------
-- Table structure for oa_meeting_room
-- ----------------------------
DROP TABLE IF EXISTS `oa_meeting_room`;
CREATE TABLE `oa_meeting_room`  (
  `id` bigint(0) NOT NULL,
  `room_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `capacity` int(0) NOT NULL,
  `equipment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint(0) NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_meeting_room
-- ----------------------------
INSERT INTO `oa_meeting_room` VALUES (1, '理科一号楼 101', 80, '投影仪,白板', '理科一号楼', 1);
INSERT INTO `oa_meeting_room` VALUES (2, '英杰交流中心阳光厅', 520, '视频会议,音响,投影仪', '英杰交流中心', 1);
INSERT INTO `oa_meeting_room` VALUES (3, '二教 201', 120, '投影仪', '第二教学楼', 1);
INSERT INTO `oa_meeting_room` VALUES (4, '百周年纪念讲堂', 2000, '视频会议,音响,投影仪,灯光', '百周年纪念讲堂', 1);

-- ----------------------------
-- Table structure for oa_report
-- ----------------------------
DROP TABLE IF EXISTS `oa_report`;
CREATE TABLE `oa_report`  (
  `id` bigint(0) NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `secrecy_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `applicant_id` bigint(0) NOT NULL,
  `dept_id` bigint(0) NOT NULL,
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `reply` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_report
-- ----------------------------

-- ----------------------------
-- Table structure for oa_seal
-- ----------------------------
DROP TABLE IF EXISTS `oa_seal`;
CREATE TABLE `oa_seal`  (
  `id` bigint(0) NOT NULL,
  `seal_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `seal_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dept_id` bigint(0) NOT NULL,
  `keeper_id` bigint(0) NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_seal
-- ----------------------------
INSERT INTO `oa_seal` VALUES (1, '北京大学行政印章', '行政印章', 1, 8, 'in_store');
INSERT INTO `oa_seal` VALUES (2, '信息科学技术学院公章', '部门印章', 4, 8, 'in_store');

-- ----------------------------
-- Table structure for oa_seal_log
-- ----------------------------
DROP TABLE IF EXISTS `oa_seal_log`;
CREATE TABLE `oa_seal_log`  (
  `id` bigint(0) NOT NULL,
  `seal_id` bigint(0) NOT NULL,
  `applicant_id` bigint(0) NOT NULL,
  `purpose` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `material_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `copies` int(0) NULL DEFAULT 1,
  `take_out` tinyint(0) NULL DEFAULT 0,
  `matter_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `use_time` datetime(0) NULL DEFAULT NULL,
  `return_time` datetime(0) NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `take_out_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `take_out_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `supervisor_id` bigint(0) NULL DEFAULT NULL,
  `return_deadline` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_seal_log
-- ----------------------------
INSERT INTO `oa_seal_log` VALUES (1071, 2, 2, 'MySQL用印材料持久化复测', NULL, 1, 0, '常规事项', NULL, NULL, 'pending_dept', '2026-05-26 01:04:52', NULL, NULL, NULL, NULL);
INSERT INTO `oa_seal_log` VALUES (1080, 2, 2, 'MySQL用印材料持久化复测', NULL, 1, 0, '常规事项', NULL, NULL, 'draft', '2026-05-26 01:04:52', NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for oa_seal_transfer
-- ----------------------------
DROP TABLE IF EXISTS `oa_seal_transfer`;
CREATE TABLE `oa_seal_transfer`  (
  `id` bigint(0) NOT NULL,
  `seal_id` bigint(0) NOT NULL,
  `transferor_id` bigint(0) NOT NULL,
  `receiver_id` bigint(0) NOT NULL,
  `supervisor_id` bigint(0) NOT NULL,
  `material_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `transfer_time` datetime(0) NOT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_seal_transfer_seal`(`seal_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_seal_transfer
-- ----------------------------
INSERT INTO `oa_seal_transfer` VALUES (1037, 2, 8, 5, 8, '/files/mysql-transfer.pdf', 'mysql smoke', '2026-05-25 07:42:01', '2026-05-25 07:42:01', '2026-05-25 07:42:01');

-- ----------------------------
-- Table structure for oa_travel
-- ----------------------------
DROP TABLE IF EXISTS `oa_travel`;
CREATE TABLE `oa_travel`  (
  `id` bigint(0) NOT NULL,
  `applicant_id` bigint(0) NOT NULL,
  `destination` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `staff_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `travel_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `transport` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `budget` decimal(10, 2) NULL DEFAULT NULL,
  `actual_expense` decimal(10, 2) NULL DEFAULT NULL,
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `receipt_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `over_limit_reason` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `reimbursement_submitted` tinyint(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_travel
-- ----------------------------
INSERT INTO `oa_travel` VALUES (1039, 2, 'Shanghai', '2026-11-01', '2026-11-02', 'research', 'level3', 'research', '高铁二等座', 1200.00, 5000.00, 'pending_finance', '2026-05-25 07:42:01', '/receipts/mysql-smoke.pdf', 'approved exceptional fare', 1);
INSERT INTO `oa_travel` VALUES (1058, 2, 'Browser QA', '2026-07-06', '2026-07-07', '界面报销对话框验收', '三类', '教学科研业务', '高铁二等座', 800.00, 0.00, 'approved', '2026-05-25 08:06:19', NULL, NULL, 0);

-- ----------------------------
-- Table structure for oa_travel_standard
-- ----------------------------
DROP TABLE IF EXISTS `oa_travel_standard`;
CREATE TABLE `oa_travel_standard`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `staff_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `travel_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `city_level` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `hotel_limit` decimal(10, 2) NOT NULL,
  `meal_subsidy` decimal(10, 2) NOT NULL,
  `local_transport_subsidy` decimal(10, 2) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_travel_standard
-- ----------------------------
INSERT INTO `oa_travel_standard` VALUES (1, '一类', '教学科研业务', '普通地区', 1440.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (2, '二类', '教学科研业务', '普通地区', 1170.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (3, '三类', '教学科研业务', '普通地区', 900.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (4, '三类', '其他业务', '普通地区', 500.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (5, '一类', '教学科研业务', '普通地区', 1440.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (6, '二类', '教学科研业务', '普通地区', 1170.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (7, '三类', '教学科研业务', '普通地区', 900.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (8, '三类', '其他业务', '普通地区', 500.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (9, '一类', '教学科研业务', '普通地区', 1440.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (10, '二类', '教学科研业务', '普通地区', 1170.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (11, '三类', '教学科研业务', '普通地区', 900.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (12, '三类', '其他业务', '普通地区', 500.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (13, '一类', '教学科研业务', '普通地区', 1440.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (14, '二类', '教学科研业务', '普通地区', 1170.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (15, '三类', '教学科研业务', '普通地区', 900.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (16, '三类', '其他业务', '普通地区', 500.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (17, '一类', '教学科研业务', '普通地区', 1440.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (18, '二类', '教学科研业务', '普通地区', 1170.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (19, '三类', '教学科研业务', '普通地区', 900.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (20, '三类', '其他业务', '普通地区', 500.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (21, '一类', '教学科研业务', '普通地区', 1440.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (22, '二类', '教学科研业务', '普通地区', 1170.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (23, '三类', '教学科研业务', '普通地区', 900.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (24, '三类', '其他业务', '普通地区', 500.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (25, '一类', '教学科研业务', '普通地区', 1440.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (26, '二类', '教学科研业务', '普通地区', 1170.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (27, '三类', '教学科研业务', '普通地区', 900.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (28, '三类', '其他业务', '普通地区', 500.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (29, '一类', '教学科研业务', '普通地区', 1440.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (30, '二类', '教学科研业务', '普通地区', 1170.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (31, '三类', '教学科研业务', '普通地区', 900.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (32, '三类', '其他业务', '普通地区', 500.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (33, '一类', '教学科研业务', '普通地区', 1440.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (34, '二类', '教学科研业务', '普通地区', 1170.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (35, '三类', '教学科研业务', '普通地区', 900.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (36, '三类', '其他业务', '普通地区', 500.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (37, '一类', '教学科研业务', '普通地区', 1440.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (38, '二类', '教学科研业务', '普通地区', 1170.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (39, '三类', '教学科研业务', '普通地区', 900.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (40, '三类', '其他业务', '普通地区', 500.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (41, '一类', '教学科研业务', '普通地区', 1440.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (42, '二类', '教学科研业务', '普通地区', 1170.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (43, '三类', '教学科研业务', '普通地区', 900.00, 100.00, 80.00);
INSERT INTO `oa_travel_standard` VALUES (44, '三类', '其他业务', '普通地区', 500.00, 100.00, 80.00);

-- ----------------------------
-- Table structure for sys_attachment
-- ----------------------------
DROP TABLE IF EXISTS `sys_attachment`;
CREATE TABLE `sys_attachment`  (
  `id` bigint(0) NOT NULL,
  `biz_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `biz_id` bigint(0) NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `secrecy_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '公开',
  `uploader_id` bigint(0) NOT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `storage_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `file_size` bigint(0) NULL DEFAULT NULL,
  `content_type` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `deleted` tinyint(0) NULL DEFAULT 0,
  `deleted_by` bigint(0) NULL DEFAULT NULL,
  `deleted_at` datetime(0) NULL DEFAULT NULL,
  `delete_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_attachment_biz`(`biz_type`, `biz_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_attachment
-- ----------------------------
INSERT INTO `sys_attachment` VALUES (1052, 'travel', 1039, '差旅报销凭证', '/receipts/mysql-smoke.pdf', '公开', 2, '2026-05-25 07:42:01', NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, '2026-05-26 01:03:10');
INSERT INTO `sys_attachment` VALUES (1072, 'seal', 1071, 'oms-seal-material-runtime.pdf', '/api/workflow/attachments/1072/download', '内部', 2, '2026-05-26 01:04:52', 'oms-seal-material-runtime.pdf', 'C:\\Users\\Ava\\.oms\\uploads\\1072.pdf', 21, 'application/pdf', 0, NULL, NULL, NULL, '2026-05-26 01:04:52');
INSERT INTO `sys_attachment` VALUES (1081, 'seal', 1080, 'oms-seal-material-runtime.pdf', '/api/workflow/attachments/1081/download', '内部', 2, '2026-05-26 01:04:52', 'oms-seal-material-runtime.pdf', 'C:\\Users\\Ava\\.oms\\uploads\\1081.pdf', 21, 'application/pdf', 1, 2, '2026-05-26 01:04:52', '运行态逻辑删除验证', '2026-05-26 01:04:52');
INSERT INTO `sys_attachment` VALUES (1085, 'seal', 1080, 'ui-material-final.pdf', '/api/workflow/attachments/1085/download', '内部', 2, '2026-05-26 01:26:06', 'ui-material.pdf', 'C:\\Users\\Ava\\.oms\\uploads\\1085.pdf', 16, 'application/pdf', 1, 2, '2026-05-26 01:27:10', '页面逻辑删除验证', '2026-05-26 01:27:10');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint(0) NOT NULL,
  `dept_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `parent_id` bigint(0) NULL DEFAULT 0,
  `status` tinyint(0) NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (1, '党委办公室校长办公室', 0, 1);
INSERT INTO `sys_dept` VALUES (2, '财务部', 0, 1);
INSERT INTO `sys_dept` VALUES (3, '保卫部', 0, 1);
INSERT INTO `sys_dept` VALUES (4, '信息科学技术学院', 0, 1);

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`  (
  `id` bigint(0) NOT NULL,
  `dict_type` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dict_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sort_order` int(0) NULL DEFAULT 0,
  `enabled` tinyint(0) NULL DEFAULT 1,
  `system_item` tinyint(0) NULL DEFAULT 0,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_item_code`(`dict_type`, `dict_code`) USING BTREE,
  INDEX `idx_dict_item_type_enabled`(`dict_type`, `enabled`, `sort_order`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------
INSERT INTO `sys_dict_item` VALUES (201, 'business_status', 'draft', '草稿', 10, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (202, 'business_status', 'pending_dept', '部门负责人审批中', 20, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (203, 'business_status', 'pending_office', '党办校办审核中', 30, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (204, 'business_status', 'pending_leader', '校级领导审批中', 40, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (205, 'business_status', 'pending_security', '保卫部门审核中', 50, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (206, 'business_status', 'pending_finance', '财务审核中', 60, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (207, 'business_status', 'pending_secret_review', '保密审查中', 70, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (208, 'business_status', 'approved', '审批通过', 80, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (209, 'business_status', 'archived', '已归档', 90, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (210, 'business_status', 'rejected', '已退回', 100, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (211, 'business_status', 'used', '已用印', 110, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (212, 'business_status', 'returned', '已归还', 120, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (213, 'business_status', 'running', '办理中', 130, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (214, 'business_status', 'completed', '已完成', 140, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (215, 'business_status', 'pending', '待办理', 150, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (216, 'distribution_status', 'not_distributed', '未分发', 10, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (217, 'distribution_status', 'distributed', '待签收', 20, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (218, 'distribution_status', 'partially_received', '部分签收', 30, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (219, 'distribution_status', 'received', '已签收', 40, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (220, 'biz_type', 'document', '公文', 10, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (221, 'biz_type', 'seal', '用印', 20, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (222, 'biz_type', 'meeting', '会议', 30, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (223, 'biz_type', 'travel', '差旅', 40, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (224, 'biz_type', 'report', '请示报告', 50, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (225, 'flow_node', 'pending_dept', '部门负责人审批', 10, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (226, 'flow_node', 'pending_office', '党办校办审核', 20, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (227, 'flow_node', 'pending_leader', '校级领导审批', 30, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (228, 'flow_node', 'pending_security', '保卫部门审核', 40, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (229, 'flow_node', 'pending_finance', '财务审核', 50, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (230, 'flow_node', 'pending_secret_review', '保密审查', 60, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (231, 'flow_node', 'approved', '审批完成', 70, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (232, 'flow_node', 'archived', '归档完成', 80, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (233, 'role_key', 'admin', '系统管理员', 10, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (234, 'role_key', 'office_user', '普通办公人员', 20, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (235, 'role_key', 'dept_head', '部门负责人', 30, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (236, 'role_key', 'school_leader', '校级领导', 40, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (237, 'role_key', 'office_admin', '党办校办人员', 50, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (238, 'role_key', 'finance_staff', '财务人员', 60, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (239, 'role_key', 'security_staff', '保卫人员', 70, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (240, 'role_key', 'seal_keeper', '印章保管人', 80, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (241, 'secrecy_level', '公开', '公开', 10, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (242, 'secrecy_level', '内部', '内部', 20, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (243, 'secrecy_level', '秘密', '秘密', 30, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (244, 'secrecy_level', '机密', '机密', 40, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (245, 'secrecy_level', '绝密', '绝密', 50, 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (246, 'document_type', '通知', '通知', 10, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (247, 'document_type', '决定', '决定', 20, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (248, 'document_type', '请示', '请示', 30, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (249, 'document_type', '批复', '批复', 40, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (250, 'document_type', '报告', '报告', 50, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (251, 'document_type', '函', '函', 60, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (252, 'document_type', '公告', '公告', 70, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (253, 'matter_level', '常规事项', '常规事项', 10, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (254, 'matter_level', '一般事项', '一般事项', 20, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (255, 'matter_level', '重大事项', '重大事项', 30, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (256, 'seal_type', '行政印章', '行政印章', 10, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (257, 'seal_type', '部门印章', '部门印章', 20, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (258, 'seal_type', '专用章', '专用章', 30, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (259, 'seal_type', '名章', '名章', 40, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (260, 'seal_status', 'in_store', '在库', 10, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:38:51');
INSERT INTO `sys_dict_item` VALUES (261, 'seal_status', 'in_use', '使用中', 20, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (262, 'seal_status', 'lent', '外带中', 30, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (263, 'seal_status', 'retired', '已停用', 40, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (264, 'meeting_type', '国内管理会议', '国内管理会议', 10, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 22:34:28');
INSERT INTO `sys_dict_item` VALUES (265, 'meeting_type', '国内业务会议', '国内业务会议', 20, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (266, 'meeting_type', '在华举办的国际会议', '在华举办的国际会议', 30, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (267, 'venue_type', '室内', '室内', 10, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (268, 'venue_type', '室外', '室外', 20, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (269, 'report_type', '请示', '请示', 10, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (270, 'report_type', '报告', '报告', 20, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (271, 'staff_level', '一类', '一类人员', 10, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (272, 'staff_level', '二类', '二类人员', 20, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (273, 'staff_level', '三类', '三类人员', 30, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (274, 'travel_type', '教学科研业务', '教学科研业务', 10, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (275, 'travel_type', '行政管理业务', '行政管理业务', 20, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (276, 'travel_type', '学术交流', '学术交流', 30, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (277, 'travel_type', '其他业务', '其他业务', 40, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (278, 'transport_type', '飞机', '飞机', 10, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (279, 'transport_type', '高铁一等座', '高铁一等座', 20, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (280, 'transport_type', '高铁二等座', '高铁二等座', 30, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (281, 'transport_type', '火车软卧', '火车软卧', 40, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (282, 'transport_type', '火车硬卧', '火车硬卧', 50, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_item` VALUES (283, 'transport_type', '火车硬座', '火车硬座', 60, 1, 0, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint(0) NOT NULL,
  `dict_type` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `system_type` tinyint(0) NULL DEFAULT 0,
  `enabled` tinyint(0) NULL DEFAULT 1,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type`) USING BTREE,
  INDEX `idx_dict_type_enabled`(`enabled`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (101, 'business_status', '业务状态', 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (102, 'distribution_status', '分发状态', 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (103, 'biz_type', '业务类型', 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (104, 'flow_node', '流程节点', 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (105, 'role_key', '角色', 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (106, 'secrecy_level', '保密等级', 1, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (107, 'document_type', '公文类型', 0, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (108, 'matter_level', '事项级别', 0, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (109, 'seal_type', '印章类型', 0, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (110, 'seal_status', '印章状态', 0, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (111, 'meeting_type', '会议类型', 0, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (112, 'venue_type', '场地类型', 0, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (113, 'report_type', '请示报告类型', 0, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (114, 'staff_level', '人员等级', 0, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (115, 'travel_type', '出差类型', 0, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');
INSERT INTO `sys_dict_type` VALUES (116, 'transport_type', '交通工具', 0, 1, NULL, '2026-05-27 21:33:42', '2026-05-27 21:33:42');

-- ----------------------------
-- Table structure for sys_notification
-- ----------------------------
DROP TABLE IF EXISTS `sys_notification`;
CREATE TABLE `sys_notification`  (
  `id` bigint(0) NOT NULL,
  `receiver_id` bigint(0) NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `read_status` tinyint(0) NULL DEFAULT 0,
  `biz_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `biz_id` bigint(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notification_receiver`(`receiver_id`, `read_status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notification
-- ----------------------------
INSERT INTO `sys_notification` VALUES (1013, 3, '新的待办审批', 'meeting#1008 等待您处理：pending_dept', 0, 'meeting', 1008, '2026-05-25 07:42:01');
INSERT INTO `sys_notification` VALUES (1021, 3, '新的待办审批', 'document#1014 等待您处理：pending_dept', 0, 'document', 1014, '2026-05-25 07:42:01');
INSERT INTO `sys_notification` VALUES (1024, 5, '新的待办审批', 'document#1014 等待您处理：pending_office', 0, 'document', 1014, '2026-05-25 07:42:01');
INSERT INTO `sys_notification` VALUES (1028, 4, '新的待办审批', 'document#1014 等待您处理：pending_leader', 0, 'document', 1014, '2026-05-25 07:42:01');
INSERT INTO `sys_notification` VALUES (1031, 2, '流程状态变更', 'document#1014 已流转为 approved', 0, 'document', 1014, '2026-05-25 07:42:01');
INSERT INTO `sys_notification` VALUES (1034, 2, '公文待签收', 'mysql-distribution-smoke 已分发，请及时签收', 0, 'document', 1014, '2026-05-25 07:42:01');
INSERT INTO `sys_notification` VALUES (1044, 3, '新的待办审批', 'travel#1039 等待您处理：pending_dept', 0, 'travel', 1039, '2026-05-25 07:42:01');
INSERT INTO `sys_notification` VALUES (1047, 6, '新的待办审批', 'travel#1039 等待您处理：pending_finance', 0, 'travel', 1039, '2026-05-25 07:42:01');
INSERT INTO `sys_notification` VALUES (1050, 2, '流程状态变更', 'travel#1039 已流转为 approved', 0, 'travel', 1039, '2026-05-25 07:42:01');
INSERT INTO `sys_notification` VALUES (1057, 6, '新的待办审批', 'travel#1039 等待您处理：pending_finance', 0, 'travel', 1039, '2026-05-25 07:42:01');
INSERT INTO `sys_notification` VALUES (1063, 3, '新的待办审批', 'travel#1058 等待您处理：pending_dept', 0, 'travel', 1058, '2026-05-25 08:06:19');
INSERT INTO `sys_notification` VALUES (1066, 6, '新的待办审批', 'travel#1058 等待您处理：pending_finance', 0, 'travel', 1058, '2026-05-25 08:06:19');
INSERT INTO `sys_notification` VALUES (1069, 2, '流程状态变更', 'travel#1058 已流转为 approved', 0, 'travel', 1058, '2026-05-25 08:06:19');
INSERT INTO `sys_notification` VALUES (1079, 3, '新的待办审批', 'seal#1071 等待您处理：pending_dept', 0, 'seal', 1071, '2026-05-26 01:04:52');

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint(0) NOT NULL,
  `operator_id` bigint(0) NULL DEFAULT NULL,
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `biz_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `biz_id` bigint(0) NULL DEFAULT NULL,
  `detail` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_operation_biz`(`biz_type`, `biz_id`) USING BTREE,
  INDEX `idx_operation_operator`(`operator_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------
INSERT INTO `sys_operation_log` VALUES (1005, 2, 'document', 'create', 'document', 1003, 'mysql-persistence-smoke', '2026-05-25 02:23:35');
INSERT INTO `sys_operation_log` VALUES (1012, 2, 'meeting', 'start_flow', 'meeting', 1008, '流程进入pending_dept', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1016, 2, 'document', 'create', 'document', 1014, 'mysql-distribution-smoke', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1020, 2, 'document', 'start_flow', 'document', 1014, '流程进入pending_dept', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1025, 3, 'document', 'advance_flow', 'document', 1014, 'pending_dept -> pending_office', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1029, 5, 'document', 'advance_flow', 'document', 1014, 'pending_office -> pending_leader', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1032, 4, 'document', 'advance_flow', 'document', 1014, 'pending_leader -> approved', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1035, 5, 'document', 'distribute', 'document', 1014, '分发至用户#2', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1036, 2, 'document', 'receipt', 'document', 1014, '签收记录#1033', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1038, 8, 'seal', 'transfer', 'seal', 2, '移交记录#1037', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1043, 2, 'travel', 'start_flow', 'travel', 1039, '流程进入pending_dept', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1048, 3, 'travel', 'advance_flow', 'travel', 1039, 'pending_dept -> pending_finance', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1051, 6, 'travel', 'advance_flow', 'travel', 1039, 'pending_finance -> approved', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1053, 2, 'travel', 'upload_attachment', 'travel', 1039, '差旅报销凭证', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1056, 2, 'travel', 'start_flow', 'travel', 1039, '流程进入pending_finance', '2026-05-25 07:42:01');
INSERT INTO `sys_operation_log` VALUES (1062, 2, 'travel', 'start_flow', 'travel', 1058, '流程进入pending_dept', '2026-05-25 08:06:19');
INSERT INTO `sys_operation_log` VALUES (1067, 3, 'travel', 'advance_flow', 'travel', 1058, 'pending_dept -> pending_finance', '2026-05-25 08:06:19');
INSERT INTO `sys_operation_log` VALUES (1070, 6, 'travel', 'advance_flow', 'travel', 1058, 'pending_finance -> approved', '2026-05-25 08:06:19');
INSERT INTO `sys_operation_log` VALUES (1073, 2, 'seal', 'upload_attachment', 'seal', 1071, 'oms-seal-material-runtime.pdf', '2026-05-26 01:04:52');
INSERT INTO `sys_operation_log` VALUES (1074, 2, 'seal', 'download_attachment', 'seal', 1071, 'oms-seal-material-runtime.pdf', '2026-05-26 01:04:52');
INSERT INTO `sys_operation_log` VALUES (1078, 2, 'seal', 'start_flow', 'seal', 1071, '流程进入pending_dept', '2026-05-26 01:04:52');
INSERT INTO `sys_operation_log` VALUES (1082, 2, 'seal', 'upload_attachment', 'seal', 1080, 'oms-seal-material-runtime.pdf', '2026-05-26 01:04:52');
INSERT INTO `sys_operation_log` VALUES (1083, 2, 'seal', 'delete_attachment', 'seal', 1080, '运行态逻辑删除验证', '2026-05-26 01:04:52');
INSERT INTO `sys_operation_log` VALUES (1084, 2, 'seal', 'download_attachment', 'seal', 1071, 'oms-seal-material-runtime.pdf', '2026-05-26 01:22:10');
INSERT INTO `sys_operation_log` VALUES (1086, 2, 'seal', 'upload_attachment', 'seal', 1080, 'ui-material.pdf', '2026-05-26 01:26:06');
INSERT INTO `sys_operation_log` VALUES (1087, 2, 'seal', 'update_attachment', 'seal', 1080, 'ui-material-final.pdf', '2026-05-26 01:26:42');
INSERT INTO `sys_operation_log` VALUES (1088, 2, 'seal', 'delete_attachment', 'seal', 1080, '页面逻辑删除验证', '2026-05-26 01:27:10');
INSERT INTO `sys_operation_log` VALUES (1089, 1, 'dictionary', 'update_item', 'dictionary', 260, 'seal_status/in_store', '2026-05-27 21:36:19');
INSERT INTO `sys_operation_log` VALUES (1090, 1, 'dictionary', 'update_item', 'dictionary', 260, 'seal_status/in_store', '2026-05-27 21:37:12');
INSERT INTO `sys_operation_log` VALUES (1091, 1, 'dictionary', 'update_item', 'dictionary', 260, 'seal_status/in_store', '2026-05-27 21:37:55');
INSERT INTO `sys_operation_log` VALUES (1092, 1, 'dictionary', 'update_item', 'dictionary', 260, 'seal_status/in_store', '2026-05-27 21:38:51');
INSERT INTO `sys_operation_log` VALUES (1093, 1, 'dictionary', 'update_item', 'dictionary', 264, 'meeting_type/国内管理会议', '2026-05-27 22:31:01');
INSERT INTO `sys_operation_log` VALUES (1094, 1, 'dictionary', 'update_item', 'dictionary', 264, 'meeting_type/国内管理会议', '2026-05-27 22:34:28');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(0) NOT NULL,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `level` int(0) NULL DEFAULT 0,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `role_key`(`role_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '系统管理员', 'admin', 100, '系统配置和基础数据维护');
INSERT INTO `sys_role` VALUES (2, '普通办公人员', 'office_user', 10, '发起日常办公流程');
INSERT INTO `sys_role` VALUES (3, '部门负责人', 'dept_head', 30, '审批本部门流程');
INSERT INTO `sys_role` VALUES (4, '校级领导', 'school_leader', 80, '审批校级重大事项');
INSERT INTO `sys_role` VALUES (5, '党办校办人员', 'office_admin', 60, '公文归档和校级印章审核');
INSERT INTO `sys_role` VALUES (6, '财务人员', 'finance_staff', 50, '费用预算和报销审核');
INSERT INTO `sys_role` VALUES (7, '保卫人员', 'security_staff', 50, '大型活动安全审核');
INSERT INTO `sys_role` VALUES (8, '印章保管人', 'seal_keeper', 40, '用印登记和归还确认');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(0) NOT NULL,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dept_id` bigint(0) NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint(0) NULL DEFAULT 1,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '系统管理员', 1, NULL, NULL, 1, '2026-05-21 00:36:31', '2026-05-25 02:21:46');
INSERT INTO `sys_user` VALUES (2, 'user', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '普通办公人员', 4, NULL, NULL, 1, '2026-05-21 00:36:31', '2026-05-25 02:21:46');
INSERT INTO `sys_user` VALUES (3, 'head', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '部门负责人', 4, NULL, NULL, 1, '2026-05-21 00:36:31', '2026-05-25 02:21:46');
INSERT INTO `sys_user` VALUES (4, 'leader', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '校级领导', 1, NULL, NULL, 1, '2026-05-21 00:36:31', '2026-05-25 02:21:46');
INSERT INTO `sys_user` VALUES (5, 'office', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '党办校办人员', 1, NULL, NULL, 1, '2026-05-21 00:36:31', '2026-05-25 02:21:46');
INSERT INTO `sys_user` VALUES (6, 'finance', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '财务人员', 2, NULL, NULL, 1, '2026-05-21 00:36:31', '2026-05-25 02:21:46');
INSERT INTO `sys_user` VALUES (7, 'security', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '保卫人员', 3, NULL, NULL, 1, '2026-05-21 00:36:31', '2026-05-25 02:21:46');
INSERT INTO `sys_user` VALUES (8, 'keeper', 'pbkdf2$120000$T2ZmaWNlTWdtdFNhbHQwMQ==$Z3heUPjh43uuIEz+H3am6K517zg+3H0tEMInRgwsg1M=', '印章保管人', 1, NULL, NULL, 1, '2026-05-21 00:36:31', '2026-05-25 02:21:46');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint(0) NOT NULL,
  `role_id` bigint(0) NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);
INSERT INTO `sys_user_role` VALUES (3, 3);
INSERT INTO `sys_user_role` VALUES (4, 4);
INSERT INTO `sys_user_role` VALUES (5, 5);
INSERT INTO `sys_user_role` VALUES (6, 6);
INSERT INTO `sys_user_role` VALUES (7, 7);
INSERT INTO `sys_user_role` VALUES (8, 8);

SET FOREIGN_KEY_CHECKS = 1;
