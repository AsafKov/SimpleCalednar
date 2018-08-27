package com.example.android.calendar.Activities;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.example.android.calendar.Fragments.CalendarFragment;
import com.example.android.calendar.Fragments.DayViewFragment;
import com.example.android.calendar.Helpers.SingleFragmentActivity;

import java.util.Calendar;

public class DayViewActivity extends SingleFragmentActivity {

    protected Fragment createFragment(){
        DayViewFragment fragment = new DayViewFragment();
        Bundle args = new Bundle();
        args.putLong(CalendarFragment.ARGS_DATE, getIntent().getLongExtra(CalendarFragment.ARGS_DATE, Calendar.getInstance().getTimeInMillis()));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putInt(FRAGMENT_ID_KEY, getSupportFragmentManager().getFragments().get(0).getId());

        super.onSaveInstanceState(savedInstanceState);
    }
}
