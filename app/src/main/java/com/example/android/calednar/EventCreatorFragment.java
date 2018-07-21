package com.example.android.calednar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class EventCreatorFragment extends Fragment {

    // Toasts
    private static final String NO_TITLE_TOAST = "Please Insert title";
    private static final String TIME_PARADOX_TOAST = "Your activity starts after it ends. " +
            "Please deliver your time machine to the nearest police station";

    // App configuration keys
    private static final String  KEY_DAY_ID = "dayIdKey";
    private static final String KEY_TITLE = "titleKey";
    private static final String KEY_DETAILS = "detailsKey";
    private static final String KEY_START_HOUR = "startHourKey";
    private static final String KEY_START_MINUTE = "startMinuteKey";
    private static final String KEY_END_HOUR = "endHourKey";
    private static final String KEY_END_MINUTE = "endMinuteKey";


    // widgets
    private EditText mNewEventTitle, mNewEventDetails;
    private TextView mFromTimeTextView, mToTimeTextView;
    private Button mSaveButton;

    // TimePickerDialog tags, codes and keys
    public static final int FROM_TIME_REQUEST_CODE = -1;
    public static final String FROM_TIME_DIALOG_TAG = "fromTimePickerDialog";
    public static final int TO_TIME_REQUEST_CODE = 1;
    public static final String TO_TIME_DIALOG_TAG = "toTimePickerDialog";
    public static final String EXTRA_HOUR_OF_DAY = "hourOfDay";
    public static final String EXTRA_MINUTES = "minutes";

    // OverrideDialog tags and code
    public static final String OVERRIDE_DIALOG_TAG = "overrideDialogTag";
    public static final int OVERRIDE_DIALOG_REQUEST = 2;

    private Day mDayParent;
    private Calendar mStartCalendar = Calendar.getInstance();
    private Calendar mEndCalendar = Calendar.getInstance();

    // If an event is being edited, rather than a new one being created
    private UUID mEventId;
    private Event mThisEvent;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mDayParent = Day.findDayById(((UUID)getActivity().getIntent().getSerializableExtra(DayViewFragment.EXTRA_DAY_PARENT_ID)));
        mEventId = (UUID)getActivity().getIntent().getSerializableExtra(DayViewFragment.EXTRA_EVENT_ID);

        Event lastOnDay = mDayParent.getLastOnDay();

        // If received an event id, set calendar to it's time
        if(mEventId == null) {
            mThisEvent = new Event(mDayParent);
            if(lastOnDay != null) {
                mStartCalendar.set(Calendar.HOUR_OF_DAY, lastOnDay.getEndTime() / 60);
                mStartCalendar.set(Calendar.MINUTE, lastOnDay.getEndTime() % 60);
                mEndCalendar.set(Calendar.HOUR_OF_DAY, mStartCalendar.get(Calendar.HOUR_OF_DAY) + 1);
                mEndCalendar.set(Calendar.MINUTE, mStartCalendar.get(Calendar.MINUTE));
            }
        }
        else {
            mThisEvent = mDayParent.findEventById(mEventId);
            mStartCalendar.set(Calendar.HOUR_OF_DAY, mThisEvent.getStartTime()/60);
            mStartCalendar.set(Calendar.MINUTE, mThisEvent.getStartTime()%60);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.event_creator_layout, parent, false);
        initWidgets(v);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        if(mEventId != null)
            savedInstanceState.putString(KEY_DAY_ID, mDayParent.getId().toString());
        else{
            savedInstanceState.putString(KEY_DETAILS, mThisEvent.getEventDetails());
            savedInstanceState.putString(KEY_TITLE, mThisEvent.getEventTitle());
            savedInstanceState.putString();
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    // Initialize widgets and their functions
    private void initWidgets(View v){
        mNewEventTitle = v.findViewById(R.id.newEventTitle);
        mNewEventTitle.setText(mThisEvent.getEventTitle());
        mNewEventTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mEventId == null)
                    mThisEvent.setEventTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mNewEventDetails = v.findViewById(R.id.newEventDetails);
        mNewEventDetails.setText(mThisEvent.getEventDetails());
        mNewEventDetails.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mEventId == null)
                    mThisEvent.setEventDetails(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mFromTimeTextView = v.findViewById(R.id.eventStartAt);
        mFromTimeTextView.setText(DateFormat.format("HH:mm", mStartCalendar.getTime()));
        mThisEvent.scheduleEvent(Event.START_TIME_KEY, mStartCalendar.get(Calendar.HOUR_OF_DAY), mStartCalendar.get(Calendar.MINUTE));
        mFromTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimePicking(v);
            }
        });

        mToTimeTextView = v.findViewById(R.id.eventEndsAt);
        if(mEventId != null){
            Calendar endTime = Calendar.getInstance();
            endTime.set(Calendar.HOUR_OF_DAY, mThisEvent.getEndTime()/60);
            endTime.set(Calendar.MINUTE, mThisEvent.getEndTime()%60);
            mToTimeTextView.setText(DateFormat.format("HH:mm", endTime.getTime()));
        }
        else {
                mThisEvent.scheduleEvent(Event.END_TIME_KEY, mEndCalendar.get(Calendar.HOUR_OF_DAY) + 1,
                        mEndCalendar.get(Calendar.MINUTE));
        }
        mToTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimePicking(v);
            }
        });

        mSaveButton = v.findViewById(R.id.saveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateInfo()) {
                    if (mEventId != null) {
                        mDayParent.removeEvent(new Event[]{mThisEvent});
                        mDayParent.setEditedEvent(mThisEvent);
                    } else{
                        mThisEvent.setEventTitle(mNewEventTitle.getText().toString());
                        mThisEvent.setEventDetails(mNewEventDetails.getText().toString());
                        mThisEvent.scheduleEvent
                                (Event.END_TIME_KEY, mEndCalendar.get(Calendar.HOUR_OF_DAY), mEndCalendar.get(Calendar.MINUTE));
                        mThisEvent.scheduleEvent
                                (Event.START_TIME_KEY, mStartCalendar.get(Calendar.HOUR_OF_DAY), mStartCalendar.get(Calendar.MINUTE));
                        mDayParent.addEvent(mThisEvent);
                    }
                    getActivity().finish();
                }
            }
        });
    }


    // Checks if the the necessary fields were inserted
    // and if the info does not conflict with previous activities
    private boolean validateInfo(){
        if(mThisEvent.getEventTitle() == null) {
            Toast.makeText(getContext(), NO_TITLE_TOAST, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(mThisEvent.getStartTime() > mThisEvent.getEndTime()) {
            Toast.makeText(getContext(), TIME_PARADOX_TOAST, Toast.LENGTH_LONG).show();
            return false;
        }
        Event intersEvent = mDayParent.findIntersection(mThisEvent);
        if(intersEvent != null){
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            OverrideDialog dialog = new OverrideDialog();
            dialog.setTargetFragment(EventCreatorFragment.this, OVERRIDE_DIALOG_REQUEST);
            dialog.show(fragmentManager, OVERRIDE_DIALOG_TAG);
            return false;
        }

        return true;
    }

    // Initiates TimePickerFragment with respect to the relevant time-section (FROM or TO)
    private void onTimePicking(View v){
        int requestCode;
        String tag;
        if(v.getId() == R.id.eventStartAt){
            requestCode = FROM_TIME_REQUEST_CODE;
            tag = FROM_TIME_DIALOG_TAG;
        }
        else{
            requestCode = TO_TIME_REQUEST_CODE;
            tag = TO_TIME_DIALOG_TAG;
        }

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        TimePickerFragment timePicker = TimePickerFragment.newInstance(mStartCalendar.get(Calendar.HOUR_OF_DAY), mStartCalendar.get(Calendar.MINUTE));
        timePicker.setTargetFragment(EventCreatorFragment.this, requestCode);
        timePicker.show(fragmentManager, tag);
    }

    // Handles results from dialogs etc.
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case TO_TIME_REQUEST_CODE: {
                if(resultCode == Activity.RESULT_OK){
                    mEndCalendar.set(Calendar.HOUR_OF_DAY, data.getIntExtra(EXTRA_HOUR_OF_DAY, 0));
                    mEndCalendar.set(Calendar.MINUTE, data.getIntExtra(EXTRA_MINUTES, 0));

                    mToTimeTextView.setText(DateFormat.format("HH:mm", mEndCalendar.getTime()));
                }
            } break;
            case FROM_TIME_REQUEST_CODE: {
                if (resultCode == Activity.RESULT_OK) {
                    mStartCalendar.set(Calendar.HOUR_OF_DAY, data.getIntExtra(EXTRA_HOUR_OF_DAY, 0));
                    mStartCalendar.set(Calendar.MINUTE, data.getIntExtra(EXTRA_MINUTES, 0));

                    mFromTimeTextView.setText(DateFormat.format("HH:mm", mStartCalendar.getTime()));
                }
            } break;
        }
    }
}
