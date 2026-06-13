package com.university.oms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * MySQL数据源与JDBC配置，包含数据库连接和自动迁移逻辑
 */
@Configuration
public class MysqlJdbcConfig {
    /**
     * 创建数据源，并在连接MySQL时自动执行表结构迁移
     */
    @Bean
    public DataSource dataSource(@Value("${spring.datasource.url}") String url,
                                 @Value("${spring.datasource.username}") String username,
                                 @Value("${spring.datasource.password}") String password,
                                 @Value("${spring.datasource.driver-class-name}") String driverClassName) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);

        // 仅在使用MySQL时执行数据库迁移
        if (url.contains("mysql")) {
            try {
                runMigrations(dataSource);
            } catch (Exception e) {
                System.err.println("DB migration warning: " + e.getMessage());
            }
        }

        return dataSource;
    }

    /**
     * 执行增量迁移：为已有表补充缺失的列，并创建缺失的表
     */
    private void runMigrations(DataSource dataSource) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        // 迁移定义：每项为 {表名, 列名, 列定义}
        String[][] migrations = {
                {"oa_seal_log", "take_out_reason", "VARCHAR(500)"},
                {"oa_seal_log", "take_out_location", "VARCHAR(255)"},
                {"oa_seal_log", "supervisor_id", "BIGINT"},
                {"oa_seal_log", "return_deadline", "DATETIME"},
                {"oa_seal_log", "retention_until", "DATETIME"},
                {"oa_meeting", "accommodation_fee", "DECIMAL(10,2)"},
                {"oa_meeting", "meal_fee", "DECIMAL(10,2)"},
                {"oa_meeting", "venue_fee", "DECIMAL(10,2)"},
                {"oa_meeting", "other_fee", "DECIMAL(10,2)"},
                {"oa_meeting", "sign_in_count", "INT DEFAULT 0"},
                {"oa_meeting", "minutes", "LONGTEXT"},
                {"oa_meeting", "recorder_id", "BIGINT"},
                {"oa_meeting", "large_activity", "TINYINT DEFAULT 0"},
                {"oa_meeting", "risk_report_url", "VARCHAR(500)"},
                {"oa_meeting", "security_plan_url", "VARCHAR(500)"},
                {"oa_meeting", "emergency_plan_url", "VARCHAR(500)"},
                {"oa_travel", "receipt_url", "VARCHAR(500)"},
                {"oa_travel", "over_limit_reason", "VARCHAR(1000)"},
                {"oa_travel", "reimbursement_submitted", "TINYINT DEFAULT 0"},
                {"oa_document", "version", "INT DEFAULT 1"},
                {"oa_document", "distribution_status", "VARCHAR(30) DEFAULT 'not_distributed'"},
                {"oa_document", "ai_review_result", "LONGTEXT"},
                {"sys_attachment", "original_name", "VARCHAR(255)"},
                {"sys_attachment", "storage_path", "VARCHAR(1000)"},
                {"sys_attachment", "file_size", "BIGINT"},
                {"sys_attachment", "content_type", "VARCHAR(120)"},
                {"sys_attachment", "deleted", "TINYINT DEFAULT 0"},
                {"sys_attachment", "deleted_by", "BIGINT"},
                {"sys_attachment", "deleted_at", "DATETIME"},
                {"sys_attachment", "delete_reason", "VARCHAR(500)"},
                {"sys_attachment", "updated_at", "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"},
                {"sys_user", "email", "VARCHAR(100)"},
        };
        for (String[] m : migrations) {
            addColumnIfMissing(jdbc, m[0], m[1], m[2]);
        }

        // 创建缺失的表
        createTableIfNotExists(jdbc, "oms_sequence",
                "id INT DEFAULT 1 PRIMARY KEY, next_id BIGINT NOT NULL");
        createTableIfNotExists(jdbc, "oa_meeting_participant",
                "id BIGINT PRIMARY KEY, meeting_id BIGINT NOT NULL, user_id BIGINT NOT NULL, "
                + "is_recorder TINYINT DEFAULT 0, minutes_confirmed TINYINT DEFAULT 0, confirmed_at DATETIME, "
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                + "CONSTRAINT uk_meeting_user UNIQUE(meeting_id, user_id)");
        createTableIfNotExists(jdbc, "oa_flow_instance",
                "id BIGINT PRIMARY KEY, biz_type VARCHAR(20) NOT NULL, biz_id BIGINT NOT NULL, "
                + "current_node_key VARCHAR(50), status VARCHAR(30) NOT NULL, starter_id BIGINT, "
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                + "CONSTRAINT uk_flow_biz UNIQUE(biz_type, biz_id)");
        createTableIfNotExists(jdbc, "oa_flow_task",
                "id BIGINT PRIMARY KEY, instance_id BIGINT NOT NULL, biz_type VARCHAR(20) NOT NULL, biz_id BIGINT NOT NULL, "
                + "node_key VARCHAR(50) NOT NULL, approver_role VARCHAR(50), approver_id BIGINT, status VARCHAR(20) NOT NULL, "
                + "due_time DATETIME, created_at DATETIME DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        // 审批流程配置表：定义各流程的审批步骤顺序、节点名称和审批角色
        createTableIfNotExists(jdbc, "oa_flow_node",
                "id BIGINT PRIMARY KEY, flow_key VARCHAR(40) NOT NULL, sort_order INT NOT NULL, "
                + "node_key VARCHAR(40) NOT NULL, node_label VARCHAR(100), role_key VARCHAR(40) NOT NULL, "
                + "enabled TINYINT DEFAULT 1, "
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
        // 首次创建时插入默认审批流程配置（若表为空）
        seedFlowNodesIfEmpty(jdbc);
    }

    /**
     * 当 oa_flow_node 表为空时，插入与原硬编码一致的默认审批流程配置
     */
    private void seedFlowNodesIfEmpty(JdbcTemplate jdbc) {
        try {
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM oa_flow_node", Integer.class);
            if (count != null && count > 0) {
                return;
            }
        } catch (Exception e) {
            System.err.println("Seed flow nodes check: " + e.getMessage());
            return;
        }
        // flowKey, nodeKey, nodeLabel, roleKey
        String[][] defaults = {
                {"document", "pending_dept", "部门负责人审批", "dept_head"},
                {"document", "pending_office", "党办校办审核", "office_admin"},
                {"document", "pending_leader", "校级领导签发", "school_leader"},
                {"seal_office", "pending_office", "党办校办审批", "office_admin"},
                {"seal_dept", "pending_dept", "部门负责人审批", "dept_head"},
                {"seal_dept_major", "pending_dept", "部门负责人审批", "dept_head"},
                {"seal_dept_major", "pending_office", "党办校办审核", "office_admin"},
                {"seal_school_major", "pending_office", "党办校办审核", "office_admin"},
                {"seal_school_major", "pending_leader", "校级领导签发", "school_leader"},
                {"meeting", "pending_dept", "部门负责人审批", "dept_head"},
                {"meeting_large", "pending_security", "保卫处安全审批", "security_staff"},
                {"meeting_large", "pending_dept", "部门负责人审批", "dept_head"},
                {"meeting_large", "pending_leader", "校级领导审批", "school_leader"},
                {"travel", "pending_dept", "部门负责人审批", "dept_head"},
                {"travel", "pending_finance", "财务处审批", "finance_staff"},
                {"report", "pending_secret_review", "保密审查", "office_admin"},
                {"report", "pending_dept", "部门负责人审批", "dept_head"},
                {"report", "pending_leader", "校级领导审批", "school_leader"},
        };
        long id = 1;
        int order = 1;
        String prevFlow = null;
        for (String[] d : defaults) {
            if (!d[0].equals(prevFlow)) {
                order = 1;
                prevFlow = d[0];
            } else {
                order++;
            }
            try {
                jdbc.update("INSERT INTO oa_flow_node (id, flow_key, sort_order, node_key, node_label, role_key, enabled) "
                                + "VALUES (?,?,?,?,?,?,1)",
                        id, d[0], order, d[1], d[2], d[3]);
            } catch (Exception e) {
                System.err.println("Seed flow node " + d[0] + "/" + d[1] + ": " + e.getMessage());
            }
            id++;
        }
    }

    /**
     * 检查表中是否存在指定列，不存在则添加
     */
    private void addColumnIfMissing(JdbcTemplate jdbc, String table, String column, String definition) {
        try {
            List<Map<String, Object>> cols = jdbc.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    table, column);
            if (cols.isEmpty()) {
                jdbc.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            }
        } catch (Exception e) {
            System.err.println("Migration " + table + "." + column + ": " + e.getMessage());
        }
    }

    /**
     * 如果表不存在则创建
     */
    private void createTableIfNotExists(JdbcTemplate jdbc, String table, String columns) {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS " + table + " (" + columns + ")");
        } catch (Exception e) {
            System.err.println("Create table " + table + ": " + e.getMessage());
        }
    }

    /** 创建JdbcTemplate实例 */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
