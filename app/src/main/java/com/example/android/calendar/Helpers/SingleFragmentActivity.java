package com.example.android.calendar.Helpers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import com.example.android.calendar.R;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected final static String FRAGMENT_ID_KEY = "fragmentIdKey";

    protected abstract Fragment createFragment();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;
        if(savedInstanceState != null)
            fragment = fragmentManager.findFragmentById(savedInstanceState.getInt(FRAGMENT_ID_KEY));
        else
            fragment = fragmentManager.findFragmentById(R.id.eventCreator);

        if(fragment == null){
            fragment = createFragment();
            fragmentManager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }
}
