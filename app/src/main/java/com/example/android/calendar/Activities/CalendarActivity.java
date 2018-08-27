package com.example.android.calendar.Activities;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.example.android.calendar.Fragments.CalendarFragment;
import com.example.android.calendar.Helpers.SingleFragmentActivity;

public class CalendarActivity extends SingleFragmentActivity {

    protected Fragment createFragment(){
        return new CalendarFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

}
