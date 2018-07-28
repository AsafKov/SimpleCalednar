package com.example.android.calednar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class DayViewFragment extends Fragment {

    public static final int RC_EDIT = 0;
    public static final int RC_ADD = 1;
    public static final int RC_DELETE = 2;

    // App configuration keys
    public static final String DAY_ID = "dayId";

    // Widgets
    private TextView mDayHeadline;
    private FloatingActionButton fab;

    // Planned day and its activities
    private Day mDay;
    private RecyclerViewAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // Restore data after app-configuration
        if(savedInstanceState != null && !savedInstanceState.isEmpty())
            mDay = Day.findDayById(UUID.fromString(savedInstanceState.getString(DAY_ID)));
        else
            mDay = new Day(Calendar.getInstance().getTime());

        adapter = new RecyclerViewAdapter(getActivity(), new ArrayList<Event>(), this);
        adapter.updateDataSet(mDay.getEvents());
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        super.onCreateView(inflater, parent, savedInstanceState);

        View v = inflater.inflate(R.layout.day_layout, parent, false);
        RecyclerView eventsContainer = v.findViewById(R.id.eventsRecyclerView);
        eventsContainer.setAdapter(adapter);
        eventsContainer.setLayoutManager(new LinearLayoutManager(getActivity()));

        initWidgets(v);
        return v;
    }

    // Save necessary data upon app configuration
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putString(DAY_ID, mDay.getId().toString());

        super.onSaveInstanceState(savedInstanceState);
    }

    // Initialize widgets and their functions
    private void initWidgets(View v){

        mDayHeadline = v.findViewById(R.id.dayHeadline);
        mDayHeadline.setText(mDay.getDate());

        fab = v.findViewById(R.id.fabulousFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext().getApplicationContext(), EventCreatorActivity.class);
                intent.putExtra(RecyclerViewAdapter.EX_DAY_ID, mDay.getId());
                startActivityForResult(intent, RC_ADD);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK)
            return;

        UUID eventId = (UUID)data.getSerializableExtra(EventCreatorFragment.EX_EVENT_ID);
        Event event = mDay.findEventById(eventId);

        switch (requestCode){
            case RC_EDIT:{
                int previousPosition = adapter.getPreviousPosition(eventId);
                int currentPosition = mDay.getIndex(event);
                adapter.updateDataSet(mDay.getEvents());
                if(previousPosition != currentPosition)
                    adapter.notifyItemMoved(previousPosition, mDay.getIndex(event));
                adapter.notifyItemChanged(currentPosition);
            } break;
            case RC_ADD:{
                adapter.updateDataSet(mDay.getEvents());
                adapter.notifyItemInserted(mDay.getIndex(event));
            } break;
        }
    }
}
