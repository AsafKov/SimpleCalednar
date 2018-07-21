package com.example.android.calednar;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class EventCreatorActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment(){
        return new EventCreatorFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putInt(FRAGMENT_ID_KEY, getSupportFragmentManager().getFragments().get(0).getId());

        super.onSaveInstanceState(savedInstanceState);
    }
}
