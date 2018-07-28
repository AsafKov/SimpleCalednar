package com.example.android.calednar;

import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

public class Day{

    private static ArrayList<Day> plannedDays = new ArrayList<>();

    final private Date mDate;
    final private UUID mDayId;

    private final static UUID EMPTY_SPOT_UUID = UUID.randomUUID();
    private UUID[] mMinuteLevelEventClassification;

    private ArrayList<Event> mEventsList;

    private Event editedEvent;

    public Day(Date mDate){
        this.mDate = mDate;
        mDayId = UUID.randomUUID();
        mEventsList = new ArrayList<>();

        mMinuteLevelEventClassification = new UUID[60*24];
        for(int i=0; i<mMinuteLevelEventClassification.length; i++)
            mMinuteLevelEventClassification[i] = EMPTY_SPOT_UUID;

        plannedDays.add(this);
    }

    public void addEvent(Event event){
        for(int i=event.getStartTime(); i<event.getEndTime(); i++){
            mMinuteLevelEventClassification[i] = event.getId();
        }

        int position = getIndex(event);
        if(mEventsList.isEmpty() || position == mEventsList.size()){
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

    public void removeEvent(Event[] events){
        for(Event e : events){
            for(Event event : mEventsList){
                if(e.getId().compareTo(event.getId()) == 0) {
                    mEventsList.remove(e);
                    for(int i=0; i<mMinuteLevelEventClassification.length; i++){
                        if(mMinuteLevelEventClassification[i].compareTo(e.getId()) == 0)
                            mMinuteLevelEventClassification[i] = EMPTY_SPOT_UUID;
                    }
                    break;
                }
            }
        }
    }

    public int getIndex(Event event){
        int startAt = event.getStartTime()-1;
        int index = 0;
        UUID currentId = EMPTY_SPOT_UUID;

        for (int i=startAt; i>=0; i--){
            if(mMinuteLevelEventClassification[i].compareTo(event.getId()) == 0)
                break;
            if (mMinuteLevelEventClassification[i].compareTo(currentId) != 0 &&
                    mMinuteLevelEventClassification[i].compareTo(EMPTY_SPOT_UUID) != 0){
                index++;
            }
            currentId = mMinuteLevelEventClassification[i];
        }
        return index;
    }

    public Event findIntersection(Event newEvent){
        for(int i=newEvent.getStartTime(); i<newEvent.getEndTime(); i++){
            if(mMinuteLevelEventClassification[i].compareTo(EMPTY_SPOT_UUID) != 0 &&
                    mMinuteLevelEventClassification[i].compareTo(newEvent.getId()) != 0)
                return findEventById(mMinuteLevelEventClassification[i]);
        }
        return null;
    }

    public Event getEditedEvent(){ return this.editedEvent; }

    public Event getLastOnDay(){
        int latest = Integer.MIN_VALUE;
        Event latestEvent = null;
        for(Event event : mEventsList){
            if(event.getEndTime() > latest) {
                latest = event.getEndTime();
                latestEvent = event;
            }
        }
        return latestEvent;
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
