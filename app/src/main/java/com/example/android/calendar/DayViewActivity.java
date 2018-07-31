package com.example.android.calendar;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toolbar;

public class DayViewActivity extends SingleFragmentActivity {

    protected Fragment createFragment(){
        return new DayViewFragment();
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
