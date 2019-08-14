package com.example.RoomDb;

import android.arch.persistence.room.TypeConverter;

import com.example.Models.Source;

public class SourceConverter {
    @TypeConverter
    public static String sourceToString(Source source) {
        return source.getName();
    }

    @TypeConverter
    public static Source stringToSource(String data) {
        Source source = new Source();
        source.setName(data);
        source.setId("");
        return source;
    }
}
