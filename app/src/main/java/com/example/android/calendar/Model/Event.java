package com.example.android.calendar.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.support.annotation.NonNull;

import com.example.android.calendar.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity (tableName = "events_table")
public class Event{

    private static final int INITIAL_DEFAULT_BLOCK_COLOR = R.color.eventBlockBackgroundDefault;

    public static final String[] mLabelsTypes = new String[]{"Social Event",  "Relationship", "Family",
            "Training", "Eating", "Sleeping", "Reading", "Studying", "School-work", "Work", "Arrangements",
            "Meeting", "Break", "Traveling", "Housework", "Internet", "T.V" , "Hobby"};

    public final static int START_TIME_KEY = 201;
    public final static int END_TIME_KEY = 202;

    @NonNull
    @ColumnInfo (name = "day_parent_stamp")
    private long mDayTimeStamp;
    @NonNull
    @ColumnInfo (name = "label")
    private String mLabel;
    @NonNull
    @ColumnInfo (name = "comment")
    private String mComment;
    @NonNull
    @PrimaryKey
    @ColumnInfo (name = "id", typeAffinity = ColumnInfo.TEXT)
    private UUID mId;
    @NonNull
    @ColumnInfo (name = "default_block_color")
    private int mBlockDefaultColor;
    @NonNull
    @ColumnInfo (name = "start_time")
    private int mStartTime;
    @NonNull
    @ColumnInfo (name = "end_time")
    private int mEndTime;

    public Event(long dayTimeStamp){
        mId = UUID.randomUUID();
        mDayTimeStamp = dayTimeStamp;
        mLabel = "Break";
        mBlockDefaultColor = INITIAL_DEFAULT_BLOCK_COLOR;
    }

    public Event clone(){
        Event cloneEvent = new Event(this.mDayTimeStamp);
        cloneEvent.setLabel(this.mLabel);
        cloneEvent.setComment(this.mComment);
        cloneEvent.setBlockDefaultColor(this.mBlockDefaultColor);

        return cloneEvent;
    }

    public void scheduleEvent(int timeKey, int hourOfDay, int minute){
        if(timeKey == START_TIME_KEY) {
            if(hourOfDay == 24)
                hourOfDay = 0;
            this.mStartTime = hourOfDay * 60 + minute;
        }

        if(timeKey == END_TIME_KEY) {
            if(hourOfDay*60 + minute < mStartTime || minute == 0)
                if(hourOfDay == 0)
                    hourOfDay = 24;
            if(hourOfDay == 24)
                minute = 0;
            this.mEndTime = hourOfDay * 60 + minute;
        }
    }

    public void setUntilNextEvent(Event event){
        if(event == null)
            mEndTime = (60*24)-1;
        else
            mEndTime = event.getStartTime();
    }

    public String getDurationInFormat(Context context){
        String duration = "";

        int hours = (mEndTime - mStartTime)/60;
        int minutes = (mEndTime - mStartTime)%60;

        if(hours != 0)
            duration = context.getResources().getString(R.string.durationHours, hours);
        if(minutes != 0)
            duration += context.getResources().getString(R.string.durationMinutes, minutes);

        return duration;
    }

    // Getters-setters
    public int getStartTime(){ return this.mStartTime; }
    public void setStartTime(int startTime){
        this.mStartTime = startTime;
    }
    public void setEndTime(int endTime){
        this.mEndTime = endTime;
    }
    public int getEndTime(){ return this.mEndTime; }
    public String getLabel(){
        return this.mLabel;
    }
    public void setLabel(String mLabel){
        this.mLabel = mLabel;
    }
    public String getComment(){
        return this.mComment;
    }
    public void setComment(String mComment){
        this.mComment = mComment;
    }
    public UUID getId(){
        return this.mId;
    }
    public void setId(UUID mId){ this.mId = mId; }
    public void setBlockDefaultColor(int defaultColor){
        mBlockDefaultColor = defaultColor;
    }
    public int getBlockDefaultColor(){return this.mBlockDefaultColor; }
    public Date getTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, this.mStartTime/60);
        calendar.set(Calendar.MINUTE, this.mStartTime%60);

        return calendar.getTime();
    }
    public long getDayTimeStamp(){ return this.mDayTimeStamp; }

}
