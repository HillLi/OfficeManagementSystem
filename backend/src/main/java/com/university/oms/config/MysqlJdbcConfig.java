package com.university.oms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

@Configuration
public class MysqlJdbcConfig {
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

        // Run migration on MySQL startup only
        if (url.contains("mysql")) {
            try {
                runMigrations(dataSource);
            } catch (Exception e) {
                System.err.println("DB migration warning: " + e.getMessage());
            }
        }

        return dataSource;
    }

    private void runMigrations(DataSource dataSource) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
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

        // Create missing tables
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

        // Execute seed data (dictionary types, dictionary items, etc.)
        // Uses INSERT IGNORE so it's safe to run on every startup.
        // To add new dictionary items, just edit seed-data.sql — no Java change needed.
        executeSeedData(dataSource);
    }

    /**
     * Execute seed-data.sql from classpath.
     * All statements use INSERT IGNORE, so this is idempotent and safe on every startup.
     */
    private void executeSeedData(DataSource dataSource) {
        try {
            ClassPathResource resource = new ClassPathResource("seed-data.sql");
            if (resource.exists()) {
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.setSqlScriptEncoding("UTF-8");
                populator.addScript(resource);
                populator.execute(dataSource);
            }
        } catch (Exception e) {
            System.err.println("Seed data warning: " + e.getMessage());
        }
    }

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

    private void createTableIfNotExists(JdbcTemplate jdbc, String table, String columns) {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS " + table + " (" + columns + ")");
        } catch (Exception e) {
            System.err.println("Create table " + table + ": " + e.getMessage());
        }
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
