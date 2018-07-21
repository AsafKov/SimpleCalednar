package com.example.android.calednar;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Event {

    public final static int START_TIME_KEY = 1;
    public final static int END_TIME_KEY = 2;

    private Day mDayParent;
    private String mEventTitle;
    private String mEventDetails;
    private UUID mId;

    private int mStartAtMinuteInDay;
    private int mEndAtMinuteInDay;

    public Event(Day dayParent){
        mId = UUID.randomUUID();
        mDayParent = dayParent;
    }

    public Event clone(){
        Event cloneEvent = new Event(this.mDayParent);
        cloneEvent.setEventTitle(this.getEventTitle());
        cloneEvent.setEventDetails(this.getEventDetails());

        return cloneEvent;
    }


    public void scheduleEvent(int timeKey, int hourOfDay, int minute){
        if(timeKey == START_TIME_KEY) {
            if(hourOfDay == 24)
                hourOfDay = 0;
            this.mStartAtMinuteInDay = hourOfDay * 60 + minute;
        }

        if(timeKey == END_TIME_KEY) {
            if(hourOfDay == 0)
                hourOfDay = 24;
            if(hourOfDay == 24)
                minute = 0;
            this.mEndAtMinuteInDay = hourOfDay * 60 + minute;
        }
    }

    public String getDuration(Context context){
        String duration = "";

        int hours = (mEndAtMinuteInDay - mStartAtMinuteInDay)/60;
        int minutes = (mEndAtMinuteInDay - mStartAtMinuteInDay)%60;

        if(hours != 0)
            duration = context.getResources().getString(R.string.durationHours, hours);
        if(minutes != 0)
            duration += context.getResources().getString(R.string.durationMinutes, minutes);

        return duration;
    }

    public int getStartTime(){ return this.mStartAtMinuteInDay; }

    public int getEndTime(){ return this.mEndAtMinuteInDay; }

    public String getEventTitle(){
        return this.mEventTitle;
    }

    public void setEventTitle(String mEventTitle){
        this.mEventTitle = mEventTitle;
    }

    public String getEventDetails(){
        return this.mEventDetails;
    }

    public void setEventDetails(String mEventDetails){
        this.mEventDetails = mEventDetails;
    }

    public UUID getId(){
        return this.mId;
    }

    public Date getTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, this.mStartAtMinuteInDay/60);
        calendar.set(Calendar.MINUTE, this.mStartAtMinuteInDay%60);

        return calendar.getTime();
    }
}
