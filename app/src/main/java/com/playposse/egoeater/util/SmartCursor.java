package com.playposse.egoeater.util;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.playposse.egoeater.storage.ProfileParcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * An improvement over {@link Cursor} that returns values by column name.
 */
public class SmartCursor {

    private final Cursor cursor;
    private final Map<String, Integer> columnNameToIndexMap;

    public SmartCursor(Cursor cursor, String[] columnNames) {
        this.cursor = cursor;

        columnNameToIndexMap = new HashMap<>(columnNames.length);
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            columnNameToIndexMap.put(columnName, i);
        }
    }

    public String getString(String columnName) {
        return cursor.getString(columnNameToIndexMap.get(columnName));
    }

    public int getInt(String columnName) {
        return cursor.getInt(columnNameToIndexMap.get(columnName));
    }

    public double getDouble(String columnName) {
        return cursor.getDouble(columnNameToIndexMap.get(columnName));
    }

    public long getLong(String columnName) {
        return cursor.getLong(columnNameToIndexMap.get(columnName));
    }

    public int getInt(int columnIndex) {
        return cursor.getInt(columnIndex);
    }

    public boolean getBoolean(String columnName) {
        return getInt(columnName) > 0;
    }
}
