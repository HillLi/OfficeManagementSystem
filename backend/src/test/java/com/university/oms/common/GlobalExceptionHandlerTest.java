package com.university.oms.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {
    @Test
    void unexpectedExceptionsDoNotExposeInternalMessages() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpected(
                new IllegalStateException("数据库连接密码 secret=123456"));

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("系统繁忙，请稍后重试", response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("secret"));
    }
}
