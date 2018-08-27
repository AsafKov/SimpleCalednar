package com.example.android.calendar.Database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import com.example.android.calendar.Model.Event;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EventRepository {

    private static final String ACTION_INSERT = "actionInsert";
    private static final String ACTION_DELETE = "actionDelete";
    private static final String ACTION_UPDATE = "actionUpdate";

    private EventDao mEventDao;
    private LiveData<List<Event>> mEventsUnderDay;
    private AsyncTaskHandler taskHandler;

    public EventRepository(Application application){
        EventRoomDatabase database = EventRoomDatabase.getDataBase(application);
        mEventDao = database.mEventDao();
    }

    public LiveData<List<Event>> getEventsUnderDay(final long stamp) {
        mEventsUnderDay = mEventDao.getEventsOfDay(stamp);
        return mEventsUnderDay;
    }

    public void insert(Event event){
        taskHandler = new AsyncTaskHandler(mEventDao);
        taskHandler.mAction = ACTION_INSERT;
        taskHandler.execute(event);
    }

    public void delete(Event[] events){
        taskHandler = new AsyncTaskHandler(mEventDao);
        taskHandler.mAction = ACTION_DELETE;
        taskHandler.execute(events);
    }

    public void update(Event event){
        taskHandler = new AsyncTaskHandler(mEventDao);
        taskHandler.mAction = ACTION_UPDATE;
        taskHandler.execute(event);
    }

    private class AsyncTaskHandler extends AsyncTask<Event, String, Void>{

        private EventDao mEventDao;
        private String mAction;

        AsyncTaskHandler(EventDao eventDao){
            mEventDao = eventDao;
        }

        @Override
        protected Void doInBackground(Event... events) {
            switch (mAction){
                case ACTION_INSERT: { mEventDao.insert(events); }
                    break;
                case ACTION_DELETE: { mEventDao.delete(events); }
                    break;
                case ACTION_UPDATE: { mEventDao.update(events);}
                    break;
            }
            return null;
        }
    }
}
