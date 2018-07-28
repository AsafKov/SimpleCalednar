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
import java.util.UUID;

public class EventCreatorFragment extends Fragment {

    public static final String EX_EVENT_ID = "extraEventId";

    // Toasts
    private static final String NO_TITLE_TOAST = "Please choose label";
    private static final String TIME_PARADOX_TOAST = "Your activity starts after it ends. " +
            "Please deliver your time machine to the nearest police station";

    // App configuration keys
    private static final String KEY_DAY_PARENT_ID = "dayParentId";
    private static final String KEY_EVENT_ID = "eventId";
    private static final String KEY_EDITED_STARTING_TIME = "editedStartingTime";
    private static final String KEY_EDITED_ENDING_TIME = "editedEndingTime";

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

    // Widgets
    private EditText mNewEventLabel, mNewEventComment;
    private TextView mFromTimeTextView, mToTimeTextView;
    private Button mSaveButton;

    private Day mDayParent;
    private Calendar mStartCalendar = Calendar.getInstance();
    private Calendar mEndCalendar = Calendar.getInstance();

    private Event mThisEvent;
    private NotificationPublisher notificationPublisher = new NotificationPublisher();
    private UUID mEventId; // If an event is being edited, rather than a new one being created

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        if(savedInstanceState != null && !savedInstanceState.isEmpty()){
            mDayParent = Day.findDayById(UUID.fromString(savedInstanceState.getString(KEY_DAY_PARENT_ID)));
            if(savedInstanceState.get(KEY_EVENT_ID) != null)
                mEventId = UUID.fromString(savedInstanceState.getString(KEY_EVENT_ID));
            else
                mEventId = null;
        }
        else {
            mDayParent = Day.findDayById(((UUID)getActivity().getIntent().getSerializableExtra(RecyclerViewAdapter.EX_DAY_ID)));
            mEventId = (UUID)getActivity().getIntent().getSerializableExtra(RecyclerViewAdapter.EX_EVENT_ID);
        }

        // Determine whatever it is an edit or not
        if(mEventId == null)
            mThisEvent = new Event(mDayParent);
        else
            mThisEvent = mDayParent.findEventById(mEventId);

