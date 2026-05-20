package com.university.oms.design;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObserverPatternTest {
    @Test
    void listenerNotifiedOnStatusChange() {
        StatusChangeNotifier notifier = new StatusChangeNotifier(new ArrayList<>());
        List<String> received = new ArrayList<>();
        notifier.addListener((bizType, bizId, oldStatus, newStatus, operatorId) ->
                received.add(bizType + ":" + oldStatus + "->" + newStatus));

        notifier.notify("document", 1L, "pending_dept", "pending_office", 3L);

        assertEquals(1, received.size());
        assertEquals("document:pending_dept->pending_office", received.get(0));
    }

    @Test
    void multipleListenersAllNotified() {
        StatusChangeNotifier notifier = new StatusChangeNotifier(new ArrayList<>());
        List<String> log1 = new ArrayList<>();
        List<String> log2 = new ArrayList<>();
        notifier.addListener((bizType, bizId, oldStatus, newStatus, operatorId) -> log1.add("called"));
        notifier.addListener((bizType, bizId, oldStatus, newStatus, operatorId) -> log2.add("called"));

        notifier.notify("seal", 2L, "pending_dept", "approved", 3L);

        assertEquals(1, log1.size());
        assertEquals(1, log2.size());
    }
}
