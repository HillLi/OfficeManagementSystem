CREATE DATABASE IF NOT EXISTS office_management_system
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE office_management_system;

CREATE TABLE IF NOT EXISTS sys_dept (
  id BIGINT PRIMARY KEY,
  dept_name VARCHAR(100) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  status TINYINT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  dept_id BIGINT NOT NULL,
  email VARCHAR(100),
  phone VARCHAR(20),
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY,
  role_name VARCHAR(50) NOT NULL,
  role_key VARCHAR(50) NOT NULL UNIQUE,
  level INT DEFAULT 0,
  description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_dict_type (
  id BIGINT PRIMARY KEY,
  dict_type VARCHAR(60) NOT NULL UNIQUE,
  dict_name VARCHAR(100) NOT NULL,
  system_type TINYINT DEFAULT 0,
  enabled TINYINT DEFAULT 1,
  remark VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_dict_type_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_dict_item (
  id BIGINT PRIMARY KEY,
  dict_type VARCHAR(60) NOT NULL,
  dict_code VARCHAR(100) NOT NULL,
  dict_label VARCHAR(100) NOT NULL,
  sort_order INT DEFAULT 0,
  enabled TINYINT DEFAULT 1,
  system_item TINYINT DEFAULT 0,
  remark VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_dict_item_code (dict_type, dict_code),
  INDEX idx_dict_item_type_enabled (dict_type, enabled, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_document (
  id BIGINT PRIMARY KEY,
  doc_no VARCHAR(50),
  title VARCHAR(255) NOT NULL,
  doc_type VARCHAR(20) NOT NULL,
  urgency VARCHAR(20) DEFAULT '普通',
  secrecy_level VARCHAR(20) DEFAULT '公开',
  knowledge_scope VARCHAR(255),
  content LONGTEXT,
  applicant_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  status VARCHAR(30) NOT NULL,
  version INT DEFAULT 1,
  distribution_status VARCHAR(30) DEFAULT 'not_distributed',
  ai_review_result JSON,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_seal (
  id BIGINT PRIMARY KEY,
  seal_name VARCHAR(100) NOT NULL,
  seal_type VARCHAR(20) NOT NULL,
  dept_id BIGINT NOT NULL,
  keeper_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_seal_log (
  id BIGINT PRIMARY KEY,
  seal_id BIGINT NOT NULL,
  applicant_id BIGINT NOT NULL,
  purpose VARCHAR(500) NOT NULL,
  material_url VARCHAR(500),
  copies INT DEFAULT 1,
  take_out TINYINT DEFAULT 0,
  matter_level VARCHAR(20),
  take_out_reason VARCHAR(500),
  take_out_location VARCHAR(255),
  supervisor_id BIGINT,
  return_deadline DATETIME,
  retention_until DATETIME,
  use_time DATETIME,
  return_time DATETIME,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_meeting_room (
  id BIGINT PRIMARY KEY,
  room_name VARCHAR(100) NOT NULL,
  capacity INT NOT NULL,
  equipment VARCHAR(255),
  location VARCHAR(255),
  status TINYINT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_meeting (
  id BIGINT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  room_id BIGINT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  organizer_id BIGINT NOT NULL,
  expected_count INT,
  venue_type VARCHAR(20),
  meeting_type VARCHAR(50),
  budget DECIMAL(10,2),
  accommodation_fee DECIMAL(10,2),
  meal_fee DECIMAL(10,2),
  venue_fee DECIMAL(10,2),
  other_fee DECIMAL(10,2),
  risk_report_url VARCHAR(500),
  security_plan_url VARCHAR(500),
  emergency_plan_url VARCHAR(500),
  large_activity TINYINT DEFAULT 0,
  sign_in_count INT DEFAULT 0,
  minutes LONGTEXT,
  status VARCHAR(30) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_travel (
  id BIGINT PRIMARY KEY,
  applicant_id BIGINT NOT NULL,
  destination VARCHAR(100) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  reason VARCHAR(500) NOT NULL,
  staff_level VARCHAR(20),
  travel_type VARCHAR(30),
  transport VARCHAR(50),
  budget DECIMAL(10,2),
  actual_expense DECIMAL(10,2),
  receipt_url VARCHAR(500),
  over_limit_reason VARCHAR(1000),
  reimbursement_submitted TINYINT DEFAULT 0,
  status VARCHAR(30) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_report (
  id BIGINT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  type VARCHAR(10) NOT NULL,
  secrecy_level VARCHAR(20),
  applicant_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  content LONGTEXT,
  reply LONGTEXT,
  status VARCHAR(30) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_approval_history (
  id BIGINT PRIMARY KEY,
  biz_type VARCHAR(20) NOT NULL,
  biz_id BIGINT NOT NULL,
  operator_id BIGINT NOT NULL,
  action VARCHAR(20) NOT NULL,
  opinion VARCHAR(500),
  operated_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_document_distribution (
  id BIGINT PRIMARY KEY,
  document_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  receiver_dept_id BIGINT NOT NULL,
  status VARCHAR(30) NOT NULL,
  distributed_at DATETIME,
  received_at DATETIME,
  reminded_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_document_distribution_doc (document_id),
  INDEX idx_document_distribution_receiver (receiver_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_seal_transfer (
  id BIGINT PRIMARY KEY,
  seal_id BIGINT NOT NULL,
  transferor_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  supervisor_id BIGINT NOT NULL,
  material_url VARCHAR(500) NOT NULL,
  remark VARCHAR(500),
  transfer_time DATETIME NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_seal_transfer_seal (seal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_attachment (
  id BIGINT PRIMARY KEY,
  biz_type VARCHAR(20) NOT NULL,
  biz_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  original_name VARCHAR(255),
  file_url VARCHAR(500) NOT NULL,
  storage_path VARCHAR(1000),
  file_size BIGINT,
  content_type VARCHAR(120),
  secrecy_level VARCHAR(20) DEFAULT '公开',
  uploader_id BIGINT NOT NULL,
  deleted TINYINT DEFAULT 0,
  deleted_by BIGINT,
  deleted_at DATETIME,
  delete_reason VARCHAR(500),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_attachment_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_operation_log (
  id BIGINT PRIMARY KEY,
  operator_id BIGINT,
  module VARCHAR(50) NOT NULL,
  action VARCHAR(50) NOT NULL,
  biz_type VARCHAR(20),
  biz_id BIGINT,
  detail VARCHAR(1000),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_operation_biz (biz_type, biz_id),
  INDEX idx_operation_operator (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_notification (
  id BIGINT PRIMARY KEY,
  receiver_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  content VARCHAR(1000),
  read_status TINYINT DEFAULT 0,
  biz_type VARCHAR(20),
  biz_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_notification_receiver (receiver_id, read_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_announcement (
  id BIGINT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content LONGTEXT NOT NULL,
  category VARCHAR(30) DEFAULT 'notice',
  target_type VARCHAR(20) DEFAULT 'all',
  target_dept_id BIGINT,
  pinned TINYINT DEFAULT 0,
  status VARCHAR(20) NOT NULL,
  publisher_id BIGINT,
  published_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_announcement_status (status, pinned, published_at),
  INDEX idx_announcement_scope (target_type, target_dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_flow_instance (
  id BIGINT PRIMARY KEY,
  biz_type VARCHAR(20) NOT NULL,
  biz_id BIGINT NOT NULL,
  current_node_key VARCHAR(50),
  status VARCHAR(30) NOT NULL,
  starter_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_flow_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_flow_task (
  id BIGINT PRIMARY KEY,
  instance_id BIGINT NOT NULL,
  biz_type VARCHAR(20) NOT NULL,
  biz_id BIGINT NOT NULL,
  node_key VARCHAR(50) NOT NULL,
  approver_role VARCHAR(50),
  approver_id BIGINT,
  status VARCHAR(20) NOT NULL,
  due_time DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_flow_task_biz (biz_type, biz_id),
  INDEX idx_flow_task_status (status, approver_role, approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_travel_standard (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  staff_level VARCHAR(20) NOT NULL,
  travel_type VARCHAR(30) NOT NULL,
  city_level VARCHAR(30) NOT NULL,
  hotel_limit DECIMAL(10,2) NOT NULL,
  meal_subsidy DECIMAL(10,2) NOT NULL,
  local_transport_subsidy DECIMAL(10,2) NOT NULL,
  UNIQUE KEY uk_travel_standard (staff_level, travel_type, city_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS oa_meeting_fee_standard (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  meeting_type VARCHAR(50) NOT NULL,
  accommodation_limit DECIMAL(10,2) NOT NULL,
  meal_limit DECIMAL(10,2) NOT NULL,
  other_limit DECIMAL(10,2) NOT NULL,
  total_limit DECIMAL(10,2) NOT NULL,
  UNIQUE KEY uk_meeting_fee_standard (meeting_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP PROCEDURE IF EXISTS add_column_if_missing;
DELIMITER //
CREATE PROCEDURE add_column_if_missing(
  IN p_table_name VARCHAR(64),
  IN p_column_name VARCHAR(64),
  IN p_definition VARCHAR(255)
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table_name
      AND COLUMN_NAME = p_column_name
  ) THEN
    SET @alter_sql = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN `',
      p_column_name, '` ', p_definition);
    PREPARE alter_stmt FROM @alter_sql;
    EXECUTE alter_stmt;
    DEALLOCATE PREPARE alter_stmt;
  END IF;
END//
DELIMITER ;

CALL add_column_if_missing('oa_document', 'version', 'INT DEFAULT 1');
CALL add_column_if_missing('oa_document', 'distribution_status', 'VARCHAR(30) DEFAULT ''not_distributed''');
CALL add_column_if_missing('oa_seal_log', 'take_out_reason', 'VARCHAR(500)');
CALL add_column_if_missing('oa_seal_log', 'take_out_location', 'VARCHAR(255)');
CALL add_column_if_missing('oa_seal_log', 'supervisor_id', 'BIGINT');
CALL add_column_if_missing('oa_seal_log', 'return_deadline', 'DATETIME');
CALL add_column_if_missing('oa_seal_log', 'retention_until', 'DATETIME');
CALL add_column_if_missing('oa_meeting', 'accommodation_fee', 'DECIMAL(10,2)');
CALL add_column_if_missing('oa_meeting', 'meal_fee', 'DECIMAL(10,2)');
CALL add_column_if_missing('oa_meeting', 'venue_fee', 'DECIMAL(10,2)');
CALL add_column_if_missing('oa_meeting', 'other_fee', 'DECIMAL(10,2)');
CALL add_column_if_missing('oa_meeting', 'sign_in_count', 'INT DEFAULT 0');
CALL add_column_if_missing('oa_meeting', 'minutes', 'LONGTEXT');
CALL add_column_if_missing('oa_travel', 'receipt_url', 'VARCHAR(500)');
CALL add_column_if_missing('oa_travel', 'over_limit_reason', 'VARCHAR(1000)');
CALL add_column_if_missing('oa_travel', 'reimbursement_submitted', 'TINYINT DEFAULT 0');
CALL add_column_if_missing('sys_attachment', 'original_name', 'VARCHAR(255)');
CALL add_column_if_missing('sys_attachment', 'storage_path', 'VARCHAR(1000)');
CALL add_column_if_missing('sys_attachment', 'file_size', 'BIGINT');
CALL add_column_if_missing('sys_attachment', 'content_type', 'VARCHAR(120)');
CALL add_column_if_missing('sys_attachment', 'deleted', 'TINYINT DEFAULT 0');
CALL add_column_if_missing('sys_attachment', 'deleted_by', 'BIGINT');
CALL add_column_if_missing('sys_attachment', 'deleted_at', 'DATETIME');
CALL add_column_if_missing('sys_attachment', 'delete_reason', 'VARCHAR(500)');
CALL add_column_if_missing('sys_attachment', 'updated_at', 'DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');

DROP PROCEDURE IF EXISTS add_column_if_missing;
