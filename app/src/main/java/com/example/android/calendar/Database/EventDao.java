package com.example.android.calendar.Database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import com.example.android.calendar.Model.Event;
import java.util.List;
import java.util.UUID;

@Dao
public interface EventDao {

    @Insert
    void insert(Event[] events);

    @Update
    void update(Event[] event);

    @Delete
    void delete(Event[] events);

    @Query("SELECT * from events_table WHERE day_parent_stamp = (:stamp) ORDER BY start_time")
    LiveData<List<Event>> getEventsOfDay(long stamp);
}
