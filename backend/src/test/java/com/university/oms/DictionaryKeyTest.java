package com.university.oms;

import com.university.oms.model.DictionaryItem;
import com.university.oms.repository.InMemoryDatabase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DictionaryKeyTest {
    @Test
    void dictionaryItemKeysDoNotCollideWhenTypeOrCodeContainsDelimiter() {
        InMemoryDatabase db = new InMemoryDatabase();
        DictionaryItem northArea = item("campus", "area:north");
        DictionaryItem campusAreaNorth = item("campus:area", "north");

        String northAreaKey = db.dictionaryItemKey(northArea.getDictType(), northArea.getDictCode());
        String campusAreaNorthKey = db.dictionaryItemKey(campusAreaNorth.getDictType(), campusAreaNorth.getDictCode());

        assertNotEquals(northAreaKey, campusAreaNorthKey);
        db.dictionaryItems().put(northAreaKey, northArea);
        db.dictionaryItems().put(campusAreaNorthKey, campusAreaNorth);
        assertEquals(2, db.dictionaryItems().size());
        assertSame(northArea, db.dictionaryItems().get(northAreaKey));
        assertSame(campusAreaNorth, db.dictionaryItems().get(campusAreaNorthKey));
    }

    @Test
    void inMemoryBusinessStatusIncludesMeetingMinutesStates() {
        InMemoryDatabase db = new InMemoryDatabase();
        db.init();

        assertEquals("纪要待确认",
                db.dictionaryItems().get(db.dictionaryItemKey("business_status", "minutes_pending")).getDictLabel());
        assertEquals("纪要已确认",
                db.dictionaryItems().get(db.dictionaryItemKey("business_status", "minutes_confirmed")).getDictLabel());
    }

    private DictionaryItem item(String type, String code) {
        DictionaryItem item = new DictionaryItem();
        item.setDictType(type);
        item.setDictCode(code);
        return item;
    }
}
