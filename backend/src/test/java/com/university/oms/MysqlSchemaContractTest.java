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

        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS oa_document_distribution"));
        assertTrue(schema.contains("CREATE TABLE IF NOT EXISTS oa_seal_transfer"));
        assertTrue(schema.contains("distribution_status"));
        assertTrue(schema.contains("version"));
        assertTrue(schema.contains("take_out_reason"));
        assertTrue(schema.contains("accommodation_fee"));
        assertTrue(schema.contains("receipt_url"));
        assertTrue(schema.contains("over_limit_reason"));
        assertFalse(schema.contains("ADD COLUMN IF NOT EXISTS"));
        assertTrue(schema.contains("CREATE PROCEDURE add_column_if_missing"));
        assertTrue(schema.contains("CALL add_column_if_missing('oa_meeting', 'sign_in_count'"));
        assertTrue(schema.contains("CALL add_column_if_missing('oa_meeting', 'minutes'"));
    }

    @Test
    void mysqlInitializationDefaultsMatchApplicationProfile() throws Exception {
        String script = new String(Files.readAllBytes(Paths.get("scripts", "init-mysql.ps1")), StandardCharsets.UTF_8);

        assertTrue(script.contains("[string]$Password = \"123456\""));
    }
}
