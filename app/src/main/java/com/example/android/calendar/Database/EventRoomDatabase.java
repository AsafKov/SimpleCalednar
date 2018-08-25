package com.example.android.calendar.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.example.android.calendar.Model.Event;

@TypeConverters(EventTypeConverter.class)
@Database(entities = Event.class, version = 2)
public abstract class EventRoomDatabase extends RoomDatabase {

    public abstract EventDao mEventDao();
    private static EventRoomDatabase INSTANCE;

    public static EventRoomDatabase getDataBase(final Context context){
        if(INSTANCE == null){
            synchronized (EventRoomDatabase.class){
                if(INSTANCE == null)
                    INSTANCE = Room.databaseBuilder(context, EventRoomDatabase.class, "events_database").
                            fallbackToDestructiveMigration().build();
            }
        }
        return INSTANCE;
    }
}
