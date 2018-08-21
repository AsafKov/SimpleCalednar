package com.example.android.calendar.Model;

import android.support.annotation.Nullable;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

public class Day{

    private static ArrayList<Day> plannedDays = new ArrayList<>();

    private final Date mDate;
    private final UUID mDayId;

    private final static UUID EMPTY_SPOT_UUID = UUID.randomUUID();
    private UUID[] mDayTimeline;

    private ArrayList<Event> mEventsList;

    public Day(Date mDate){
        this.mDate = mDate;
        mDayId = UUID.randomUUID();
        mEventsList = new ArrayList<>();

        mDayTimeline = new UUID[60*24];
        for(int i=0; i<mDayTimeline.length; i++)
            mDayTimeline[i] = EMPTY_SPOT_UUID;

        plannedDays.add(this);
    }

    // If an event is edited, this function should be called after calling removeEvents() on it
    public void addEvent(Event event){
        for(int i=event.getStartTime(); i<event.getEndTime(); i++){
            mDayTimeline[i] = event.getId();
        }

        int position = getIndex(event);
        if(mEventsList.isEmpty() || position == mEventsList.size()+1){
            mEventsList.add(event);
            return;
        }

        LinkedList<Event> saveForReinsertion = new LinkedList<>();
        while(position != mEventsList.size()) {
            saveForReinsertion.add(mEventsList.get(position));
            mEventsList.remove(position);
        }

        mEventsList.add(event);
        while(!saveForReinsertion.isEmpty())
            mEventsList.add(saveForReinsertion.removeFirst());
    }

    public void removeEvents(ArrayList<Event> events){
        for(Event e : events){
            removeEventFromDayTimeline(e);
            mEventsList.remove(e);
        }
    }

    public void removeEvent(Event event){
        for(Event e : mEventsList){
            if(e.getId().compareTo(event.getId()) == 0) {
                removeEventFromDayTimeline(e);
                mEventsList.remove(e);
                break;
            }
        }
        removeEventFromDayTimeline(event);
    }

    public void removeEventFromDayTimeline(Event event){
        if(!mEventsList.contains(event))
            return;
        mEventsList.remove(event);
        for(int i=0; i<mDayTimeline.length; i++){
            if(mDayTimeline[i].compareTo(event.getId()) == 0)
                mDayTimeline[i] = EMPTY_SPOT_UUID;
        }
    }

    public Event findNextEvent(int startTime){
        for(int i=startTime+1; i<mDayTimeline.length; i++){
            if(mDayTimeline[i].compareTo(EMPTY_SPOT_UUID) != 0 &&
                    (startTime != 0 && mDayTimeline[i].compareTo(mDayTimeline[startTime-1]) != 0) &&
                    mDayTimeline[i].compareTo(mDayTimeline[startTime]) != 0)
                return findEventById(mDayTimeline[i]);
        }
        return null;
    }

    public int getIndex(Event event){
        int startAt = event.getStartTime()-1;
        int index = 0;
        UUID currentId = EMPTY_SPOT_UUID;

        for (int i=startAt; i>=0; i--){
            if(mDayTimeline[i].compareTo(event.getId()) == 0)
                break;
            if (mDayTimeline[i].compareTo(currentId) != 0 &&
                    mDayTimeline[i].compareTo(EMPTY_SPOT_UUID) != 0){
                index++;
            }
            currentId = mDayTimeline[i];
        }
        return index;
    }

    public boolean findIntersection(Calendar startTimeCalendar, Calendar endTimeCalendar, UUID eventId){
        int startTime = startTimeCalendar.get(Calendar.HOUR_OF_DAY)*60 + startTimeCalendar.get(Calendar.MINUTE);
        int endTime = endTimeCalendar.get(Calendar.HOUR_OF_DAY)*60 + endTimeCalendar.get(Calendar.MINUTE);
        for(int i=startTime+1; i<endTime; i++){
            if(mDayTimeline[i].compareTo(EMPTY_SPOT_UUID) != 0 &&
                    mDayTimeline[i].compareTo(eventId) != 0)
                return true;
        }
        return false;
    }

    public void overwrite(int startTime, int endTime){
        Event nextEvent = null;
        Event previousEvent = null;
        if(startTime != 0 && mDayTimeline[startTime-1].compareTo(EMPTY_SPOT_UUID) != 0)
            previousEvent = findEventById(mDayTimeline[startTime-1]);
        if(endTime != mDayTimeline.length-1 && mDayTimeline[endTime+1].compareTo(EMPTY_SPOT_UUID) != 0)
            nextEvent = findEventById(mDayTimeline[endTime+1]);

        Event temp;
        boolean nothingToDoHere = false;
        UUID prevId = UUID.randomUUID();
        UUID nextId = UUID.randomUUID();
        if(previousEvent == null && nextEvent != null) {
            nextEvent.setStartTime(endTime);
            nextId = nextEvent.getId();
            nothingToDoHere = true;
        }
        if(previousEvent != null && nextEvent == null) {
            previousEvent.setEndTime(startTime);
            prevId = previousEvent.getId();
            nothingToDoHere = true;
        }
        if(previousEvent == null && nextEvent == null)
            nothingToDoHere = true;

        if(!nothingToDoHere){
            prevId = previousEvent.getId();
            nextId = nextEvent.getId();
        }

        UUID tempId;
        for(int i=startTime; i<endTime; i++) {
            tempId = mDayTimeline[i];
            if(tempId.compareTo(EMPTY_SPOT_UUID) != 0 && tempId.compareTo(prevId) != 0
                    && tempId.compareTo(nextId) != 0){
                removeEvent(findEventById(mDayTimeline[i]));
            }
            mDayTimeline[i] = EMPTY_SPOT_UUID;
        }

        if(nothingToDoHere)
            return;

        if(previousEvent.getId().compareTo(nextEvent.getId()) == 0){
            temp = previousEvent;
            previousEvent = previousEvent.clone();
            previousEvent.setStartTime(temp.getStartTime());
            previousEvent.setEndTime(startTime);
            nextEvent = nextEvent.clone();
            nextEvent.setStartTime(endTime);
            nextEvent.setEndTime(temp.getEndTime());
            removeEvent(temp);
            addEvent(previousEvent);
            addEvent(nextEvent);
        }
        else {
            nextEvent.setStartTime(endTime);
            previousEvent.setEndTime(startTime);
        }
    }

    public Event getLastOnDay(){
        for(int i=mDayTimeline.length-1; i>=0; i--){
            if(mDayTimeline[i].compareTo(EMPTY_SPOT_UUID) != 0)
                return findEventById(mDayTimeline[i]);
        }
        return null;
    }

    public ArrayList<Event> getEvents(){
        return this.mEventsList;
    }

    public UUID getId(){
        return this.mDayId;
    }

    public Event findEventById(UUID id){
        for(Event event : mEventsList){
            if(event.getId().compareTo(id) == 0)
                return event;
        }
        return null;
    }

    public String getDate(){
        return DateFormat.format("EEEE, d MMMM", mDate).toString();
    }

    @Nullable
    public static Day findDayById(UUID Id){
        for(Day d : plannedDays){
            if(d.getId().compareTo(Id) == 0)
                return d;
        }
        return null;
    }
}
