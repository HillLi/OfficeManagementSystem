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

CREATE TABLE IF NOT EXISTS sys_attachment (
  id BIGINT PRIMARY KEY,
  biz_type VARCHAR(20) NOT NULL,
  biz_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_url VARCHAR(500) NOT NULL,
  secrecy_level VARCHAR(20) DEFAULT '公开',
  uploader_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
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
