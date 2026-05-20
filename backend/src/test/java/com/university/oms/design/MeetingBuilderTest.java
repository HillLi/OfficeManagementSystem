package com.university.oms.design;

import com.university.oms.common.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MeetingBuilderTest {
    @Test
    void buildSuccessWithAllFields() {
        com.university.oms.model.Meeting meeting = new MeetingBuilder()
                .title("测试会议")
                .roomId(1L)
                .startTime(LocalDateTime.of(2026, 6, 1, 9, 0))
                .endTime(LocalDateTime.of(2026, 6, 1, 11, 0))
                .organizerId(2L)
                .expectedCount(50)
                .venueType("室内")
                .budget(BigDecimal.valueOf(1000))
                .build();

        assertEquals("测试会议", meeting.getTitle());
        assertEquals(1L, meeting.getRoomId());
        assertEquals(50, meeting.getExpectedCount());
    }

    @Test
    void buildFailsWithoutTitle() {
        assertThrows(BusinessException.class, () -> new MeetingBuilder()
                .roomId(1L)
                .startTime(LocalDateTime.of(2026, 6, 1, 9, 0))
                .endTime(LocalDateTime.of(2026, 6, 1, 11, 0))
                .organizerId(2L)
                .build());
    }

    @Test
    void buildFailsWhenEndTimeBeforeStartTime() {
        assertThrows(BusinessException.class, () -> new MeetingBuilder()
                .title("测试")
                .roomId(1L)
                .startTime(LocalDateTime.of(2026, 6, 1, 11, 0))
                .endTime(LocalDateTime.of(2026, 6, 1, 9, 0))
                .organizerId(2L)
                .build());
    }
}
