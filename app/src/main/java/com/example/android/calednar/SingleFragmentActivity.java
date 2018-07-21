package com.example.android.calednar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public abstract class SingleFragmentActivity extends FragmentActivity {

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
