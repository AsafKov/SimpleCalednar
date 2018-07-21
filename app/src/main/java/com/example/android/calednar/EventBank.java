package com.example.android.calednar;

import java.util.ArrayList;

// Will be used, hopefully

public class EventBank {

    private static ArrayList<Event> eventBank;

    public EventBank(){
        eventBank = new ArrayList<>();
    }

    public void add(Event activity){
        eventBank.add(activity);
    }
}
