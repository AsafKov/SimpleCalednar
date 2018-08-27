package com.example.android.calendar.Fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import com.example.android.calendar.Activities.EventCreatorActivity;
import com.example.android.calendar.Database.EventViewModel;
import com.example.android.calendar.Model.Event;
import com.example.android.calendar.R;
import com.example.android.calendar.Helpers.RecyclerViewAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DayViewFragment extends Fragment {

    private long mDayTimeStamp;

    private FloatingActionButton fab;
    private RecyclerViewAdapter adapter;

    public static EventViewModel mEventViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mDayTimeStamp = getArguments().getLong(CalendarFragment.ARGS_DATE);

        adapter = new RecyclerViewAdapter(getActivity(), new ArrayList<Event>(), this);

        mEventViewModel = ViewModelProviders.of(this).get(EventViewModel.class);
        mEventViewModel.getEventsUnderDay(mDayTimeStamp).observe(this, new Observer<List<Event>>() {
            @Override
            public void onChanged(@Nullable List<Event> events) {
                adapter.updateDataSet(new ArrayList<>(events));
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        super.onCreateView(inflater, parent, savedInstanceState);
        View v = inflater.inflate(R.layout.day_layout, parent, false);

        RecyclerView eventsContainer = v.findViewById(R.id.eventsRecyclerView);
        registerForContextMenu(eventsContainer);
        eventsContainer.setAdapter(adapter);
        eventsContainer.setLayoutManager(new LinearLayoutManager(getActivity()));

        getActivity().setTitle(getDateString());
        initWidgets(v);
        setHasOptionsMenu(true);
        return v;
    }

    private String getDateString(){
        Date date = new Date(mDayTimeStamp);
        return DateFormat.format("EEEE, d MMMM", date).toString();
    }

    // Initialize widgets and their functions
    private void initWidgets(View v){
        fab = v.findViewById(R.id.fabulousFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.setClickable(false);
                Animation fabButtonClickedAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_animation);
                fabButtonClickedAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        adapter.layOffPressedItems();
                        Intent intent = new Intent(getContext().getApplicationContext(), EventCreatorActivity.class);
                        intent.putExtra(RecyclerViewAdapter.EX_TIME_STAMP, mDayTimeStamp);
                        intent.putExtra(RecyclerViewAdapter.EX_EVENT_ID, UUID.randomUUID());
                        startActivity(intent);
                        fab.setClickable(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                fab.startAnimation(fabButtonClickedAnimation);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.menu_main, menu);
        menu.add(getDateString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.deleteEventMenuItem: {
                if (adapter.getPressedItems().isEmpty()) {
                    Toast.makeText(getContext(), R.string.noEventsChosenToast, Toast.LENGTH_LONG).show();
                    return true;
                }
                mEventViewModel.delete(adapter.getPressedItems());
                adapter.layOffPressedItems(); // Leave auto-long-press mode
            }
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
