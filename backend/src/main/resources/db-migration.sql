-- Migration script using stored procedure (MySQL 5.7 compatible)

DROP PROCEDURE IF EXISTS add_column_if_missing;
DELIMITER //
CREATE PROCEDURE add_column_if_missing(
  IN p_table_name VARCHAR(64),
  IN p_column_name VARCHAR(64),
  IN p_definition VARCHAR(255)
)
BEGIN
  SET @col_exists = 0;
  SELECT 1 INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table_name AND COLUMN_NAME = p_column_name;
  IF @col_exists = 0 THEN
    SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_name, ' ', p_definition);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END //
DELIMITER ;

-- oa_seal_log
CALL add_column_if_missing('oa_seal_log', 'take_out_reason', 'VARCHAR(500)');
CALL add_column_if_missing('oa_seal_log', 'take_out_location', 'VARCHAR(255)');
CALL add_column_if_missing('oa_seal_log', 'supervisor_id', 'BIGINT');
CALL add_column_if_missing('oa_seal_log', 'return_deadline', 'DATETIME');
CALL add_column_if_missing('oa_seal_log', 'retention_until', 'DATETIME');

-- oa_meeting
CALL add_column_if_missing('oa_meeting', 'accommodation_fee', 'DECIMAL(10,2)');
CALL add_column_if_missing('oa_meeting', 'meal_fee', 'DECIMAL(10,2)');
CALL add_column_if_missing('oa_meeting', 'venue_fee', 'DECIMAL(10,2)');
CALL add_column_if_missing('oa_meeting', 'other_fee', 'DECIMAL(10,2)');
CALL add_column_if_missing('oa_meeting', 'sign_in_count', 'INT DEFAULT 0');
CALL add_column_if_missing('oa_meeting', 'minutes', 'LONGTEXT');

-- oa_travel
CALL add_column_if_missing('oa_travel', 'receipt_url', 'VARCHAR(500)');
CALL add_column_if_missing('oa_travel', 'over_limit_reason', 'VARCHAR(1000)');
CALL add_column_if_missing('oa_travel', 'reimbursement_submitted', 'TINYINT DEFAULT 0');

-- oa_document
CALL add_column_if_missing('oa_document', 'version', 'INT DEFAULT 1');
CALL add_column_if_missing('oa_document', 'distribution_status', 'VARCHAR(30) DEFAULT ''not_distributed''');

-- sys_attachment
CALL add_column_if_missing('sys_attachment', 'original_name', 'VARCHAR(255)');
CALL add_column_if_missing('sys_attachment', 'storage_path', 'VARCHAR(1000)');
CALL add_column_if_missing('sys_attachment', 'file_size', 'BIGINT');
CALL add_column_if_missing('sys_attachment', 'content_type', 'VARCHAR(120)');
CALL add_column_if_missing('sys_attachment', 'deleted', 'TINYINT DEFAULT 0');
CALL add_column_if_missing('sys_attachment', 'deleted_by', 'BIGINT');
CALL add_column_if_missing('sys_attachment', 'deleted_at', 'DATETIME');
CALL add_column_if_missing('sys_attachment', 'delete_reason', 'VARCHAR(500)');
CALL add_column_if_missing('sys_attachment', 'updated_at', 'DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP');

-- sys_user
CALL add_column_if_missing('sys_user', 'email', 'VARCHAR(100)');

-- Create missing tables (CREATE TABLE IF NOT EXISTS is safe)
CREATE TABLE IF NOT EXISTS oa_meeting_participant (
  id BIGINT PRIMARY KEY,
  meeting_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  is_recorder TINYINT DEFAULT 0,
  minutes_confirmed TINYINT DEFAULT 0,
  confirmed_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_meeting_user UNIQUE(meeting_id, user_id)
);

CREATE TABLE IF NOT EXISTS oa_flow_instance (
  id BIGINT PRIMARY KEY,
  biz_type VARCHAR(20) NOT NULL,
  biz_id BIGINT NOT NULL,
  current_node_key VARCHAR(50),
  status VARCHAR(30) NOT NULL,
  starter_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_flow_biz UNIQUE(biz_type, biz_id)
);

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
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS oms_sequence (
  id INT DEFAULT 1 PRIMARY KEY,
  next_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS oa_travel_standard (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  staff_level VARCHAR(20) NOT NULL,
  travel_type VARCHAR(30) NOT NULL,
  city_level VARCHAR(30) NOT NULL,
  hotel_limit DECIMAL(10,2) NOT NULL,
  meal_subsidy DECIMAL(10,2) NOT NULL,
  local_transport_subsidy DECIMAL(10,2) NOT NULL,
  CONSTRAINT uk_travel_standard UNIQUE(staff_level, travel_type, city_level)
);

CREATE TABLE IF NOT EXISTS oa_meeting_fee_standard (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  meeting_type VARCHAR(50) NOT NULL,
  accommodation_limit DECIMAL(10,2) NOT NULL,
  meal_limit DECIMAL(10,2) NOT NULL,
  other_limit DECIMAL(10,2) NOT NULL,
  total_limit DECIMAL(10,2) NOT NULL,
  CONSTRAINT uk_meeting_fee_standard UNIQUE(meeting_type)
);

DROP PROCEDURE IF EXISTS add_column_if_missing;
