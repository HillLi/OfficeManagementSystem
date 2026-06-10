DROP ALL OBJECTS;

CREATE TABLE sys_dept (
  id BIGINT PRIMARY KEY,
  dept_name VARCHAR(100) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  status TINYINT DEFAULT 1
);

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  dept_id BIGINT NOT NULL,
  email VARCHAR(100),
  phone VARCHAR(20),
  status TINYINT DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY,
  role_name VARCHAR(50) NOT NULL,
  role_key VARCHAR(50) NOT NULL UNIQUE,
  level INT DEFAULT 0,
  description VARCHAR(255)
);

CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id)
);

CREATE TABLE sys_dict_type (
  id BIGINT PRIMARY KEY,
  dict_type VARCHAR(60) NOT NULL UNIQUE,
  dict_name VARCHAR(100) NOT NULL,
  system_type TINYINT DEFAULT 0,
  enabled TINYINT DEFAULT 1,
  remark VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_dict_item (
  id BIGINT PRIMARY KEY,
  dict_type VARCHAR(60) NOT NULL,
  dict_code VARCHAR(100) NOT NULL,
  dict_label VARCHAR(100) NOT NULL,
  sort_order INT DEFAULT 0,
  enabled TINYINT DEFAULT 1,
  system_item TINYINT DEFAULT 0,
  remark VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_dict_item_code UNIQUE(dict_type, dict_code)
);

CREATE TABLE oa_document (
  id BIGINT PRIMARY KEY,
  doc_no VARCHAR(50),
  title VARCHAR(255) NOT NULL,
  doc_type VARCHAR(20) NOT NULL,
  urgency VARCHAR(20) DEFAULT '普通',
  secrecy_level VARCHAR(20) DEFAULT '公开',
  knowledge_scope VARCHAR(255),
  content CLOB,
  applicant_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  status VARCHAR(30) NOT NULL,
  version INT DEFAULT 1,
  distribution_status VARCHAR(30) DEFAULT 'not_distributed',
  ai_review_result CLOB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE oa_seal (
  id BIGINT PRIMARY KEY,
  seal_name VARCHAR(100) NOT NULL,
  seal_type VARCHAR(20) NOT NULL,
  dept_id BIGINT NOT NULL,
  keeper_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL
);

CREATE TABLE oa_seal_log (
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
  return_deadline TIMESTAMP,
  retention_until TIMESTAMP,
  use_time TIMESTAMP,
  return_time TIMESTAMP,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE oa_meeting_room (
  id BIGINT PRIMARY KEY,
  room_name VARCHAR(100) NOT NULL,
  capacity INT NOT NULL,
  equipment VARCHAR(255),
  location VARCHAR(255),
  status TINYINT DEFAULT 1
);

CREATE TABLE oa_meeting (
  id BIGINT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  room_id BIGINT NOT NULL,
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP NOT NULL,
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
  minutes CLOB,
  status VARCHAR(30) NOT NULL,
  recorder_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE oa_travel (
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
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE oa_report (
  id BIGINT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  type VARCHAR(10) NOT NULL,
  secrecy_level VARCHAR(20),
  applicant_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  content CLOB,
  reply CLOB,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE oa_approval_history (
  id BIGINT PRIMARY KEY,
  biz_type VARCHAR(20) NOT NULL,
  biz_id BIGINT NOT NULL,
  operator_id BIGINT NOT NULL,
  action VARCHAR(20) NOT NULL,
  opinion VARCHAR(500),
  operated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE oa_document_distribution (
  id BIGINT PRIMARY KEY,
  document_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  receiver_dept_id BIGINT NOT NULL,
  status VARCHAR(30) NOT NULL,
  distributed_at TIMESTAMP,
  received_at TIMESTAMP,
  reminded_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE oa_seal_transfer (
  id BIGINT PRIMARY KEY,
  seal_id BIGINT NOT NULL,
  transferor_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  supervisor_id BIGINT NOT NULL,
  material_url VARCHAR(500) NOT NULL,
  remark VARCHAR(500),
  transfer_time TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_attachment (
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
  deleted_at TIMESTAMP,
  delete_reason VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_operation_log (
  id BIGINT PRIMARY KEY,
  operator_id BIGINT,
  module VARCHAR(50) NOT NULL,
  action VARCHAR(50) NOT NULL,
  biz_type VARCHAR(20),
  biz_id BIGINT,
  detail VARCHAR(1000),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_notification (
  id BIGINT PRIMARY KEY,
  receiver_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  content VARCHAR(1000),
  read_status TINYINT DEFAULT 0,
  biz_type VARCHAR(20),
  biz_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE oa_mail_message (
  id BIGINT PRIMARY KEY,
  sender_id BIGINT NOT NULL,
  subject VARCHAR(255) NOT NULL,
  content CLOB NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE oa_mail_recipient (
  id BIGINT PRIMARY KEY,
  mail_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  recipient_type VARCHAR(10) NOT NULL,
  read_status TINYINT DEFAULT 0,
  read_at TIMESTAMP,
  email_status VARCHAR(20) DEFAULT 'pending',
  email_error VARCHAR(1000),
  email_sent_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_mail_user_type UNIQUE(mail_id, user_id, recipient_type)
);

CREATE TABLE sys_announcement (
  id BIGINT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content CLOB NOT NULL,
  category VARCHAR(30) DEFAULT 'notice',
  target_type VARCHAR(20) DEFAULT 'all',
  target_dept_id BIGINT,
  pinned TINYINT DEFAULT 0,
  status VARCHAR(20) NOT NULL,
  publisher_id BIGINT,
  published_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE oa_flow_instance (
  id BIGINT PRIMARY KEY,
  biz_type VARCHAR(20) NOT NULL,
  biz_id BIGINT NOT NULL,
  current_node_key VARCHAR(50),
  status VARCHAR(30) NOT NULL,
  starter_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_flow_biz UNIQUE(biz_type, biz_id)
);

CREATE TABLE oa_flow_task (
  id BIGINT PRIMARY KEY,
  instance_id BIGINT NOT NULL,
  biz_type VARCHAR(20) NOT NULL,
  biz_id BIGINT NOT NULL,
  node_key VARCHAR(50) NOT NULL,
  approver_role VARCHAR(50),
  approver_id BIGINT,
  status VARCHAR(20) NOT NULL,
  due_time TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE oa_travel_standard (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  staff_level VARCHAR(20) NOT NULL,
  travel_type VARCHAR(30) NOT NULL,
  city_level VARCHAR(30) NOT NULL,
  hotel_limit DECIMAL(10,2) NOT NULL,
  meal_subsidy DECIMAL(10,2) NOT NULL,
  local_transport_subsidy DECIMAL(10,2) NOT NULL,
  CONSTRAINT uk_travel_standard UNIQUE(staff_level, travel_type, city_level)
);

CREATE TABLE oa_meeting_fee_standard (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  meeting_type VARCHAR(50) NOT NULL,
  accommodation_limit DECIMAL(10,2) NOT NULL,
  meal_limit DECIMAL(10,2) NOT NULL,
  other_limit DECIMAL(10,2) NOT NULL,
  total_limit DECIMAL(10,2) NOT NULL,
  CONSTRAINT uk_meeting_fee_standard UNIQUE(meeting_type)
);

CREATE TABLE oms_sequence (
  id INT DEFAULT 1 PRIMARY KEY,
  next_id BIGINT NOT NULL
);

CREATE TABLE oa_meeting_participant (
  id BIGINT PRIMARY KEY,
  meeting_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  is_recorder TINYINT DEFAULT 0,
  minutes_confirmed TINYINT DEFAULT 0,
  confirmed_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_meeting_user UNIQUE(meeting_id, user_id)
);
