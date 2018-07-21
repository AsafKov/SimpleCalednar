package com.example.android.calednar;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.UUID;

public class DayViewFragment extends Fragment {

    // On-edit event extras
    public final static String EXTRA_DAY_PARENT_ID = "dayParentId";
    public final static String EXTRA_EVENT_ID = "eventId";

    // App configuration keys
    public final static String DAY_ID = "dayId";
    public final static String ID_ARRAY_KEY = "idArrayKey";

    // Widgets
    private TextView mDateHeadline;
    private FloatingActionButton fab;


    // Planned day and its activities
    private Day mDay;
    private ArrayList<Event> mPlannedEvents;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mPlannedEvents = new ArrayList<>();

        // Restore data after app-configuration
        if(savedInstanceState != null && !savedInstanceState.isEmpty()) {
            mDay = Day.findDayById(UUID.fromString(savedInstanceState.getString(DAY_ID)));
            String[] idArray = savedInstanceState.getStringArray(ID_ARRAY_KEY);
            for (int i = 0; i < idArray.length; i++) {
                mPlannedEvents.add(mDay.findEventById(UUID.fromString(idArray[i])));
            }
        }
        else
            mDay = new Day(Calendar.getInstance().getTime());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        super.onCreateView(inflater, parent, savedInstanceState);

        View v = inflater.inflate(R.layout.day_layout, parent, false);
        LinearLayout blocksContainer = v.findViewById(R.id.eventsContainer);

        // Restore view-state after app configuration
        if(savedInstanceState != null){
            for(Event event : mPlannedEvents){
                blocksContainer.addView(setupBlockView(event));
            }
        }
        initWidgets(v);
        return v;
    }


    // Save necessary data upon app configuration
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        String[] idArray = new String[mPlannedEvents.size()];
        LinearLayout eventsContainer = getView().findViewById(R.id.eventsContainer);
        TextView idHolder;
        for(int i=0; i<eventsContainer.getChildCount(); i++){
            idHolder = eventsContainer.getChildAt(i).findViewById(R.id.idHolder);
            idArray[i] = idHolder.getText().toString();
        }
        savedInstanceState.putStringArray(ID_ARRAY_KEY, idArray);
        savedInstanceState.putString(DAY_ID, mDay.getId().toString());

        super.onSaveInstanceState(savedInstanceState);
    }

    // Initialize widgets and their functions
    private void initWidgets(View v){

        mDateHeadline = v.findViewById(R.id.dateHeadline);
        mDateHeadline.setText(mDay.getDate());

        fab = v.findViewById(R.id.fabulousFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EventCreatorActivity.class);
                intent.putExtra(EXTRA_DAY_PARENT_ID, mDay.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        // Determine whatever a new event was add, or one was edited
        if((mPlannedEvents.isEmpty() &&  !mDay.getEvents().isEmpty()) ||
                mDay.getEvents().size() != mPlannedEvents.size()) {
            if(mDay.getEvents().size() < mPlannedEvents.size()) {
                mDay.addEvent(mDay.getEditedEvent());
                editEventBlock(mDay.getEditedEvent());
            }
            else
                addNewEvent();
        }
    }

    // Adds a new event, both to the model and the view
    private void addNewEvent(){
        int eventIndex;

        mPlannedEvents.add(mDay.getEvents().get(mDay.getEvents().size()-1));
        Event newEvent = mDay.getEvents().get(mDay.getEvents().size()-1);
        LinearLayout blocksContainer = getView().findViewById(R.id.eventsContainer);
        eventIndex = mDay.getIndex(newEvent);

        View viewItem = setupBlockView(newEvent);

        if(eventIndex != mDay.getEvents().size()-1)
            insertBlock(blocksContainer, eventIndex, viewItem);
        else
            blocksContainer.addView(viewItem);
    }

    // Change an existing event data. If requires, change the layout to fit the new scheduling
    private void editEventBlock(Event editedEvent){

        int eventIndex;
        TextView textView;

        eventIndex = mDay.getIndex(editedEvent);
        LinearLayout blocksContainer = getView().findViewById(R.id.eventsContainer);
        View eventBlock = blocksContainer.getChildAt(eventIndex);
        View currentActivitySpot = null;

        textView = eventBlock.findViewById(R.id.idHolder);
        if(UUID.fromString(textView.getText().toString()).compareTo(editedEvent.getId()) != 0){
            for(int i=0; i<blocksContainer.getChildCount(); i++){
                textView = blocksContainer.getChildAt(i).findViewById(R.id.idHolder);
                if(UUID.fromString(textView.getText().toString()).compareTo(editedEvent.getId()) == 0){
                    currentActivitySpot = blocksContainer.getChildAt(i);
                    blocksContainer.removeViewAt(i);
                    break;
                }
            }
            insertBlock(blocksContainer, eventIndex, currentActivitySpot);
        }

        eventBlock = blocksContainer.getChildAt(eventIndex);
        textView = eventBlock.findViewById(R.id.eventBlockTitle);
        textView.setText(editedEvent.getEventTitle());

        textView = eventBlock.findViewById(R.id.eventBlockDetails);
        textView.setText(editedEvent.getEventDetails());

        textView = eventBlock.findViewById(R.id.eventBlockStartingTime);
        textView.setText(DateFormat.format("HH:mm", editedEvent.getTime()));

        textView = eventBlock.findViewById(R.id.eventBlockDuration);
        textView.setText(editedEvent.getDuration(getContext()));

    }

    // Take an event and creates its event-block view
    private View setupBlockView(Event event){
        View viewItem = getLayoutInflater().inflate(R.layout.event_block_layout, null);
        TextView textView = viewItem.findViewById(R.id.eventBlockTitle);
        textView.setText(event.getEventTitle());

        textView = viewItem.findViewById(R.id.eventBlockDetails);
        textView.setText(event.getEventDetails());

        textView = viewItem.findViewById(R.id.eventBlockStartingTime);
        textView.setText(DateFormat.format("HH:mm", event.getTime()));

        textView = viewItem.findViewById(R.id.eventBlockDuration);
        textView.setText(event.getDuration(getContext()));

        textView = viewItem.findViewById(R.id.idHolder);
        textView.setText(event.getId().toString());

        viewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView idHolder = v.findViewById(R.id.idHolder);
                UUID id = UUID.fromString(idHolder.getText().toString());

                Intent intent = new Intent(getContext(), EventCreatorActivity.class);
                intent.putExtra(EXTRA_DAY_PARENT_ID, mDay.getId());
                intent.putExtra(EXTRA_EVENT_ID, id);

                startActivity(intent);
            }
        });
        return viewItem;
    }

    // Insert an event-block view in a given index, and adjusting the entire view accordingly
    private void insertBlock(LinearLayout blocksContainer, int insertAt, View block){
        LinkedList<View> removedViews = new LinkedList<>();
        while(blocksContainer.getChildAt(insertAt) != null){
            removedViews.add(blocksContainer.getChildAt(insertAt));
            blocksContainer.removeViewAt(insertAt);
        }

        blocksContainer.addView(block);

        while(!removedViews.isEmpty())
            blocksContainer.addView(removedViews.removeFirst());
    }
}
