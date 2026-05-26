package com.university.oms;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MysqlSchemaContractTest {
    @Test
    void schemaProvidesTablesAndColumnsRequiredByWorkflowClosures() throws Exception {
        String schema = new String(Files.readAllBytes(Paths.get("sql", "schema.sql")), StandardCharsets.UTF_8);
        String data = new String(Files.readAllBytes(Paths.get("sql", "data.sql")), StandardCharsets.UTF_8);

        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS oa_document_distribution"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS oa_seal_transfer"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS sys_dict_type"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS sys_dict_item"));
        assertTrue(schema.contains("dict_type VARCHAR(60) NOT NULL UNIQUE"));
        assertTrue(schema.contains("UNIQUE KEY uk_dict_item_code (dict_type, dict_code)"));
        assertTrue(schema.contains("distribution_status"));
        assertTrue(schema.contains("version"));
        assertTrue(schema.contains("take_out_reason"));
        assertTrue(schema.contains("accommodation_fee"));
        assertTrue(schema.contains("receipt_url"));
        assertTrue(schema.contains("over_limit_reason"));
        assertTrue(schema.contains("original_name"));
        assertTrue(schema.contains("storage_path"));
        assertTrue(schema.contains("file_size"));
        assertTrue(schema.contains("content_type"));
        assertTrue(schema.contains("deleted_by"));
        assertTrue(schema.contains("deleted_at"));
        assertTrue(schema.contains("delete_reason"));
        assertTrue(schema.contains("CALL add_column_if_missing('sys_attachment', 'storage_path'"));
        assertFalse(schema.contains("ADD COLUMN IF NOT EXISTS"));
        assertTrue(schema.contains("CREATE PROCEDURE add_column_if_missing"));
        assertTrue(schema.contains("CALL add_column_if_missing('oa_meeting', 'sign_in_count'"));
        assertTrue(schema.contains("CALL add_column_if_missing('oa_meeting', 'minutes'"));
        assertTrue(data.contains("'business_status'"));
        assertTrue(data.contains("'pending_dept', '部门负责人审批中'"));
        assertTrue(data.contains("'role_key'"));
        assertTrue(data.contains("'meeting_type'"));
    }

    @Test
    void mysqlInitializationDefaultsMatchApplicationProfile() throws Exception {
        String script = new String(Files.readAllBytes(Paths.get("scripts", "init-mysql.ps1")), StandardCharsets.UTF_8);

        assertTrue(script.contains("[string]$Password = \"123456\""));
    }
}
