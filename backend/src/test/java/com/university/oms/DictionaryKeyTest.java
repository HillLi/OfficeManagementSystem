package com.university.oms;

import com.university.oms.model.DictionaryItem;
import com.university.oms.repository.OmsRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DictionaryKeyTest {
    @Test
    void dictionaryItemKeysDoNotCollideWhenTypeOrCodeContainsDelimiter() {
        DictionaryItem northArea = item("campus", "area:north");
        DictionaryItem campusAreaNorth = item("campus:area", "north");

        String northAreaKey = OmsRepository.dictionaryItemKey(northArea.getDictType(), northArea.getDictCode());
        String campusAreaNorthKey = OmsRepository.dictionaryItemKey(campusAreaNorth.getDictType(), campusAreaNorth.getDictCode());

        assertNotEquals(northAreaKey, campusAreaNorthKey);
    }

    @Test
    void dictionaryItemKeyProducesExpectedFormat() {
        String key = OmsRepository.dictionaryItemKey("business_status", "minutes_pending");
        String key2 = OmsRepository.dictionaryItemKey("business_status", "minutes_confirmed");
        assertNotEquals(key, key2);
        assertEquals("15:business_statusminutes_pending", key);
        assertEquals("15:business_statusminutes_confirmed", key2);
    }

    private DictionaryItem item(String type, String code) {
        DictionaryItem item = new DictionaryItem();
        item.setDictType(type);
        item.setDictCode(code);
        return item;
    }
}
