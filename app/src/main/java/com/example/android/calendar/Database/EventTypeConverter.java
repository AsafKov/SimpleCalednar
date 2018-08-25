package com.example.android.calendar.Database;

import android.arch.persistence.room.TypeConverter;

import java.util.UUID;

public class EventTypeConverter {

    @TypeConverter
    public static String fromIdToString(UUID id){
        return id.toString();
    }

    @TypeConverter
    public static UUID fromStringToID(String id){
        return UUID.fromString(id);
    }

}