        adjustCalendars(savedInstanceState);
    }

    private void adjustCalendars(Bundle savedInstanceState){
        int startingTime = 0;
        int endingTime = 60;
        Event previousEvent;
        if(savedInstanceState != null && !savedInstanceState.isEmpty()){
            startingTime = savedInstanceState.getInt(KEY_EDITED_STARTING_TIME);
            endingTime = savedInstanceState.getInt(KEY_EDITED_ENDING_TIME);
        }
        else{
            if(mEventId != null){
                startingTime = mThisEvent.getStartTime();
                endingTime = mThisEvent.getEndTime();
            }
            else{
                previousEvent = mDayParent.getLastOnDay();
                if(previousEvent != null){
                    startingTime = previousEvent.getEndTime();
                    endingTime = startingTime + 60;
                }
            }
        }

        mStartCalendar.set(Calendar.HOUR_OF_DAY, startingTime/60);
        mStartCalendar.set(Calendar.MINUTE, startingTime%60);
        mEndCalendar.set(Calendar.HOUR_OF_DAY, endingTime/60);
        mEndCalendar.set(Calendar.MINUTE, endingTime%60);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.event_creator_layout, parent, false);
        initWidgets(v);

        return v;
    }

    // Initialize widgets and their functions
    private void initWidgets(View v){
        mNewEventLabel = v.findViewById(R.id.newEventLabel);
        mNewEventLabel.setText(mThisEvent.getLabel());
        mNewEventLabel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mNewEventComment = v.findViewById(R.id.newEventComment);
        mNewEventComment.setText(mThisEvent.getComment());
        mNewEventComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mFromTimeTextView = v.findViewById(R.id.eventStartAt);
        mFromTimeTextView.setText(DateFormat.format("HH:mm", mStartCalendar.getTime()));
        mFromTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimePicking(v);
            }
        });

        mToTimeTextView = v.findViewById(R.id.eventEndsAt);
        mToTimeTextView.setText(DateFormat.format("HH:mm", mEndCalendar.getTime()));
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
                    mThisEvent.setLabel(mNewEventLabel.getText().toString());
                    mThisEvent.setComment(mNewEventComment.getText().toString());
                    mThisEvent.scheduleEvent
                            (Event.END_TIME_KEY, mEndCalendar.get(Calendar.HOUR_OF_DAY), mEndCalendar.get(Calendar.MINUTE));
                    mThisEvent.scheduleEvent
                            (Event.START_TIME_KEY, mStartCalendar.get(Calendar.HOUR_OF_DAY), mStartCalendar.get(Calendar.MINUTE));

                    if (mEventId == null)
                        mDayParent.addEvent(mThisEvent);
                    else{
                        mDayParent.removeEvent(new Event[]{mThisEvent});
                        mDayParent.addEvent(mThisEvent);
                    }

                    sendResult();
                    getActivity().finish();

                }
            }
        });
    }

    public void sendResult(){
        Intent data = new Intent();
        data.putExtra(EX_EVENT_ID, mThisEvent.getId());
        getActivity().setResult(Activity.RESULT_OK, data);
    }

    // Initiates TimePickerFragment with respect to the relevant time-section (FROM or TO)
    private void onTimePicking(View v){
        int requestCode;
        String tag;
        TimePickerFragment timePicker;
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        if(v.getId() == R.id.eventStartAt){
            requestCode = FROM_TIME_REQUEST_CODE;
            tag = FROM_TIME_DIALOG_TAG;
            timePicker = TimePickerFragment.
                    newInstance(mStartCalendar.get(Calendar.HOUR_OF_DAY), mStartCalendar.get(Calendar.MINUTE));
        }
        else{
            requestCode = TO_TIME_REQUEST_CODE;
            tag = TO_TIME_DIALOG_TAG;
            timePicker = TimePickerFragment.
                    newInstance(mEndCalendar.get(Calendar.HOUR_OF_DAY), mEndCalendar.get(Calendar.MINUTE));
        }

        timePicker.setTargetFragment(EventCreatorFragment.this, requestCode);
        timePicker.show(fragmentManager, tag);
    }

    // Handles results from dialogs etc.
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        int duration = 0;
        if (resultCode != Activity.RESULT_OK)
            return;

        if(mEventId != null)
            duration = mThisEvent.getEndTime() - mThisEvent.getStartTime();

        switch(requestCode){
            case TO_TIME_REQUEST_CODE: {
                mEndCalendar.set(Calendar.HOUR_OF_DAY, data.getIntExtra(EXTRA_HOUR_OF_DAY, 0));
                mEndCalendar.set(Calendar.MINUTE, data.getIntExtra(EXTRA_MINUTES, 0));

                mToTimeTextView.setText(DateFormat.format("HH:mm", mEndCalendar.getTime()));
            } break;
            case FROM_TIME_REQUEST_CODE: {
                mStartCalendar.set(Calendar.HOUR_OF_DAY, data.getIntExtra(EXTRA_HOUR_OF_DAY, 0));
                mStartCalendar.set(Calendar.MINUTE, data.getIntExtra(EXTRA_MINUTES, 0));

                // Automatically restore event-duration in case the start-time changed to be later then end-time
                if(mStartCalendar.getTimeInMillis() > mEndCalendar.getTimeInMillis()){
                    mEndCalendar.set(Calendar.HOUR_OF_DAY, mStartCalendar.get(Calendar.HOUR_OF_DAY) + duration/60);
                    mEndCalendar.set(Calendar.MINUTE, mStartCalendar.get(Calendar.MINUTE) + duration%60);
                    mToTimeTextView.setText(DateFormat.format("HH:mm", mEndCalendar.getTime()));
                }

                mFromTimeTextView.setText(DateFormat.format("HH:mm", mStartCalendar.getTime()));
            } break;
        }
    }

    // Checks if the the necessary fields were inserted
    // and if the info does not conflict with previous activities
    private boolean validateInfo(){
        if(mNewEventLabel.getText() == null) {
            Toast.makeText(getContext(), NO_TITLE_TOAST, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(mEndCalendar.getTimeInMillis() < mStartCalendar.getTimeInMillis()) {
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){

        mThisEvent.scheduleEvent(Event.START_TIME_KEY,
                mStartCalendar.get(Calendar.HOUR_OF_DAY), mStartCalendar.get(Calendar.MINUTE));
        mThisEvent.scheduleEvent(Event.END_TIME_KEY,
                mEndCalendar.get(Calendar.HOUR_OF_DAY), mEndCalendar.get(Calendar.MINUTE));

        savedInstanceState.putString(KEY_DAY_PARENT_ID, mDayParent.getId().toString());
        savedInstanceState.putInt(KEY_EDITED_STARTING_TIME, mThisEvent.getStartTime());
        savedInstanceState.putInt(KEY_EDITED_ENDING_TIME, mThisEvent.getEndTime());

        if(mEventId != null)
            savedInstanceState.putString(KEY_EVENT_ID, mThisEvent.getId().toString());

        super.onSaveInstanceState(savedInstanceState);
    }

}
