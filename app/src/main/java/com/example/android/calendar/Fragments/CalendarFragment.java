package com.example.android.calendar.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import com.example.android.calendar.Activities.DayViewActivity;
import com.example.android.calendar.Helpers.NotificationJobsManager;
import com.example.android.calendar.R;
import java.util.Calendar;

public class CalendarFragment extends Fragment {

    public static final String ARGS_DATE = "dateArgument";

    private CalendarView mCalendar;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        NotificationJobsManager.createNotificationChannel(getContext().getApplicationContext());
        NotificationJobsManager.loadPendingJobs(getContext().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        super.onCreateView(inflater, parent, savedInstanceState);

        View v = inflater.inflate(R.layout.calendar_layout, null);
        initWidgets(v);

        return v;
    }

    private void initWidgets(View v){
        mCalendar = v.findViewById(R.id.calendarView);

        mCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                mCalendar.setClickable(false);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDayActivity(calendar);
                mCalendar.setClickable(true);
            }
        });
    }

    private void startDayActivity(Calendar time){
        Intent intent = new Intent(getContext().getApplicationContext(), DayViewActivity.class);
        intent.putExtra(ARGS_DATE, time.getTimeInMillis());
        startActivity(intent);
    }
}
