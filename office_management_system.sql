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

 Date: 12/06/2026 08:08:48
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
INSERT INTO `oa_approval_history` VALUES (1096, 'document', 1095, 2, 'create', '起草公文', '2026-06-10 23:22:02');
INSERT INTO `oa_approval_history` VALUES (5001, 'document', 2003, 3, 'approve', '同意，内容完整，格式规范', '2026-06-03 15:00:00');
INSERT INTO `oa_approval_history` VALUES (5002, 'document', 2004, 3, 'approve', '同意上报，人才引进是学院重点工作', '2026-06-04 09:30:00');
INSERT INTO `oa_approval_history` VALUES (5003, 'document', 2004, 5, 'approve', '审核通过，已确认公文格式', '2026-06-04 10:00:00');
INSERT INTO `oa_approval_history` VALUES (5004, 'document', 2005, 3, 'approve', '同意，基础设施改造方案可行', '2026-06-05 13:00:00');
INSERT INTO `oa_approval_history` VALUES (5005, 'document', 2005, 5, 'approve', '审核通过', '2026-06-05 14:00:00');
INSERT INTO `oa_approval_history` VALUES (5006, 'document', 2005, 4, 'approve', '同意，按计划执行', '2026-06-05 15:00:00');
INSERT INTO `oa_approval_history` VALUES (5007, 'document', 2006, 3, 'approve', '同意', '2026-05-15 10:00:00');
INSERT INTO `oa_approval_history` VALUES (5008, 'document', 2006, 5, 'approve', '审核通过', '2026-05-15 11:00:00');
INSERT INTO `oa_approval_history` VALUES (5009, 'document', 2006, 4, 'approve', '同意', '2026-05-15 14:00:00');
INSERT INTO `oa_approval_history` VALUES (5010, 'document', 2007, 3, 'reject', '请补充具体时间安排及班车调整方案后再提交', '2026-06-06 16:00:00');
INSERT INTO `oa_approval_history` VALUES (5011, 'document', 2008, 3, 'approve', '同意', '2026-06-07 11:00:00');
INSERT INTO `oa_approval_history` VALUES (5012, 'document', 2008, 5, 'reject', '格式不规范，请参照公文格式要求修改后重新提交', '2026-06-07 14:00:00');
INSERT INTO `oa_approval_history` VALUES (5013, 'document', 2009, 3, 'approve', '同意，安全管理措施到位', '2026-06-08 09:00:00');
INSERT INTO `oa_approval_history` VALUES (5014, 'document', 2009, 5, 'approve', '审核通过', '2026-06-08 10:00:00');
INSERT INTO `oa_approval_history` VALUES (5015, 'document', 2009, 4, 'approve', '同意，安全工作要常抓不懈', '2026-06-08 11:00:00');
INSERT INTO `oa_approval_history` VALUES (5016, 'document', 2010, 3, 'approve', '同意上报', '2026-06-09 17:00:00');
INSERT INTO `oa_approval_history` VALUES (5017, 'document', 2010, 5, 'approve', '审核通过，职称评审工作安排合理', '2026-06-10 09:00:00');
INSERT INTO `oa_approval_history` VALUES (5101, 'seal', 2005, 3, 'approve', '同意用印', '2026-06-04 16:00:00');
INSERT INTO `oa_approval_history` VALUES (5102, 'seal', 2006, 3, 'approve', '同意', '2026-06-05 11:00:00');
INSERT INTO `oa_approval_history` VALUES (5103, 'seal', 2007, 3, 'approve', '同意用印', '2026-06-05 15:30:00');
INSERT INTO `oa_approval_history` VALUES (5104, 'seal', 2007, 5, 'approve', '审核通过', '2026-06-05 16:00:00');
INSERT INTO `oa_approval_history` VALUES (5105, 'seal', 2008, 3, 'reject', '材料不完整，请补充论文版权协议原件', '2026-06-06 17:00:00');
INSERT INTO `oa_approval_history` VALUES (5106, 'seal', 2009, 5, 'approve', '同意外带用印', '2026-06-07 09:00:00');
INSERT INTO `oa_approval_history` VALUES (5107, 'seal', 2009, 4, 'approve', '同意，注意保管', '2026-06-07 10:00:00');
INSERT INTO `oa_approval_history` VALUES (5201, 'meeting', 2005, 3, 'approve', '同意，会议安排合理', '2026-06-05 10:00:00');
INSERT INTO `oa_approval_history` VALUES (5202, 'meeting', 2006, 3, 'approve', '同意', '2026-06-06 09:00:00');
INSERT INTO `oa_approval_history` VALUES (5203, 'meeting', 2007, 3, 'approve', '同意召开', '2026-06-07 11:00:00');
INSERT INTO `oa_approval_history` VALUES (5204, 'meeting', 2008, 3, 'approve', '同意', '2026-05-10 10:00:00');
INSERT INTO `oa_approval_history` VALUES (5205, 'meeting', 2009, 7, 'reject', '安全预案不充分，大型活动需提供详细的安全疏散方案', '2026-06-08 15:00:00');
INSERT INTO `oa_approval_history` VALUES (5206, 'meeting', 2010, 7, 'approve', '安全预案审核通过', '2026-06-09 09:00:00');
INSERT INTO `oa_approval_history` VALUES (5207, 'meeting', 2010, 3, 'approve', '同意', '2026-06-09 10:00:00');
INSERT INTO `oa_approval_history` VALUES (5208, 'meeting', 2010, 4, 'approve', '同意举办，注意控制经费', '2026-06-09 11:00:00');
INSERT INTO `oa_approval_history` VALUES (5209, 'meeting', 2004, 7, 'approve', '安保方案可行', '2026-06-04 11:00:00');
INSERT INTO `oa_approval_history` VALUES (5210, 'meeting', 2004, 3, 'approve', '同意', '2026-06-04 14:00:00');
INSERT INTO `oa_approval_history` VALUES (5301, 'travel', 2003, 3, 'approve', '同意出差，注意安全', '2026-06-03 14:00:00');
INSERT INTO `oa_approval_history` VALUES (5302, 'travel', 2004, 3, 'approve', '同意', '2026-06-04 10:00:00');
INSERT INTO `oa_approval_history` VALUES (5303, 'travel', 2004, 6, 'approve', '预算合理，同意报销', '2026-06-04 14:00:00');
INSERT INTO `oa_approval_history` VALUES (5304, 'travel', 2005, 3, 'reject', '出差事由不充分，建议通过视频会议替代', '2026-06-05 15:00:00');
INSERT INTO `oa_approval_history` VALUES (5305, 'travel', 2006, 3, 'approve', '同意', '2026-05-01 10:00:00');
INSERT INTO `oa_approval_history` VALUES (5306, 'travel', 2006, 6, 'approve', '费用合规，同意报销', '2026-05-01 14:00:00');
INSERT INTO `oa_approval_history` VALUES (5307, 'travel', 2009, 3, 'approve', '同意，科研需要可以理解超支', '2026-06-01 15:00:00');
INSERT INTO `oa_approval_history` VALUES (5308, 'travel', 2009, 6, 'approve', '超支理由充分，同意报销', '2026-06-01 16:00:00');
INSERT INTO `oa_approval_history` VALUES (5309, 'travel', 2010, 3, 'approve', '同意', '2026-06-08 14:00:00');
INSERT INTO `oa_approval_history` VALUES (5401, 'report', 2003, 5, 'approve', '保密审查通过，可进入下一环节', '2026-06-03 12:00:00');
INSERT INTO `oa_approval_history` VALUES (5402, 'report', 2004, 5, 'approve', '保密审查通过', '2026-06-04 15:00:00');
INSERT INTO `oa_approval_history` VALUES (5403, 'report', 2004, 3, 'approve', '同意上报，招生计划调整理由充分', '2026-06-04 16:00:00');
INSERT INTO `oa_approval_history` VALUES (5404, 'report', 2005, 5, 'approve', '审查通过', '2026-06-05 10:00:00');
INSERT INTO `oa_approval_history` VALUES (5405, 'report', 2005, 3, 'approve', '同意', '2026-06-05 11:00:00');
INSERT INTO `oa_approval_history` VALUES (5406, 'report', 2005, 4, 'approve', '工作总结全面，成绩突出', '2026-06-05 14:00:00');
INSERT INTO `oa_approval_history` VALUES (5407, 'report', 2006, 5, 'approve', '审查通过', '2026-06-06 11:00:00');
INSERT INTO `oa_approval_history` VALUES (5408, 'report', 2006, 3, 'reject', '预算方案不明确，请补充详细的资金来源和使用计划', '2026-06-06 14:00:00');
INSERT INTO `oa_approval_history` VALUES (5409, 'report', 2008, 5, 'approve', '审查通过', '2026-06-08 10:00:00');
INSERT INTO `oa_approval_history` VALUES (5410, 'report', 2008, 3, 'approve', '同意', '2026-06-08 11:00:00');
INSERT INTO `oa_approval_history` VALUES (5411, 'report', 2008, 4, 'approve', '人才引进工作做得很好，继续保持', '2026-06-08 14:00:00');
INSERT INTO `oa_approval_history` VALUES (5412, 'report', 2009, 5, 'approve', '审查通过', '2026-06-09 11:00:00');
INSERT INTO `oa_approval_history` VALUES (5413, 'report', 2010, 5, 'approve', '审查通过', '2026-06-10 10:00:00');
INSERT INTO `oa_approval_history` VALUES (5414, 'report', 2010, 3, 'approve', '同意', '2026-06-10 11:00:00');
INSERT INTO `oa_approval_history` VALUES (5415, 'report', 2010, 4, 'approve', '评估报告质量高，建议加强实践教学建设', '2026-06-10 14:00:00');

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
INSERT INTO `oa_document` VALUES (1003, '校发〔2026〕1003号', 'mysql-persistence-smoke', 'notice', '普通', 'public', 'all', 'persistence-smoke-body', 2, 4, 'draft', '{\"issues\": [\"标题建议采用“关于XXX的notice”格式。\", \"正文内容较短，建议补充背景、事项和工作要求。\"], \"passed\": false, \"keywords\": [\"persistence-smoke-body\", \"mysql-persistence-smoke\"], \"suggestions\": [], \"qualityScore\": 30.0, \"maxSimilarity\": 0.0, \"sensitiveWords\": [], \"recommendedSecrecyLevel\": \"public\"}', '2026-05-25 02:23:35', '2026-06-10 23:22:42', 1, 'not_distributed');
INSERT INTO `oa_document` VALUES (1014, '校发〔2026〕1014号', 'mysql-distribution-smoke', 'notice', '普通', 'public', '全校', 'public persistence workflow content', 2, 4, 'approved', '{\"issues\": [\"标题建议采用“关于XXX的notice”格式。\"], \"passed\": false, \"keywords\": [\"workflow\", \"persistence\", \"mysql-distribution-smoke\", \"content\", \"public\"], \"suggestions\": [], \"qualityScore\": 65.0, \"maxSimilarity\": 0.0, \"sensitiveWords\": [], \"recommendedSecrecyLevel\": \"public\"}', '2026-05-25 07:42:01', '2026-06-12 07:51:45', 1, 'received');
INSERT INTO `oa_document` VALUES (1095, '校发〔2026〕1095号', '关于办公管理系统试运行的通知', '通知', '普通', '公开', '全校', '关于办公管理系统试运行的通知\n\n各单位：\n为规范推进办公管理系统试运行相关工作，根据学校办公管理要求，现将有关事项通知如下：\n\n一、背景与目标\n根据上级精神和学校工作部署，围绕办公管理系统试运行，制定本通知。重点工作包括：明确、试运行、范围、反馈、方式等方面。\n\n二、工作内容\n明确试运行范围、反馈方式和时间要求。\n\n三、工作要求\n１．关于试运行：请各单位高度重视，明确责任分工，确保试运行工作落到实处。\n２．关于范围：请各单位高度重视，明确责任分工，确保范围工作落到实处。\n３．关于明确：请各单位高度重视，明确责任分工，确保明确工作落到实处。\n\n四、时间安排与反馈\n请各单位于本学期末前将落实情况报送至学校办公室，联系人及方式另行通知。\n\n【智能提取关键词】试运行、范围、明确、方式、反馈、时间、管理系统、要求\n\n北京大学\n2026-06-10', 2, 4, 'draft', '{\"issues\": [], \"passed\": true, \"keywords\": [\"试运行\", \"工作\", \"通知\", \"学校\", \"明确\", \"单位\", \"范围\", \"管理系统\", \"要求\", \"方式\"], \"suggestions\": [], \"qualityScore\": 100.0, \"maxSimilarity\": 0.0, \"sensitiveWords\": [], \"recommendedSecrecyLevel\": \"公开\"}', '2026-06-10 23:22:02', '2026-06-10 23:22:05', 1, 'not_distributed');
INSERT INTO `oa_document` VALUES (2001, NULL, '关于调整教学计划的通知', '通知', '普通', '公开', '全校师生', '根据教育部最新要求，经学校教学委员会研究决定，对2026-2027学年教学计划进行如下调整：一、增加实践教学环节比重；二、优化通识教育课程体系；三、加强创新创业教育。请各学院认真落实。', 2, 4, 'draft', NULL, '2026-06-01 09:00:00', '2026-06-12 01:36:45', 1, 'not_distributed');
INSERT INTO `oa_document` VALUES (2002, NULL, '关于开展期末考试安排的决定', '决定', '紧急', '公开', '教务处及各学院', '为确保期末考试顺利进行，现将考试安排通知如下：考试时间为2026年7月1日至7月15日。请各学院在6月20日前完成考试安排上报。', 2, 4, 'pending_dept', NULL, '2026-06-02 10:30:00', '2026-06-12 01:36:45', 1, 'not_distributed');
INSERT INTO `oa_document` VALUES (2003, '北大办〔2026〕22号', '关于召开学术委员会会议的请示', '请示', '普通', '内部', '学术委员会成员', '兹定于2026年6月20日召开学术委员会全体会议，审议以下事项：一、新增博士学位授权点评审；二、重点学科建设方案论证；三、学术成果奖励评定。', 2, 4, 'pending_office', NULL, '2026-06-03 14:00:00', '2026-06-12 01:36:45', 1, 'not_distributed');
INSERT INTO `oa_document` VALUES (2004, '北大办〔2026〕23号', '关于引进高层次人才的批复', '批复', '加急', '秘密', '人事处及相关学院', '经学校研究，同意信息科学技术学院引进教授2名、副教授3名的申请。请人事处按照相关规定办理引进手续，确保人才待遇落实到位。', 2, 4, 'pending_leader', NULL, '2026-06-04 08:45:00', '2026-06-12 01:36:45', 2, 'not_distributed');
INSERT INTO `oa_document` VALUES (2005, '北大办〔2026〕24号', '关于校园基础设施改造的报告', '报告', '普通', '公开', '全校师生', '根据学校发展规划，现就校园基础设施改造工作进展报告如下：一、图书馆扩建工程已完成主体结构施工；二、学生公寓翻新项目正在进行中；三、实验室安全改造工程已启动招标。', 2, 4, 'approved', NULL, '2026-06-05 11:00:00', '2026-06-12 01:36:45', 1, 'distributed');
INSERT INTO `oa_document` VALUES (2006, '北大办〔2026〕25号', '关于发布年度预算执行情况的函', '函', '普通', '公开', '各院系及职能部门', '现将2025年度预算执行情况通报如下：全校总收入25.8亿元，总支出24.6亿元，结余1.2亿元。各院系预算执行率均达到95%以上。', 2, 4, 'archived', NULL, '2026-05-15 09:30:00', '2026-06-12 01:36:45', 1, 'received');
INSERT INTO `oa_document` VALUES (2007, NULL, '关于调整作息时间的通知', '通知', '普通', '公开', '全校师生', '因夏季作息时间调整需要，本通知经部门负责人退回，需补充完善具体时间安排及班车调整方案。', 2, 4, 'rejected', NULL, '2026-06-06 15:20:00', '2026-06-12 01:36:45', 1, 'not_distributed');
INSERT INTO `oa_document` VALUES (2008, '北大办〔2026〕26号', '关于组织学生暑期社会实践的公告', '公告', '普通', '公开', '全体学生', '为丰富学生暑期生活，学校将组织2026年暑期社会实践活动，包括志愿服务、企业参观、农村支教等项目。此申请经党办校办审核后退回修改。', 2, 4, 'rejected', NULL, '2026-06-07 10:00:00', '2026-06-12 01:36:45', 2, 'not_distributed');
INSERT INTO `oa_document` VALUES (2009, '北大办〔2026〕27号', '关于实验室安全管理的决定', '决定', '紧急', '内部', '各实验室负责人', '为加强实验室安全管理，经学校安全委员会研究，做出以下决定：一、建立实验室安全责任制；二、每月进行安全检查；三、配备必要的安全防护设施；四、开展安全培训。', 2, 4, 'approved', NULL, '2026-06-08 08:00:00', '2026-06-12 01:36:45', 1, 'distributed');
INSERT INTO `oa_document` VALUES (2010, '北大办〔2026〕28号', '关于教师职称评审的通知', '通知', '加急', '机密', '各学院人事干部', '根据《高等学校教师职称评审管理办法》，现启动2026年度教师职称评审工作。请各学院于7月15日前完成推荐上报。', 2, 4, 'pending_leader', NULL, '2026-06-09 16:00:00', '2026-06-12 01:36:45', 1, 'not_distributed');

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
INSERT INTO `oa_document_distribution` VALUES (2501, 2005, 3, 4, 'distributed', '2026-06-06 09:00:00', NULL, NULL, '2026-06-06 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_document_distribution` VALUES (2502, 2005, 4, 1, 'received', '2026-06-06 09:00:00', '2026-06-06 10:00:00', NULL, '2026-06-06 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_document_distribution` VALUES (2503, 2005, 5, 1, 'received', '2026-06-06 09:00:00', '2026-06-06 10:30:00', NULL, '2026-06-06 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_document_distribution` VALUES (2504, 2005, 6, 2, 'distributed', '2026-06-06 09:00:00', NULL, '2026-06-08 09:00:00', '2026-06-06 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_document_distribution` VALUES (2511, 2006, 2, 4, 'received', '2026-05-16 09:00:00', '2026-05-16 10:00:00', NULL, '2026-05-16 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_document_distribution` VALUES (2512, 2006, 3, 4, 'received', '2026-05-16 09:00:00', '2026-05-16 11:00:00', NULL, '2026-05-16 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_document_distribution` VALUES (2513, 2006, 4, 1, 'received', '2026-05-16 09:00:00', '2026-05-16 09:30:00', NULL, '2026-05-16 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_document_distribution` VALUES (2514, 2006, 6, 2, 'received', '2026-05-16 09:00:00', '2026-05-16 14:00:00', NULL, '2026-05-16 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_document_distribution` VALUES (2521, 2009, 2, 4, 'received', '2026-06-09 09:00:00', '2026-06-09 09:30:00', NULL, '2026-06-09 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_document_distribution` VALUES (2522, 2009, 3, 4, 'distributed', '2026-06-09 09:00:00', NULL, NULL, '2026-06-09 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_document_distribution` VALUES (2523, 2009, 6, 2, 'received', '2026-06-09 09:00:00', '2026-06-09 15:00:00', NULL, '2026-06-09 09:00:00', '2026-06-12 01:36:45');

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
INSERT INTO `oa_flow_instance` VALUES (3001, 'document', 2001, NULL, 'running', 2, '2026-06-01 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3002, 'document', 2002, 'pending_dept', 'running', 2, '2026-06-02 10:30:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3003, 'document', 2003, 'pending_office', 'running', 2, '2026-06-03 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3004, 'document', 2004, 'pending_leader', 'running', 2, '2026-06-04 08:45:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3005, 'document', 2005, 'approved', 'completed', 2, '2026-06-05 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3006, 'document', 2006, 'archived', 'completed', 2, '2026-05-15 09:30:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3007, 'document', 2007, 'rejected', 'terminated', 2, '2026-06-06 15:20:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3008, 'document', 2008, 'rejected', 'terminated', 2, '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3009, 'document', 2009, 'approved', 'completed', 2, '2026-06-08 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3010, 'document', 2010, 'pending_leader', 'running', 2, '2026-06-09 16:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3101, 'seal', 2001, NULL, 'running', 2, '2026-06-01 08:30:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3102, 'seal', 2002, 'pending_dept', 'running', 2, '2026-06-02 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3103, 'seal', 2003, 'pending_office', 'running', 2, '2026-06-03 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3104, 'seal', 2004, 'pending_leader', 'running', 2, '2026-06-04 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3105, 'seal', 2005, 'approved', 'completed', 2, '2026-06-04 15:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3106, 'seal', 2006, 'approved', 'completed', 2, '2026-06-05 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3107, 'seal', 2007, 'approved', 'completed', 2, '2026-06-05 14:30:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3108, 'seal', 2008, 'rejected', 'terminated', 2, '2026-06-06 16:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3109, 'seal', 2009, 'approved', 'completed', 2, '2026-06-07 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3110, 'seal', 2010, 'pending_dept', 'running', 2, '2026-06-08 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3201, 'meeting', 2001, NULL, 'running', 2, '2026-06-01 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3202, 'meeting', 2002, 'pending_dept', 'running', 2, '2026-06-02 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3203, 'meeting', 2003, 'pending_security', 'running', 2, '2026-06-03 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3204, 'meeting', 2004, 'pending_leader', 'running', 2, '2026-06-04 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3205, 'meeting', 2005, 'approved', 'completed', 2, '2026-06-05 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3206, 'meeting', 2006, 'approved', 'completed', 2, '2026-06-06 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3207, 'meeting', 2007, 'approved', 'completed', 2, '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3208, 'meeting', 2008, 'archived', 'completed', 2, '2026-05-10 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3209, 'meeting', 2009, 'rejected', 'terminated', 2, '2026-06-08 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3210, 'meeting', 2010, 'approved', 'completed', 2, '2026-06-09 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3301, 'travel', 2001, NULL, 'running', 2, '2026-06-01 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3302, 'travel', 2002, 'pending_dept', 'running', 2, '2026-06-02 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3303, 'travel', 2003, 'pending_finance', 'running', 2, '2026-06-03 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3304, 'travel', 2004, 'approved', 'completed', 2, '2026-06-04 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3305, 'travel', 2005, 'rejected', 'terminated', 2, '2026-06-05 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3306, 'travel', 2006, 'approved', 'completed', 2, '2026-05-01 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3307, 'travel', 2007, NULL, 'running', 2, '2026-06-06 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3308, 'travel', 2008, 'pending_dept', 'running', 2, '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3309, 'travel', 2009, 'approved', 'completed', 2, '2026-06-01 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3310, 'travel', 2010, 'pending_finance', 'running', 2, '2026-06-08 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3401, 'report', 2001, NULL, 'running', 2, '2026-06-01 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3402, 'report', 2002, 'pending_secret_review', 'running', 2, '2026-06-02 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3403, 'report', 2003, 'pending_dept', 'running', 2, '2026-06-03 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3404, 'report', 2004, 'pending_leader', 'running', 2, '2026-06-04 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3405, 'report', 2005, 'approved', 'completed', 2, '2026-06-05 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3406, 'report', 2006, 'rejected', 'terminated', 2, '2026-06-06 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3407, 'report', 2007, NULL, 'running', 2, '2026-06-07 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3408, 'report', 2008, 'approved', 'completed', 2, '2026-06-08 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3409, 'report', 2009, 'pending_dept', 'running', 2, '2026-06-09 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_instance` VALUES (3410, 'report', 2010, 'approved', 'completed', 2, '2026-06-10 09:00:00', '2026-06-12 01:36:45');

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
INSERT INTO `oa_flow_task` VALUES (4001, 3002, 'document', 2002, 'pending_dept', 'dept_head', NULL, 'pending', NULL, '2026-06-02 10:30:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4002, 3002, 'document', 2002, 'pending_office', 'office_admin', NULL, 'pending', NULL, '2026-06-02 10:30:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4003, 3002, 'document', 2002, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-02 10:30:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4011, 3003, 'document', 2003, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-03 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4012, 3003, 'document', 2003, 'pending_office', 'office_admin', NULL, 'pending', NULL, '2026-06-03 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4013, 3003, 'document', 2003, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-03 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4021, 3004, 'document', 2004, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-04 08:45:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4022, 3004, 'document', 2004, 'pending_office', 'office_admin', 5, 'approved', NULL, '2026-06-04 08:45:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4023, 3004, 'document', 2004, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-04 08:45:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4031, 3005, 'document', 2005, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-05 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4032, 3005, 'document', 2005, 'pending_office', 'office_admin', 5, 'approved', NULL, '2026-06-05 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4033, 3005, 'document', 2005, 'pending_leader', 'school_leader', 4, 'approved', NULL, '2026-06-05 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4041, 3006, 'document', 2006, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-05-15 09:30:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4042, 3006, 'document', 2006, 'pending_office', 'office_admin', 5, 'approved', NULL, '2026-05-15 09:30:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4043, 3006, 'document', 2006, 'pending_leader', 'school_leader', 4, 'approved', NULL, '2026-05-15 09:30:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4051, 3007, 'document', 2007, 'pending_dept', 'dept_head', 3, 'rejected', NULL, '2026-06-06 15:20:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4052, 3007, 'document', 2007, 'pending_office', 'office_admin', NULL, 'pending', NULL, '2026-06-06 15:20:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4053, 3007, 'document', 2007, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-06 15:20:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4061, 3008, 'document', 2008, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4062, 3008, 'document', 2008, 'pending_office', 'office_admin', 5, 'rejected', NULL, '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4063, 3008, 'document', 2008, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4071, 3009, 'document', 2009, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-08 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4072, 3009, 'document', 2009, 'pending_office', 'office_admin', 5, 'approved', NULL, '2026-06-08 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4073, 3009, 'document', 2009, 'pending_leader', 'school_leader', 4, 'approved', NULL, '2026-06-08 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4081, 3010, 'document', 2010, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-09 16:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4082, 3010, 'document', 2010, 'pending_office', 'office_admin', 5, 'approved', NULL, '2026-06-09 16:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4083, 3010, 'document', 2010, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-09 16:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4101, 3102, 'seal', 2002, 'pending_dept', 'dept_head', NULL, 'pending', NULL, '2026-06-02 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4111, 3103, 'seal', 2003, 'pending_office', 'office_admin', NULL, 'pending', NULL, '2026-06-03 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4121, 3104, 'seal', 2004, 'pending_office', 'office_admin', 5, 'approved', NULL, '2026-06-04 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4122, 3104, 'seal', 2004, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-04 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4131, 3105, 'seal', 2005, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-04 15:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4141, 3106, 'seal', 2006, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-05 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4151, 3107, 'seal', 2007, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-05 14:30:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4152, 3107, 'seal', 2007, 'pending_office', 'office_admin', 5, 'approved', NULL, '2026-06-05 14:30:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4161, 3108, 'seal', 2008, 'pending_dept', 'dept_head', 3, 'rejected', NULL, '2026-06-06 16:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4171, 3109, 'seal', 2009, 'pending_office', 'office_admin', 5, 'approved', NULL, '2026-06-07 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4172, 3109, 'seal', 2009, 'pending_leader', 'school_leader', 4, 'approved', NULL, '2026-06-07 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4181, 3110, 'seal', 2010, 'pending_dept', 'dept_head', NULL, 'pending', NULL, '2026-06-08 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4201, 3202, 'meeting', 2002, 'pending_dept', 'dept_head', NULL, 'pending', NULL, '2026-06-02 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4211, 3203, 'meeting', 2003, 'pending_security', 'security_staff', NULL, 'pending', NULL, '2026-06-03 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4212, 3203, 'meeting', 2003, 'pending_dept', 'dept_head', NULL, 'pending', NULL, '2026-06-03 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4213, 3203, 'meeting', 2003, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-03 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4221, 3204, 'meeting', 2004, 'pending_security', 'security_staff', 7, 'approved', NULL, '2026-06-04 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4222, 3204, 'meeting', 2004, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-04 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4223, 3204, 'meeting', 2004, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-04 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4231, 3205, 'meeting', 2005, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-05 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4241, 3206, 'meeting', 2006, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-06 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4251, 3207, 'meeting', 2007, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4261, 3208, 'meeting', 2008, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-05-10 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4271, 3209, 'meeting', 2009, 'pending_security', 'security_staff', 7, 'rejected', NULL, '2026-06-08 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4272, 3209, 'meeting', 2009, 'pending_dept', 'dept_head', NULL, 'pending', NULL, '2026-06-08 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4273, 3209, 'meeting', 2009, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-08 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4281, 3210, 'meeting', 2010, 'pending_security', 'security_staff', 7, 'approved', NULL, '2026-06-09 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4282, 3210, 'meeting', 2010, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-09 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4283, 3210, 'meeting', 2010, 'pending_leader', 'school_leader', 4, 'approved', NULL, '2026-06-09 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4301, 3302, 'travel', 2002, 'pending_dept', 'dept_head', NULL, 'pending', NULL, '2026-06-02 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4302, 3302, 'travel', 2002, 'pending_finance', 'finance_staff', NULL, 'pending', NULL, '2026-06-02 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4311, 3303, 'travel', 2003, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-03 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4312, 3303, 'travel', 2003, 'pending_finance', 'finance_staff', NULL, 'pending', NULL, '2026-06-03 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4321, 3304, 'travel', 2004, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-04 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4322, 3304, 'travel', 2004, 'pending_finance', 'finance_staff', 6, 'approved', NULL, '2026-06-04 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4331, 3305, 'travel', 2005, 'pending_dept', 'dept_head', 3, 'rejected', NULL, '2026-06-05 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4332, 3305, 'travel', 2005, 'pending_finance', 'finance_staff', NULL, 'pending', NULL, '2026-06-05 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4341, 3306, 'travel', 2006, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-05-01 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4342, 3306, 'travel', 2006, 'pending_finance', 'finance_staff', 6, 'approved', NULL, '2026-05-01 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4351, 3308, 'travel', 2008, 'pending_dept', 'dept_head', NULL, 'pending', NULL, '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4352, 3308, 'travel', 2008, 'pending_finance', 'finance_staff', NULL, 'pending', NULL, '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4361, 3309, 'travel', 2009, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-01 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4362, 3309, 'travel', 2009, 'pending_finance', 'finance_staff', 6, 'approved', NULL, '2026-06-01 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4371, 3310, 'travel', 2010, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-08 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4372, 3310, 'travel', 2010, 'pending_finance', 'finance_staff', NULL, 'pending', NULL, '2026-06-08 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4401, 3402, 'report', 2002, 'pending_secret_review', 'office_admin', NULL, 'pending', NULL, '2026-06-02 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4402, 3402, 'report', 2002, 'pending_dept', 'dept_head', NULL, 'pending', NULL, '2026-06-02 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4403, 3402, 'report', 2002, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-02 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4411, 3403, 'report', 2003, 'pending_secret_review', 'office_admin', 5, 'approved', NULL, '2026-06-03 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4412, 3403, 'report', 2003, 'pending_dept', 'dept_head', NULL, 'pending', NULL, '2026-06-03 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4413, 3403, 'report', 2003, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-03 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4421, 3404, 'report', 2004, 'pending_secret_review', 'office_admin', 5, 'approved', NULL, '2026-06-04 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4422, 3404, 'report', 2004, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-04 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4423, 3404, 'report', 2004, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-04 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4431, 3405, 'report', 2005, 'pending_secret_review', 'office_admin', 5, 'approved', NULL, '2026-06-05 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4432, 3405, 'report', 2005, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-05 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4433, 3405, 'report', 2005, 'pending_leader', 'school_leader', 4, 'approved', NULL, '2026-06-05 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4441, 3406, 'report', 2006, 'pending_secret_review', 'office_admin', 5, 'approved', NULL, '2026-06-06 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4442, 3406, 'report', 2006, 'pending_dept', 'dept_head', 3, 'rejected', NULL, '2026-06-06 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4443, 3406, 'report', 2006, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-06 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4451, 3408, 'report', 2008, 'pending_secret_review', 'office_admin', 5, 'approved', NULL, '2026-06-08 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4452, 3408, 'report', 2008, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-08 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4453, 3408, 'report', 2008, 'pending_leader', 'school_leader', 4, 'approved', NULL, '2026-06-08 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4461, 3409, 'report', 2009, 'pending_secret_review', 'office_admin', 5, 'approved', NULL, '2026-06-09 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4462, 3409, 'report', 2009, 'pending_dept', 'dept_head', NULL, 'pending', NULL, '2026-06-09 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4463, 3409, 'report', 2009, 'pending_leader', 'school_leader', NULL, 'pending', NULL, '2026-06-09 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4471, 3410, 'report', 2010, 'pending_secret_review', 'office_admin', 5, 'approved', NULL, '2026-06-10 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4472, 3410, 'report', 2010, 'pending_dept', 'dept_head', 3, 'approved', NULL, '2026-06-10 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_flow_task` VALUES (4473, 3410, 'report', 2010, 'pending_leader', 'school_leader', 4, 'approved', NULL, '2026-06-10 09:00:00', '2026-06-12 01:36:45');

-- ----------------------------
-- Table structure for oa_mail_message
-- ----------------------------
DROP TABLE IF EXISTS `oa_mail_message`;
CREATE TABLE `oa_mail_message`  (
  `id` bigint(0) NOT NULL,
  `sender_id` bigint(0) NOT NULL,
  `subject` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_mail_message
-- ----------------------------
INSERT INTO `oa_mail_message` VALUES (2001, 2, '关于提交期末教学材料的提醒', '各位老师好：请于本周五前将期末考试试卷、成绩分析报告等教学材料提交至教务办公室。如有疑问请联系教务科张老师。', '2026-06-01 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_message` VALUES (2002, 2, '科研项目中期检查通知', '各位项目负责人：2026年度科研项目中期检查将于7月1日开始，请提前准备中期报告和相关佐证材料。', '2026-06-02 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_message` VALUES (2003, 5, '关于公文格式规范的通知', '各部门：为规范公文格式，请参照最新版《公文处理办法》执行。重点注意：标题格式、正文排版、附件标注等。', '2026-06-03 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_message` VALUES (2004, 3, '部门例会通知', '信息科学技术学院全体教师：本周五下午2:00在理科一号楼101召开部门例会，请准时参加。议题：期末工作安排。', '2026-06-04 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_message` VALUES (2005, 2, '实验室安全培训报名通知', '各位实验室负责人：学校将于6月20日举办实验室安全培训班，请各实验室至少派1人参加。报名截止日期：6月15日。', '2026-06-05 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_message` VALUES (2006, 4, '关于加快推进重点项目建设的批示', '信息科学技术学院：关于人工智能实验室建设项目，请加快进度，确保9月1日前完成设备安装调试。', '2026-06-06 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_message` VALUES (2007, 2, '暑期学校志愿者招募通知', '各位同学和老师：2026年暑期学校将于8月1日开课，现招募助教志愿者20名。有意者请于6月30日前报名。', '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_message` VALUES (2008, 6, '差旅报销流程调整通知', '各位老师：自2026年7月1日起，差旅报销实行线上审批流程，请登录办公系统提交报销申请。纸质材料不再受理。', '2026-06-08 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_message` VALUES (2009, 2, '学术讲座嘉宾接待安排', '各位老师：清华大学张教授将于6月20日来校做学术报告，请相关老师协助做好接待工作。', '2026-06-09 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_message` VALUES (2010, 5, '办公系统升级维护通知', '全校师生：办公管理系统将于6月15日凌晨2:00-6:00进行升级维护，届时系统将暂停服务。请提前保存工作内容。', '2026-06-10 08:00:00', '2026-06-12 01:36:45');

-- ----------------------------
-- Table structure for oa_mail_recipient
-- ----------------------------
DROP TABLE IF EXISTS `oa_mail_recipient`;
CREATE TABLE `oa_mail_recipient`  (
  `id` bigint(0) NOT NULL,
  `mail_id` bigint(0) NOT NULL,
  `user_id` bigint(0) NOT NULL,
  `recipient_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'to/cc/bcc',
  `read_status` tinyint(0) NULL DEFAULT 0,
  `read_at` datetime(0) NULL DEFAULT NULL,
  `email_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending',
  `email_error` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email_sent_at` datetime(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_mail_recipient
-- ----------------------------
INSERT INTO `oa_mail_recipient` VALUES (2001, 2001, 3, 'to', 1, '2026-06-01 10:00:00', 'sent', NULL, '2026-06-01 09:01:00', '2026-06-01 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2002, 2001, 4, 'to', 1, '2026-06-01 11:00:00', 'sent', NULL, '2026-06-01 09:01:00', '2026-06-01 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2003, 2001, 5, 'cc', 0, NULL, 'sent', NULL, '2026-06-01 09:01:00', '2026-06-01 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2011, 2002, 3, 'to', 1, '2026-06-02 15:00:00', 'sent', NULL, '2026-06-02 10:01:00', '2026-06-02 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2012, 2002, 4, 'to', 0, NULL, 'pending', NULL, NULL, '2026-06-02 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2021, 2003, 2, 'to', 1, '2026-06-03 16:00:00', 'sent', NULL, '2026-06-03 14:01:00', '2026-06-03 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2022, 2003, 3, 'to', 0, NULL, 'sent', NULL, '2026-06-03 14:01:00', '2026-06-03 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2023, 2003, 4, 'to', 1, '2026-06-03 17:00:00', 'sent', NULL, '2026-06-03 14:01:00', '2026-06-03 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2031, 2004, 2, 'to', 1, '2026-06-04 09:30:00', 'sent', NULL, '2026-06-04 09:01:00', '2026-06-04 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2041, 2005, 3, 'to', 1, '2026-06-05 15:00:00', 'sent', NULL, '2026-06-05 11:01:00', '2026-06-05 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2042, 2005, 4, 'cc', 0, NULL, 'failed', '邮箱地址不存在', NULL, '2026-06-05 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2051, 2006, 2, 'to', 1, '2026-06-06 09:00:00', 'sent', NULL, '2026-06-06 08:01:00', '2026-06-06 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2052, 2006, 3, 'cc', 1, '2026-06-06 10:00:00', 'sent', NULL, '2026-06-06 08:01:00', '2026-06-06 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2061, 2007, 3, 'to', 0, NULL, 'pending', NULL, NULL, '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2071, 2008, 2, 'to', 0, NULL, 'sent', NULL, '2026-06-08 09:01:00', '2026-06-08 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2072, 2008, 3, 'to', 0, NULL, 'sent', NULL, '2026-06-08 09:01:00', '2026-06-08 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2081, 2009, 3, 'to', 1, '2026-06-09 16:00:00', 'sent', NULL, '2026-06-09 14:01:00', '2026-06-09 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2082, 2009, 5, 'to', 0, NULL, 'sent', NULL, '2026-06-09 14:01:00', '2026-06-09 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2091, 2010, 2, 'to', 0, NULL, 'sent', NULL, '2026-06-10 08:01:00', '2026-06-10 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2092, 2010, 3, 'to', 0, NULL, 'sent', NULL, '2026-06-10 08:01:00', '2026-06-10 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_mail_recipient` VALUES (2093, 2010, 4, 'cc', 0, NULL, 'sent', NULL, '2026-06-10 08:01:00', '2026-06-10 08:00:00', '2026-06-12 01:36:45');

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
  `recorder_id` bigint(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_meeting
-- ----------------------------
INSERT INTO `oa_meeting` VALUES (1008, 'mysql-fee-smoke-ok', 1, '2026-10-14 09:00:00', '2026-10-14 10:00:00', 2, 8, 'indoor', 'department', 300.00, NULL, NULL, NULL, 0, 'pending_dept', '2026-05-25 07:42:01', NULL, 100.00, 200.00, NULL, 0, NULL, NULL);
INSERT INTO `oa_meeting` VALUES (2001, '2026年春季学期教学工作会议', 1, '2026-06-15 09:00:00', '2026-06-15 11:30:00', 2, 60, '室内', '国内管理会议', 3000.00, NULL, NULL, NULL, 0, 'draft', '2026-06-01 10:00:00', 1000.00, 1500.00, 500.00, 0.00, 0, NULL, NULL);
INSERT INTO `oa_meeting` VALUES (2002, '信息科学技术学院学术委员会例会', 3, '2026-06-16 14:00:00', '2026-06-16 16:00:00', 2, 80, '室内', '国内管理会议', 2000.00, NULL, NULL, NULL, 0, 'pending_dept', '2026-06-02 11:00:00', 500.00, 1000.00, 500.00, 0.00, 0, NULL, NULL);
INSERT INTO `oa_meeting` VALUES (2003, '2026年校园大型招聘会', 4, '2026-06-20 08:00:00', '2026-06-20 17:00:00', 2, 1500, '室外', '国内业务会议', 50000.00, NULL, NULL, NULL, 1, 'pending_security', '2026-06-03 09:00:00', 20000.00, 15000.00, 10000.00, 5000.00, 0, NULL, NULL);
INSERT INTO `oa_meeting` VALUES (2004, '中外合作办学项目洽谈会', 2, '2026-06-22 09:00:00', '2026-06-22 17:00:00', 2, 400, '室内', '在华举办的国际会议', 80000.00, NULL, NULL, NULL, 1, 'pending_leader', '2026-06-04 10:00:00', 35000.00, 25000.00, 15000.00, 5000.00, 0, NULL, NULL);
INSERT INTO `oa_meeting` VALUES (2005, '研究生培养方案修订研讨会', 1, '2026-06-18 14:00:00', '2026-06-18 17:00:00', 2, 50, '室内', '国内管理会议', 2500.00, NULL, NULL, NULL, 0, 'approved', '2026-06-05 09:00:00', 800.00, 1200.00, 500.00, 0.00, 48, NULL, NULL);
INSERT INTO `oa_meeting` VALUES (2006, '2026年度科研工作总结会', 3, '2026-06-19 09:00:00', '2026-06-19 12:00:00', 2, 100, '室内', '国内管理会议', 3500.00, NULL, NULL, NULL, 0, 'minutes_pending', '2026-06-06 08:00:00', 1000.00, 1800.00, 700.00, 0.00, 95, '会议讨论了2026年度科研工作总体情况，审议通过以下决议：一、新增国家级科研项目12项；二、发表高水平论文数量同比增长15%；三、专利申请数量创历史新高。', 3);
INSERT INTO `oa_meeting` VALUES (2007, '学科建设规划座谈会', 1, '2026-06-10 09:00:00', '2026-06-10 11:00:00', 2, 40, '室内', '国内管理会议', 2000.00, NULL, NULL, NULL, 0, 'minutes_confirmed', '2026-06-07 10:00:00', 500.00, 1000.00, 500.00, 0.00, 38, '会议围绕学科建设规划进行了深入讨论，形成了学科发展五年规划建议稿。与会专家一致认为应重点发展人工智能和量子计算等前沿学科。', 3);
INSERT INTO `oa_meeting` VALUES (2008, '教职工代表大会', 2, '2026-05-20 09:00:00', '2026-05-20 17:00:00', 2, 450, '室内', '国内管理会议', 15000.00, NULL, NULL, NULL, 0, 'archived', '2026-05-10 09:00:00', 5000.00, 8000.00, 2000.00, 0.00, 420, '教职工代表大会审议通过了2025年度工作报告、财务决算报告和2026年度预算方案。', 3);
INSERT INTO `oa_meeting` VALUES (2009, '新学期开学典礼筹备会', 4, '2026-08-25 09:00:00', '2026-08-25 11:00:00', 2, 1800, '室内', '国内业务会议', 120000.00, NULL, NULL, NULL, 1, 'rejected', '2026-06-08 14:00:00', 50000.00, 40000.00, 20000.00, 10000.00, 0, NULL, NULL);
INSERT INTO `oa_meeting` VALUES (2010, '人工智能与教育创新论坛', 2, '2026-06-25 09:00:00', '2026-06-25 17:00:00', 2, 500, '室内', '在华举办的国际会议', 95000.00, NULL, NULL, NULL, 1, 'approved', '2026-06-09 08:00:00', 40000.00, 30000.00, 20000.00, 5000.00, 0, NULL, NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
-- Table structure for oa_meeting_participant
-- ----------------------------
DROP TABLE IF EXISTS `oa_meeting_participant`;
CREATE TABLE `oa_meeting_participant`  (
  `id` bigint(0) NOT NULL,
  `meeting_id` bigint(0) NOT NULL,
  `user_id` bigint(0) NOT NULL,
  `is_recorder` tinyint(0) NULL DEFAULT 0,
  `minutes_confirmed` tinyint(0) NULL DEFAULT 0,
  `confirmed_at` datetime(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_meeting_user`(`meeting_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_meeting_participant
-- ----------------------------
INSERT INTO `oa_meeting_participant` VALUES (2001, 2006, 2, 0, 0, NULL, '2026-06-06 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2002, 2006, 3, 1, 0, NULL, '2026-06-06 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2003, 2006, 4, 0, 0, NULL, '2026-06-06 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2004, 2006, 5, 0, 0, NULL, '2026-06-06 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2011, 2007, 2, 0, 1, '2026-06-10 14:00:00', '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2012, 2007, 3, 1, 1, '2026-06-10 13:30:00', '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2013, 2007, 4, 0, 1, '2026-06-10 14:30:00', '2026-06-07 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2021, 2008, 2, 0, 1, '2026-05-20 18:00:00', '2026-05-10 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2022, 2008, 3, 1, 1, '2026-05-20 17:30:00', '2026-05-10 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2023, 2008, 4, 0, 1, '2026-05-20 18:30:00', '2026-05-10 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2024, 2008, 5, 0, 1, '2026-05-20 18:15:00', '2026-05-10 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2031, 2005, 2, 0, 0, NULL, '2026-06-05 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2032, 2005, 3, 0, 0, NULL, '2026-06-05 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2033, 2005, 4, 0, 0, NULL, '2026-06-05 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2041, 2010, 2, 0, 0, NULL, '2026-06-09 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2042, 2010, 3, 0, 0, NULL, '2026-06-09 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2043, 2010, 4, 0, 0, NULL, '2026-06-09 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2044, 2010, 5, 0, 0, NULL, '2026-06-09 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `oa_meeting_participant` VALUES (2045, 2010, 6, 0, 0, NULL, '2026-06-09 08:00:00', '2026-06-12 01:36:45');

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
INSERT INTO `oa_report` VALUES (2001, '关于增设人工智能本科专业的请示', '请示', '公开', 2, 4, '根据国家人工智能发展战略和学校学科建设需要，信息科学技术学院拟申请增设人工智能本科专业。该专业计划每年招收60名学生，学制四年，授予工学学士学位。课程体系涵盖机器学习、深度学习、计算机视觉、自然语言处理等核心课程。', NULL, 'draft', '2026-06-01 09:00:00');
INSERT INTO `oa_report` VALUES (2002, '关于申请购置大型计算服务器的请示', '请示', '内部', 2, 4, '为满足科研计算需求，信息科学技术学院拟购置高性能计算服务器一套，预算约200万元。该设备将用于支撑人工智能、大数据等领域的科研工作，预计可满足未来5年的计算需求。', NULL, 'pending_secret_review', '2026-06-02 10:00:00');
INSERT INTO `oa_report` VALUES (2003, '关于举办国际学术研讨会的请示', '请示', '公开', 2, 4, '信息科学技术学院拟于2026年10月举办\"人工智能与未来教育\"国际学术研讨会，邀请国内外知名学者约200人参会。预计经费约50万元，拟从学院科研经费中列支。', NULL, 'pending_dept', '2026-06-03 11:00:00');
INSERT INTO `oa_report` VALUES (2004, '关于调整研究生招生计划的请示', '请示', '秘密', 2, 4, '鉴于学院师资力量增强和科研需求扩大，拟申请将2027年研究生招生计划从150人调整至180人，其中博士研究生从30人增加至40人。新增名额主要用于人工智能和网络安全方向。', NULL, 'pending_leader', '2026-06-04 14:00:00');
INSERT INTO `oa_report` VALUES (2005, '关于学院年度工作总结的报告', '报告', '公开', 2, 4, '2025年度信息科学技术学院各项工作进展顺利：一、教学质量稳步提升，学生满意度达92%；二、科研工作取得突破，获批国家级项目15项；三、师资队伍建设成效显著，新增长江学者1名；四、国际交流合作深入开展，与10所海外高校建立合作关系。', NULL, 'approved', '2026-06-05 09:00:00');
INSERT INTO `oa_report` VALUES (2006, '关于申请建设新实验楼的请示', '请示', '公开', 2, 4, '鉴于学院实验室面积严重不足，现有实验室已无法满足教学和科研需求。拟申请建设新实验楼一栋，建筑面积约10000平方米，预计投资约5000万元。', NULL, 'rejected', '2026-06-06 10:00:00');
INSERT INTO `oa_report` VALUES (2007, '关于实验室安全检查情况的报告', '报告', '内部', 2, 4, '根据学校统一部署，学院于2026年5月组织了实验室安全专项检查。共检查实验室45间，发现安全隐患8处，已全部整改完毕。建议建立实验室安全管理长效机制。', NULL, 'draft', '2026-06-07 08:00:00');
INSERT INTO `oa_report` VALUES (2008, '关于人才引进工作进展的报告', '报告', '机密', 2, 4, '2026年上半年人才引进工作进展如下：已签约教授2名（其中海外高层次人才1名），副教授3名，博士后5名。目前还有3名候选人正在洽谈中。', '同意报告内容。人才引进工作成效显著，请继续加大力度，特别关注海外优秀青年学者的引进。', 'approved', '2026-06-08 09:00:00');
INSERT INTO `oa_report` VALUES (2009, '关于申请专项经费的请示', '请示', '内部', 2, 4, '为推进学院\"双一流\"建设工作，拟申请专项经费300万元，用于学科平台建设、人才队伍培养和国际交流合作。', NULL, 'pending_dept', '2026-06-09 10:00:00');
INSERT INTO `oa_report` VALUES (2010, '关于本科教学质量评估的报告', '报告', '公开', 2, 4, '根据教育部本科教学质量评估要求，学院完成了自评自建工作。评估结果显示，学院教学质量整体优良，在师资队伍、教学资源、培养过程等方面均达到优秀标准。', '报告内容详实，反映了学院教学工作的实际情况。建议进一步加强实践教学环节建设。', 'approved', '2026-06-10 09:00:00');

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
  `retention_until` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oa_seal_log
-- ----------------------------
INSERT INTO `oa_seal_log` VALUES (1071, 2, 2, 'MySQL用印材料持久化复测', NULL, 1, 0, '常规事项', NULL, NULL, 'pending_dept', '2026-05-26 01:04:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oa_seal_log` VALUES (1080, 2, 2, 'MySQL用印材料持久化复测', NULL, 1, 0, '常规事项', NULL, NULL, 'draft', '2026-05-26 01:04:52', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oa_seal_log` VALUES (2001, 2, 2, '学生毕业证书盖章', '/uploads/seal/biye.pdf', 50, 0, '常规事项', NULL, NULL, 'draft', '2026-06-01 08:30:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oa_seal_log` VALUES (2002, 2, 2, '科研项目合同用印', '/uploads/seal/hetong.pdf', 3, 0, '常规事项', NULL, NULL, 'pending_dept', '2026-06-02 09:00:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oa_seal_log` VALUES (2003, 1, 2, '与教育部联合发文用印', '/uploads/seal/lianhe.pdf', 5, 0, '常规事项', NULL, NULL, 'pending_office', '2026-06-03 10:00:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oa_seal_log` VALUES (2004, 1, 2, '对外合作协议签署用印', '/uploads/seal/hezuo.pdf', 2, 0, '重大事项', NULL, NULL, 'pending_leader', '2026-06-04 11:00:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oa_seal_log` VALUES (2005, 2, 2, '学位授予决定盖章', '/uploads/seal/xuewei.pdf', 10, 0, '常规事项', '2026-06-05 14:00:00', NULL, 'approved', '2026-06-04 15:00:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oa_seal_log` VALUES (2006, 2, 2, '教职工录用通知盖章', '/uploads/seal/luyong.pdf', 8, 0, '常规事项', '2026-06-06 09:30:00', NULL, 'used', '2026-06-05 10:00:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oa_seal_log` VALUES (2007, 1, 2, '招投标文件用印', '/uploads/seal/zhaobiao.pdf', 3, 0, '一般事项', '2026-06-06 10:00:00', '2026-06-06 16:00:00', 'returned', '2026-06-05 14:30:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oa_seal_log` VALUES (2008, 2, 2, '学术论文授权书盖章', '/uploads/seal/lunwen.pdf', 1, 0, '常规事项', NULL, NULL, 'rejected', '2026-06-06 16:00:00', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `oa_seal_log` VALUES (2009, 1, 2, '外带用印参加教育展', '/uploads/seal/jiaoyuzhan.pdf', 5, 1, '一般事项', NULL, NULL, 'approved', '2026-06-07 08:00:00', '参加2026年全国高等教育展览会需现场盖章', '国家会议中心', 3, '2026-06-12 18:00:00', NULL);
INSERT INTO `oa_seal_log` VALUES (2010, 2, 2, '成绩单盖章', '/uploads/seal/chengji.pdf', 100, 0, '常规事项', NULL, NULL, 'pending_dept', '2026-06-08 09:00:00', NULL, NULL, NULL, NULL, NULL);

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
INSERT INTO `oa_travel` VALUES (2001, 2, '上海', '2026-07-01', '2026-07-03', '参加2026年全国人工智能学术大会，进行学术论文宣讲和交流', '二类', '学术交流', '高铁二等座', 3500.00, NULL, 'draft', '2026-06-01 09:00:00', NULL, NULL, 0);
INSERT INTO `oa_travel` VALUES (2002, 2, '广州', '2026-07-10', '2026-07-12', '前往中山大学进行科研项目合作洽谈，商讨联合课题研究方案', '二类', '教学科研业务', '飞机经济舱', 5000.00, NULL, 'pending_dept', '2026-06-02 10:00:00', NULL, NULL, 0);
INSERT INTO `oa_travel` VALUES (2003, 2, '深圳', '2026-07-15', '2026-07-18', '参加华为公司技术交流会议，探讨产学研合作模式', '一类', '教学科研业务', '飞机经济舱', 8000.00, NULL, 'pending_finance', '2026-06-03 11:00:00', NULL, NULL, 0);
INSERT INTO `oa_travel` VALUES (2004, 2, '杭州', '2026-06-20', '2026-06-22', '参加阿里巴巴达摩院开放日活动，了解前沿技术发展', '二类', '学术交流', '高铁一等座', 4500.00, NULL, 'approved', '2026-06-04 09:00:00', NULL, NULL, 0);
INSERT INTO `oa_travel` VALUES (2005, 2, '成都', '2026-06-25', '2026-06-28', '参加西南地区高校信息化建设研讨会', '三类', '行政管理业务', '高铁二等座', 3000.00, NULL, 'rejected', '2026-06-05 14:00:00', NULL, NULL, 0);
INSERT INTO `oa_travel` VALUES (2006, 2, '北京', '2026-05-10', '2026-05-12', '参加教育部组织的课程思政建设工作推进会', '二类', '行政管理业务', '高铁二等座', 2500.00, 2300.00, 'approved', '2026-05-01 09:00:00', '/uploads/travel/baoxiao2006.pdf', NULL, 1);
INSERT INTO `oa_travel` VALUES (2007, 2, '南京', '2026-08-01', '2026-08-05', '参加南京大学主办的国际量子计算研讨会', '一类', '学术交流', '飞机公务舱', 12000.00, NULL, 'draft', '2026-06-06 08:00:00', NULL, NULL, 0);
INSERT INTO `oa_travel` VALUES (2008, 2, '武汉', '2026-07-20', '2026-07-22', '前往武汉大学进行实验教学中心考察学习', '三类', '教学科研业务', '高铁二等座', 2800.00, NULL, 'pending_dept', '2026-06-07 10:00:00', NULL, NULL, 0);
INSERT INTO `oa_travel` VALUES (2009, 2, '西安', '2026-06-15', '2026-06-20', '参加西北地区高校联合科考项目，需携带实验设备', '一类', '教学科研业务', '飞机经济舱', 15000.00, 16500.00, 'approved', '2026-06-01 14:00:00', '/uploads/travel/baoxiao2009.pdf', '因携带大量实验设备，物流费用超支1500元', 1);
INSERT INTO `oa_travel` VALUES (2010, 2, '天津', '2026-07-05', '2026-07-07', '参加京津冀高等教育协同发展论坛', '二类', '行政管理业务', '高铁一等座', 4000.00, NULL, 'pending_finance', '2026-06-08 11:00:00', NULL, NULL, 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
-- Table structure for oms_sequence
-- ----------------------------
DROP TABLE IF EXISTS `oms_sequence`;
CREATE TABLE `oms_sequence`  (
  `id` int(0) NOT NULL DEFAULT 1,
  `next_id` bigint(0) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_sequence
-- ----------------------------
INSERT INTO `oms_sequence` VALUES (1, 5002);

-- ----------------------------
-- Table structure for sys_announcement
-- ----------------------------
DROP TABLE IF EXISTS `sys_announcement`;
CREATE TABLE `sys_announcement`  (
  `id` bigint(0) NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'all',
  `target_dept_id` bigint(0) NULL DEFAULT NULL,
  `pinned` tinyint(0) NULL DEFAULT 0,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'draft',
  `publisher_id` bigint(0) NULL DEFAULT NULL,
  `published_at` datetime(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `updated_at` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_announcement
-- ----------------------------
INSERT INTO `sys_announcement` VALUES (2001, '关于2026年暑假放假安排的通知', '根据学校校历安排，2026年暑假放假时间为7月16日至8月31日。请各部门做好假期值班安排，确保校园安全。', 'notice', 'all', NULL, 0, 'draft', 5, NULL, '2026-06-01 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `sys_announcement` VALUES (2002, '关于开展师德师风专题学习的通知', '根据教育部统一部署，我校将于2026年6月开展师德师风专题学习月活动。请各学院组织教师认真学习相关文件精神。', 'notice', 'all', NULL, 1, 'published', 5, '2026-06-02 08:00:00', '2026-06-02 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `sys_announcement` VALUES (2003, '2026年度国家自然科学基金申报通知', '2026年度国家自然科学基金项目申报工作即将启动，请各位老师提前做好准备。申报截止日期为2026年8月31日。', 'academic', 'all', NULL, 1, 'published', 5, '2026-06-03 10:00:00', '2026-06-03 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `sys_announcement` VALUES (2004, '校园网络升级维护公告', '因校园网络核心设备升级，6月15日22:00至6月16日06:00期间校园网络将暂停服务。请各位师生提前做好相关准备。', 'maintenance', 'all', NULL, 0, 'withdrawn', 5, '2026-06-04 09:00:00', '2026-06-04 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `sys_announcement` VALUES (2005, '关于举办教职工运动会的通知', '学校定于2026年6月28日举办第十五届教职工运动会，欢迎各位教职工踊跃报名参加。', 'activity', 'all', NULL, 0, 'published', 5, '2026-06-05 14:00:00', '2026-06-05 14:00:00', '2026-06-12 01:36:45');
INSERT INTO `sys_announcement` VALUES (2006, '信息科学技术学院学术讲座预告', '特邀清华大学张教授做\"大模型时代的机遇与挑战\"学术报告，时间：6月20日下午2:00，地点：理科一号楼101。', 'academic', 'dept', 4, 0, 'published', 5, '2026-06-06 10:00:00', '2026-06-06 10:00:00', '2026-06-12 01:36:45');
INSERT INTO `sys_announcement` VALUES (2007, '关于规范公务用车管理的通知', '为加强公务用车管理，规范用车申请流程，现就公务用车管理有关事项通知如下：一、实行用车预约制度；二、建立用车台账；三、定期公示用车情况。', 'notice', 'all', NULL, 0, 'published', 5, '2026-06-07 09:00:00', '2026-06-07 09:00:00', '2026-06-12 01:36:45');
INSERT INTO `sys_announcement` VALUES (2008, '暑期校园安全提示', '暑假将至，请各位师生注意出行安全，离开办公室和实验室前务必关闭水电、锁好门窗。', 'notice', 'all', NULL, 0, 'draft', 5, NULL, '2026-06-08 11:00:00', '2026-06-12 01:36:45');
INSERT INTO `sys_announcement` VALUES (2009, '关于开展2026年度考核工作的通知', '根据学校年度考核工作安排，2026年度教职工考核工作将于12月进行。请各部门提前做好考核准备工作。', 'notice', 'all', NULL, 0, 'published', 5, '2026-06-09 08:00:00', '2026-06-09 08:00:00', '2026-06-12 01:36:45');
INSERT INTO `sys_announcement` VALUES (2010, '图书馆开放时间调整公告', '因期末考试季来临，图书馆将延长开放时间至晚上11点，持续至7月15日。', 'maintenance', 'all', NULL, 0, 'withdrawn', 5, '2026-06-10 09:00:00', '2026-06-10 09:00:00', '2026-06-12 01:36:45');

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
INSERT INTO `sys_dict_item` VALUES (289, 'business_status', 'minutes_pending', '纪要待确认', 156, 1, 1, NULL, '2026-06-12 08:02:37', '2026-06-12 08:02:37');
INSERT INTO `sys_dict_item` VALUES (290, 'business_status', 'minutes_confirmed', '纪要已确认', 157, 1, 1, NULL, '2026-06-12 08:02:37', '2026-06-12 08:02:37');

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
INSERT INTO `sys_notification` VALUES (6001, 2, '公文审批通过', '您提交的公文《关于校园基础设施改造的报告》已审批通过', 0, 'document', 2005, '2026-06-05 15:00:00');
INSERT INTO `sys_notification` VALUES (6002, 2, '公文被退回', '您提交的公文《关于调整作息时间的通知》已被部门负责人退回', 1, 'document', 2007, '2026-06-06 16:00:00');
INSERT INTO `sys_notification` VALUES (6003, 2, '用印申请通过', '您提交的用印申请（学位授予决定盖章）已审批通过', 0, 'seal', 2005, '2026-06-04 16:00:00');
INSERT INTO `sys_notification` VALUES (6004, 2, '会议审批通过', '您申请的会议\"研究生培养方案修订研讨会\"已审批通过', 1, 'meeting', 2005, '2026-06-05 10:00:00');
INSERT INTO `sys_notification` VALUES (6005, 2, '差旅申请被退回', '您提交的差旅申请（成都）已被退回', 0, 'travel', 2005, '2026-06-05 15:00:00');
INSERT INTO `sys_notification` VALUES (6006, 3, '待审批：公文', '有一份公文待您审批：《关于开展期末考试安排的决定》', 0, 'document', 2002, '2026-06-02 10:30:00');
INSERT INTO `sys_notification` VALUES (6007, 3, '待审批：会议', '有一份会议申请待您审批：《信息科学技术学院学术委员会例会》', 0, 'meeting', 2002, '2026-06-02 11:00:00');
INSERT INTO `sys_notification` VALUES (6008, 3, '待审批：差旅', '有一份差旅申请待您审批：广州出差', 0, 'travel', 2002, '2026-06-02 10:00:00');
INSERT INTO `sys_notification` VALUES (6009, 3, '待审批：请示报告', '有一份请示报告待您审批：《关于举办国际学术研讨会的请示》', 1, 'report', 2003, '2026-06-03 12:00:00');
INSERT INTO `sys_notification` VALUES (6010, 4, '待审批：公文', '有一份公文待您审批：《关于引进高层次人才的批复》', 0, 'document', 2004, '2026-06-04 10:00:00');
INSERT INTO `sys_notification` VALUES (6011, 4, '待审批：会议', '有一份大型活动会议待您审批：《中外合作办学项目洽谈会》', 0, 'meeting', 2004, '2026-06-04 14:00:00');
INSERT INTO `sys_notification` VALUES (6012, 4, '待审批：请示报告', '有一份请示报告待您审批：《关于调整研究生招生计划的请示》', 0, 'report', 2004, '2026-06-04 16:00:00');
INSERT INTO `sys_notification` VALUES (6013, 5, '待审核：公文', '有一份公文待您审核：《关于召开学术委员会会议的请示》', 0, 'document', 2003, '2026-06-03 15:00:00');
INSERT INTO `sys_notification` VALUES (6014, 5, '待审查：请示报告', '有一份请示报告待保密审查：《关于申请购置大型计算服务器的请示》', 0, 'report', 2002, '2026-06-02 10:00:00');
INSERT INTO `sys_notification` VALUES (6015, 5, '待审核：用印', '有一份用印申请待您审核：《与教育部联合发文用印》', 1, 'seal', 2003, '2026-06-03 10:00:00');
INSERT INTO `sys_notification` VALUES (6016, 6, '待审核：差旅', '有一份差旅申请待您审核：深圳出差', 0, 'travel', 2003, '2026-06-03 14:00:00');
INSERT INTO `sys_notification` VALUES (6017, 6, '待审核：差旅', '有一份差旅申请待您审核：天津出差', 0, 'travel', 2010, '2026-06-08 14:00:00');
INSERT INTO `sys_notification` VALUES (6018, 7, '待审核：会议', '有一份大型活动待您安全审核：《2026年校园大型招聘会》', 0, 'meeting', 2003, '2026-06-03 09:00:00');
INSERT INTO `sys_notification` VALUES (6019, 8, '用印提醒', '有一份用印申请已审批通过，请办理用印：学位授予决定盖章', 0, 'seal', 2005, '2026-06-04 16:00:00');
INSERT INTO `sys_notification` VALUES (6020, 2, '请示报告审批通过', '您提交的请示报告《关于学院年度工作总结的报告》已审批通过', 1, 'report', 2005, '2026-06-05 14:00:00');

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
INSERT INTO `sys_operation_log` VALUES (1097, 2, 'document', 'create', 'document', 1095, '关于办公管理系统试运行的通知', '2026-06-10 23:22:02');
INSERT INTO `sys_operation_log` VALUES (1098, 2, 'document', 'ai_review', 'document', 1095, '密级：公开，结果：通过', '2026-06-10 23:22:05');
INSERT INTO `sys_operation_log` VALUES (1099, 2, 'document', 'ai_review', 'document', 1014, '密级：public，结果：阻断或待修正', '2026-06-10 23:22:36');
INSERT INTO `sys_operation_log` VALUES (1100, 2, 'document', 'ai_review', 'document', 1003, '密级：public，结果：阻断或待修正', '2026-06-10 23:22:42');
INSERT INTO `sys_operation_log` VALUES (5000, 2, 'seal', 'download_attachment', 'seal', 1071, 'oms-seal-material-runtime.pdf', '2026-06-12 07:51:30');
INSERT INTO `sys_operation_log` VALUES (5001, 2, 'document', 'ai_review', 'document', 1014, '密级：public，结果：阻断或待修正', '2026-06-12 07:51:45');
INSERT INTO `sys_operation_log` VALUES (7001, 2, '公文管理', '创建', 'document', 2001, '创建公文草稿：《关于调整教学计划的通知》', '2026-06-01 09:00:00');
INSERT INTO `sys_operation_log` VALUES (7002, 2, '公文管理', '提交', 'document', 2002, '提交公文审批：《关于开展期末考试安排的决定》', '2026-06-02 10:30:00');
INSERT INTO `sys_operation_log` VALUES (7003, 3, '公文管理', '审批通过', 'document', 2003, '部门负责人审批通过：《关于召开学术委员会会议的请示》', '2026-06-03 15:00:00');
INSERT INTO `sys_operation_log` VALUES (7004, 5, '公文管理', '审核通过', 'document', 2004, '党办校办审核通过：《关于引进高层次人才的批复》', '2026-06-04 10:00:00');
INSERT INTO `sys_operation_log` VALUES (7005, 4, '公文管理', '审批通过', 'document', 2005, '校级领导审批通过：《关于校园基础设施改造的报告》', '2026-06-05 15:00:00');
INSERT INTO `sys_operation_log` VALUES (7006, 5, '公文管理', '归档', 'document', 2006, '公文归档：《关于发布年度预算执行情况的函》', '2026-05-16 16:00:00');
INSERT INTO `sys_operation_log` VALUES (7007, 3, '公文管理', '退回', 'document', 2007, '部门负责人退回公文：《关于调整作息时间的通知》', '2026-06-06 16:00:00');
INSERT INTO `sys_operation_log` VALUES (7008, 5, '公文管理', '退回', 'document', 2008, '党办校办退回公文：《关于组织学生暑期社会实践的公告》', '2026-06-07 14:00:00');
INSERT INTO `sys_operation_log` VALUES (7009, 2, '用印管理', '创建', 'seal', 2001, '创建用印申请草稿：学生毕业证书盖章', '2026-06-01 08:30:00');
INSERT INTO `sys_operation_log` VALUES (7010, 8, '用印管理', '确认用印', 'seal', 2006, '确认用印：教职工录用通知盖章', '2026-06-06 09:30:00');
INSERT INTO `sys_operation_log` VALUES (7011, 8, '用印管理', '确认归还', 'seal', 2007, '确认印章归还：招投标文件用印', '2026-06-06 16:00:00');
INSERT INTO `sys_operation_log` VALUES (7012, 2, '会议管理', '创建', 'meeting', 2001, '创建会议草稿：《2026年春季学期教学工作会议》', '2026-06-01 10:00:00');
INSERT INTO `sys_operation_log` VALUES (7013, 2, '会议管理', '提交', 'meeting', 2003, '提交大型活动审批：《2026年校园大型招聘会》', '2026-06-03 09:00:00');
INSERT INTO `sys_operation_log` VALUES (7014, 7, '会议管理', '审核通过', 'meeting', 2010, '保卫部门审核通过：《人工智能与教育创新论坛》', '2026-06-09 09:00:00');
INSERT INTO `sys_operation_log` VALUES (7015, 2, '差旅管理', '创建', 'travel', 2001, '创建差旅申请草稿：上海出差', '2026-06-01 09:00:00');
INSERT INTO `sys_operation_log` VALUES (7016, 6, '差旅管理', '审核通过', 'travel', 2004, '财务审核通过：杭州出差', '2026-06-04 14:00:00');
INSERT INTO `sys_operation_log` VALUES (7017, 2, '请示报告', '创建', 'report', 2001, '创建请示报告草稿：《关于增设人工智能本科专业的请示》', '2026-06-01 09:00:00');
INSERT INTO `sys_operation_log` VALUES (7018, 5, '请示报告', '保密审查通过', 'report', 2005, '保密审查通过：《关于学院年度工作总结的报告》', '2026-06-05 10:00:00');
INSERT INTO `sys_operation_log` VALUES (7019, 2, '邮件中心', '发送', NULL, NULL, '发送邮件：《关于提交期末教学材料的提醒》', '2026-06-01 09:00:00');
INSERT INTO `sys_operation_log` VALUES (7020, 5, '通知公告', '发布', NULL, 2002, '发布公告：《关于开展师德师风专题学习的通知》', '2026-06-02 08:00:00');

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
