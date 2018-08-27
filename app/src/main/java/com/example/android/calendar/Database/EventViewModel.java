package com.example.android.calendar.Database;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.util.Log;
import com.example.android.calendar.Fragments.DayViewFragment;
import com.example.android.calendar.Model.Event;
import com.example.android.calendar.Helpers.NotificationJobsManager;
import com.example.android.calendar.Services.NotificationJobService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class EventViewModel extends AndroidViewModel {

    private static final String TAG = "EventViewModel";

    public static final String ERR_INVALID_TIME = "invalidTimeError";
    public static final String ERR_REQUIRE_OVERWRITE = "requireOverwriteError";
    public static final String VALID_DATA = "validData";

    private EventRepository mEventRepository;
    private LiveData<List<Event>> mEventsUnderDay;
    private Event mThisEvent;

    private long mTimeStamp;
    private Calendar mStartCalendar = Calendar.getInstance();
    private Calendar mEndCalendar = Calendar.getInstance();
    private String mEventLabel;
    private String mEventComment;
    private int mBlockDefaultColor;
    private int mNotificationDelay = -1;

    private ArrayList<Event> intersectingEvents;
    private boolean mIsNewEvent;

    public EventViewModel(@NonNull Application application){
        super(application);
        mEventRepository = new EventRepository(application);
    }

    public LiveData<List<Event>> getEventsUnderDay(long stamp) {
        mEventsUnderDay = mEventRepository.getEventsUnderDay(stamp);
        return mEventsUnderDay;
    }

    // Saving the changes that have been done in the UI to the event itself, before changing the database
    public void changeForCurrentEvent(){
        mThisEvent.setLabel(mEventLabel);
        mThisEvent.setComment(mEventComment);
        mThisEvent.scheduleEvent(Event.START_TIME_KEY, mStartCalendar.get(Calendar.HOUR_OF_DAY), mStartCalendar.get(Calendar.MINUTE));
        mThisEvent.scheduleEvent(Event.END_TIME_KEY, mEndCalendar.get(Calendar.HOUR_OF_DAY), mEndCalendar.get(Calendar.MINUTE));
        mThisEvent.setBlockDefaultColor(mBlockDefaultColor);
        mThisEvent.setNotificationDelay(mNotificationDelay);

        if(mIsNewEvent)
            insert(mThisEvent);
        else
            update(mThisEvent);

    }

    public void insert(Event event){
        enqueueNotificationJob(event);
        mEventRepository.insert(event);
    }

    public void delete(ArrayList<Event> events){
        Event[] eventsArray = new Event[events.size()];
        for(int i=0; i<events.size(); i++) {
            eventsArray[i] = events.get(i);
            deleteNotificationJob(events.get(i));
        }

        mEventRepository.delete(eventsArray);
    }

    public void update(Event event){
        enqueueNotificationJob(event);
        mEventRepository.update(event);
    }

    /***
     * Functions responsible for preparing the event before saving into the database, notifying the
     * user if there are issues that need fixing.
     * validateInfo() -   Checks if the data is valid and if the event doesn't conflict with other events.
     *                    Notifies the user on issues, or moving on for insertion or updating.
     * findIntersectingEvents() -   Used by validateInfo() to determine if the current event time clashes
     *                    with previously created events.
     * overwrite -        Deletes and edits conflicting events (under the user's permission)
     */

    public String validateInfo(){
        if(mEndCalendar.before( mStartCalendar))
            return ERR_INVALID_TIME;

        findIntersectingEvents();
        if(!intersectingEvents.isEmpty())
            return ERR_REQUIRE_OVERWRITE;
        return VALID_DATA;
    }

    public void findIntersectingEvents(){
        int startTime = mStartCalendar.get(Calendar.HOUR_OF_DAY)*60 + mStartCalendar.get(Calendar.MINUTE);
        int endTime = mEndCalendar.get(Calendar.HOUR_OF_DAY)*60 + mEndCalendar.get(Calendar.MINUTE);
        intersectingEvents = new ArrayList<>();
        for(Event e : mEventsUnderDay.getValue()){
           if(e.getId().compareTo(mThisEvent.getId()) != 0 &&
                   !(e.getStartTime() >= endTime || e.getEndTime() <= startTime))
               intersectingEvents.add(e);
        }
    }

    public void overwrite(){
        int startTime = mStartCalendar.get(Calendar.HOUR_OF_DAY)*60 + mStartCalendar.get(Calendar.MINUTE);
        int endTime = mEndCalendar.get(Calendar.HOUR_OF_DAY)*60 + mEndCalendar.get(Calendar.MINUTE);
        ArrayList<Event> eventsForRemoval = new ArrayList<>();
        for(Event e : intersectingEvents){
            if(e.getStartTime() >= startTime && e.getEndTime() <= endTime)
                eventsForRemoval.add(e);
        }
        delete(eventsForRemoval);
        intersectingEvents.removeAll(eventsForRemoval);

        switch (intersectingEvents.size()){
            case 0: break;
            case 1: {
                    Event eventToReschedule = intersectingEvents.get(0);
                    if(eventToReschedule.getStartTime() < startTime && eventToReschedule.getEndTime() > endTime) {
                        Event splitEvent = eventToReschedule.clone();
                        splitEvent.setStartTime(endTime);
                        splitEvent.setEndTime(eventToReschedule.getEndTime());
                        eventToReschedule.setEndTime(startTime);
                        update(eventToReschedule);
                        insert(splitEvent);
                    }
                    else{
                        if(eventToReschedule.getStartTime() < startTime)
                            eventToReschedule.setEndTime(startTime);
                        else
                            eventToReschedule.setStartTime(endTime);
                        update(eventToReschedule);
                    }
            } break;
            case 2: {
                    intersectingEvents.get(0).setEndTime(startTime);
                    intersectingEvents.get(1).setStartTime(endTime);
                    update(intersectingEvents.get(0));
                    update(intersectingEvents.get(1));
            } break;
        }
        intersectingEvents.clear();
    }

    /***
     * Functions involving setting the right time in the UI & variables and helping adjust the calendars
     * adjustCalendar() -   Called upon entering creation\editing of an event, determines the state of its calendars
     * lastEventEndTime() - Returns the ending time of the last event chronologically,
     *                      to set the next event as a followup
     * readjustEndTime() - Readjusts the end-time calendar upon start-time changes, if the end-time
     *                      is set to "Until next event" in the UI
     * getNextEvent() -     Used by readjustEndTime() to determine the new end-time
     */

    public void adjustCalendars(){
        int startingTime = 0;
        int endingTime = 0;
        int lastEventEndTime;
        if(!mIsNewEvent){
            startingTime = mThisEvent.getStartTime();
            endingTime = mThisEvent.getEndTime();
            mStartCalendar.set(Calendar.HOUR_OF_DAY, (startingTime/60)%24);
            mStartCalendar.set(Calendar.MINUTE, startingTime%60);
        }
        else{
            if(!mEventsUnderDay.getValue().isEmpty()){
                lastEventEndTime = DayViewFragment.mEventViewModel.lastEventEndTime();
                startingTime = lastEventEndTime;
                mStartCalendar.set(Calendar.HOUR_OF_DAY, (startingTime/60)%24);
                mStartCalendar.set(Calendar.MINUTE, startingTime%60);
            }
        }
        endingTime = endingTime == 0? readjustEndTime(): endingTime;
        mStartCalendar.set(Calendar.HOUR_OF_DAY, (startingTime/60)%24);
        mStartCalendar.set(Calendar.MINUTE, startingTime%60);
        mEndCalendar.set(Calendar.HOUR_OF_DAY, endingTime/60);
        mEndCalendar.set(Calendar.MINUTE, endingTime%60);
    }

    public int lastEventEndTime(){
        List<Event> events = mEventsUnderDay.getValue();
        return events.get(events.size()-1).getEndTime();
    }

    public int readjustEndTime(){
        Event event = getNextEvent();
        int endTime = event == null? (24*60)-1:event.getStartTime();
        return endTime;
    }

    public Event getNextEvent(){
        List<Event> events = mEventsUnderDay.getValue();
        int startTime = mStartCalendar.get(Calendar.HOUR_OF_DAY)*60 + mStartCalendar.get(Calendar.MINUTE);
        for(Event e: events){
            if(e.getStartTime() > startTime)
                return e;
        }
        return null;
    }

    // Deleting notifications-jobs
    private void deleteNotificationJob(Event e){
        JobScheduler jobScheduler = (JobScheduler) getApplication().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        try {
            jobScheduler.cancel(NotificationJobsManager.removeJob(e.getId()));
        } catch (NullPointerException exception){
            Log.i(TAG, "Event has no scheduled job");
        }
    }

    // Creating notifications-jobs
    private void enqueueNotificationJob(Event event){
        deleteNotificationJob(event);

        if(event.getDate().getTime() < System.currentTimeMillis())
            return;


        if(mNotificationDelay == -1)
            return;

        JobScheduler jobScheduler = (JobScheduler) getApplication().getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int jobId = (int) System.currentTimeMillis();
        ComponentName componentName = new ComponentName(getApplication().getApplicationContext(), NotificationJobService.class);
        long scheduleFor = event.getDate().getTime() - System.currentTimeMillis() - event.getNotificationDelay()*1000*60;
        PersistableBundle jobData = new PersistableBundle(1);
        jobData.putString(NotificationJobsManager.EX_JOB_EVENT_ID, event.getId().toString());
        jobData.putString(NotificationJobsManager.EX_TITLE, event.getLabel());
        if(event.getComment().length() != 0)
            jobData.putString(NotificationJobsManager.EX_INFO, event.getComment());
        else
            jobData.putString(NotificationJobsManager.EX_INFO, event.getAltComment());

        JobInfo.Builder builder = new JobInfo.Builder(jobId, componentName);
        builder.setPersisted(true);
        builder.setMinimumLatency(scheduleFor);
        builder.setOverrideDeadline(scheduleFor + 1000);
        builder.setExtras(jobData);
        JobInfo jobInfo = builder.build();
        NotificationJobsManager.addJob(jobInfo, event.getId());
        jobScheduler.schedule(jobInfo);
    }

    // Getters-setters
    public void setTimeStamp(long stamp){
        this.mTimeStamp = stamp;
    }
    public void setEventLabel(String eventLabel){
        this.mEventLabel = eventLabel;
    }
    public String getEventLabel(){ return this.mEventLabel; }
    public void setBlockDefaultColor(int defaultColor){
        this.mBlockDefaultColor = defaultColor;
    }
    public int getBlockDefaultColor(){ return this.mBlockDefaultColor; }
    public void setStartTime(int hourOfDay, int minute){
       mStartCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
       mStartCalendar.set(Calendar.MINUTE, minute);
    }
    public Calendar getStartCalendar() {
        return mStartCalendar;
    }
    public void setEndTime(int hourOfDay, int minute){
        mEndCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mEndCalendar.set(Calendar.MINUTE, minute);
    }
    public Calendar getEndCalendar() {
        return mEndCalendar;
    }
    public void setEventComment(String comment){
        this.mEventComment = comment;
    }
    public String getEventComment(){ return mThisEvent.getComment(); }
    public void findEventById(UUID id){
        for(Event e: mEventsUnderDay.getValue()){
            if(e.getId().compareTo(id) == 0) {
                mThisEvent = e;
                mEventLabel = e.getLabel();
                mBlockDefaultColor = e.getBlockDefaultColor();
                mNotificationDelay = e.getNotificationDelay();
                mIsNewEvent = false;
                return;
            }
        }
        mIsNewEvent = true;
        mThisEvent = new Event(mTimeStamp);
        mEventLabel = mThisEvent.getLabel();
        mBlockDefaultColor = mThisEvent.getBlockDefaultColor();
    }
    public boolean isNewEvent(){ return mIsNewEvent; }
    public void setNotificationDelay(int notificationDelay) {
        mNotificationDelay = notificationDelay;
    }
    public int getNotificationDelay(){ return mNotificationDelay; }
}
