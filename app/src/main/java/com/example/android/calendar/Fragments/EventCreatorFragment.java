package com.example.android.calendar.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.example.android.calendar.Dialogs.LabelPickerDialog;
import com.example.android.calendar.Dialogs.OverwriteDialog;
import com.example.android.calendar.Dialogs.TimePickerDialog;
import com.example.android.calendar.Model.Day;
import com.example.android.calendar.Model.Event;
import com.example.android.calendar.R;
import com.example.android.calendar.Helpers.RecyclerViewAdapter;
import java.util.Calendar;
import java.util.UUID;

public class EventCreatorFragment extends Fragment {

    public static final String EX_EVENT_ID = "extraEventId";
    public static final String EX_PREFORMED_OVERWRITE = "preformedOverwrite";

    // Toasts
    private static final String TIME_PARADOX_TOAST = "Your event starts after it ends. " +
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

    // OverwriteDialog tags and code
    public static final String OVERWRITE_DIALOG_TAG = "overrideDialogTag";
    public static final int OVERWRITE_DIALOG_REQUEST = 2;

    public static final String TAG_LABEL_PICKER = "labelPickerDialog";
    private static final int RQ_LABEL_PICKER = 3;

    // Widgets
    private EditText mNewEventComment;
    private Button mSaveButton, mNewEventLabel, mFromTimeButton, mToTimeButton;
    private ActionBar mActionBar;

    private Day mDayParent;
    private Calendar mStartCalendar = Calendar.getInstance();
    private Calendar mEndCalendar = Calendar.getInstance();
    private int[] mBlockColorScheme = new int[2];

    private Event mThisEvent;
    private UUID mEventId; // If an event is being edited, rather than a new one being created
    private boolean preformedOverwrite = false;

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
        int endingTime = 0;
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
                    mStartCalendar.set(Calendar.HOUR_OF_DAY, (startingTime/60)%24);
                    mStartCalendar.set(Calendar.MINUTE, startingTime%60);
                    endingTime = resetUntilNextEvent();
                }
            }
        }
        mStartCalendar.set(Calendar.HOUR_OF_DAY, (startingTime/60)%24);
        mStartCalendar.set(Calendar.MINUTE, startingTime%60);
        mEndCalendar.set(Calendar.HOUR_OF_DAY, endingTime/60);
        mEndCalendar.set(Calendar.MINUTE, endingTime%60);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.event_creator_layout, parent, false);

        getActivity().setTitle(R.string.eventCreatorTitle);
        initWidgets(v);
        return v;
    }

    // Initialize widgets and their functions
    private void initWidgets(View v){
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        int colorId = R.color.eventBlockBackgroundDefault;
        if(mEventId != null) {
            mBlockColorScheme = mThisEvent.getBlockColorScheme();
            colorId = mBlockColorScheme[0];
        }
        else{
            mBlockColorScheme[0] = R.color.eventBlockBackgroundDefault;
            mBlockColorScheme[1] = R.color.eventBlockBackgroundDefaultOnPressed;
        }
        Color color = Color.valueOf(getResources().getColor(colorId, getContext().getTheme()));
        color = Color.valueOf(color.red(), color.green(), color.blue(), 1);
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(color.toArgb()));

        mNewEventLabel = v.findViewById(R.id.newEventLabel);
        mNewEventLabel.setText(mThisEvent.getLabel());
        mNewEventLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                LabelPickerDialog labelPickerDialog = new LabelPickerDialog();
                labelPickerDialog.setTargetFragment(EventCreatorFragment.this, RQ_LABEL_PICKER);
                labelPickerDialog.show(fragmentManager, TAG_LABEL_PICKER);
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

        mFromTimeButton = v.findViewById(R.id.eventStartAt);
        mFromTimeButton.setText(DateFormat.format("HH:mm", mStartCalendar.getTime()));
        mFromTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimePicking(v);
            }
        });

        mToTimeButton = v.findViewById(R.id.eventEndsAt);
        if(mEventId != null)
            mToTimeButton.setText(DateFormat.format("HH:mm", mEndCalendar.getTime()));
        mToTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimePicking(v);
            }
        });

        initColorPicking(v);

        mSaveButton = v.findViewById(R.id.saveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateInfo()) {
                    mThisEvent.setLabel(mNewEventLabel.getText().toString());
                    mThisEvent.setComment(mNewEventComment.getText().toString());
                    mThisEvent.scheduleEvent
                            (Event.START_TIME_KEY, mStartCalendar.get(Calendar.HOUR_OF_DAY), mStartCalendar.get(Calendar.MINUTE));
                    if(mToTimeButton.getText().equals(getString(R.string.untilNextEvent)))
                        mThisEvent.setUntilNextEvent(mDayParent.findNextEvent(mThisEvent.getStartTime()));
                    else
                        mThisEvent.scheduleEvent
                                (Event.END_TIME_KEY, mEndCalendar.get(Calendar.HOUR_OF_DAY), mEndCalendar.get(Calendar.MINUTE));

                    if (mEventId == null)
                        mDayParent.addEvent(mThisEvent);
                    else{
                        mDayParent.removeEvent(mThisEvent);
                        mDayParent.addEvent(mThisEvent);
                    }

                    mThisEvent.setBlockColorScheme(mBlockColorScheme);

                    sendResult();
                    getActivity().finish();
                }
            }
        });
    }

    private void initColorPicking(View v){
        final int[] colorIds = new int[]{R.color.eventBlockBackgroundDefault, R.color.eventBlockBackgroundBlue, R.color.eventBlockBackgroundGreen,
                R.color.eventBlockBackgroundOrange, R.color.eventBlockBackgroundPink, R.color.eventBlockBackgroundRed, R.color.eventBlockBackgroundYellow};
        final int[] onPressedColorsIds = new int[]{R.color.eventBlockBackgroundDefaultOnPressed, R.color.eventBlockBackgroundBlueOnPressed,
                R.color.eventBlockBackgroundGreenOnPressed, R.color.eventBlockBackgroundOrangeOnPressed, R.color.eventBlockBackgroundPinkOnPressed,
                R.color.eventBlockBackgroundRedOnPressed, R.color.eventBlockBackgroundYellowOnPressed};
        final LinearLayout container = v.findViewById(R.id.colorLinearLayout);
        ImageButton colorPicker;
        GradientDrawable gradientDrawable;
        Color currentColor;
        for(int i=0; i<container.getChildCount(); i++){
            colorPicker = (ImageButton) container.getChildAt(i);
            gradientDrawable = (GradientDrawable) colorPicker.getBackground().getConstantState().newDrawable();
            currentColor = Color.valueOf(getResources().getColor(colorIds[i], getContext().getTheme()));
            // Re-constructing the color scheme to have 0 transparency
            currentColor = Color.valueOf(currentColor.red(), currentColor.green(), currentColor.blue(), 1);
            gradientDrawable.setColor(currentColor.toArgb());
            colorPicker.setBackground(gradientDrawable);

            colorPicker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = container.indexOfChild(v);
                    mBlockColorScheme[0] = colorIds[index];
                    mBlockColorScheme[1] = onPressedColorsIds[index];
                    Color color = Color.valueOf(getResources().getColor(colorIds[index], getContext().getTheme()));
                    color = Color.valueOf(color.red(), color.green(), color.blue(), 1);
                    mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    mActionBar.setBackgroundDrawable(new ColorDrawable(color.toArgb()));
                }
            });
        }
    }

    public void sendResult(){
        Intent data = new Intent();
        data.putExtra(EX_EVENT_ID, mThisEvent.getId());
        data.putExtra(EX_PREFORMED_OVERWRITE, preformedOverwrite);
        getActivity().setResult(Activity.RESULT_OK, data);
    }

    // Initiates TimePickerDialog with respect to the relevant time-section (FROM or TO)
    private void onTimePicking(View v){
        int requestCode;
        String tag;
        TimePickerDialog timePicker;
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        if(v.getId() == R.id.eventStartAt){
            requestCode = FROM_TIME_REQUEST_CODE;
            tag = FROM_TIME_DIALOG_TAG;
            timePicker = TimePickerDialog.
                    newInstance(mStartCalendar.get(Calendar.HOUR_OF_DAY), mStartCalendar.get(Calendar.MINUTE));
        }
        else{
            requestCode = TO_TIME_REQUEST_CODE;
            tag = TO_TIME_DIALOG_TAG;
            timePicker = TimePickerDialog.
                    newInstance(mEndCalendar.get(Calendar.HOUR_OF_DAY), mEndCalendar.get(Calendar.MINUTE));
        }

        timePicker.setTargetFragment(EventCreatorFragment.this, requestCode);
        timePicker.show(fragmentManager, tag);
    }

    // When on Until Next Event option, reset the end time according to the changes in the start time
    private int resetUntilNextEvent(){
        int startTime = mStartCalendar.get(Calendar.HOUR_OF_DAY)*60 + mStartCalendar.get(Calendar.MINUTE);
        Event event = mDayParent.findNextEvent(startTime);
        int endTime = event == null? (24*60)-1:event.getStartTime();
        return endTime;
    }

    // Handles results from dialogs etc.
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        if (resultCode != Activity.RESULT_OK)
            return;
        switch(requestCode){
            case TO_TIME_REQUEST_CODE: {
                mEndCalendar.set(Calendar.HOUR_OF_DAY, data.getIntExtra(EXTRA_HOUR_OF_DAY, 0));
                mEndCalendar.set(Calendar.MINUTE, data.getIntExtra(EXTRA_MINUTES, 0));

                mToTimeButton.setText(DateFormat.format("HH:mm", mEndCalendar.getTime()));
            } break;
            case FROM_TIME_REQUEST_CODE: {
                mStartCalendar.set(Calendar.HOUR_OF_DAY, data.getIntExtra(EXTRA_HOUR_OF_DAY, 0));
                mStartCalendar.set(Calendar.MINUTE, data.getIntExtra(EXTRA_MINUTES, 0));
                mFromTimeButton.setText(DateFormat.format("HH:mm", mStartCalendar.getTime()));
                int endTime;
                if(mToTimeButton.getText().equals(getString(R.string.untilNextEvent))) {
                    endTime = resetUntilNextEvent();
                    mEndCalendar.set(Calendar.HOUR_OF_DAY, endTime/60);
                    mEndCalendar.set(Calendar.MINUTE, endTime%60);
                }
            } break;
            case RQ_LABEL_PICKER: {
                mNewEventLabel.setText(data.getStringExtra(LabelPickerDialog.EX_LABEL));
            } break;
            case OVERWRITE_DIALOG_REQUEST: {
                int startTime = mStartCalendar.get(Calendar.HOUR_OF_DAY)*60 + mStartCalendar.get(Calendar.MINUTE);
                int endTime = mEndCalendar.get(Calendar.HOUR_OF_DAY)*60 + mEndCalendar.get(Calendar.MINUTE);
                mDayParent.overwrite(startTime, endTime);
                preformedOverwrite = true;
                mSaveButton.callOnClick();
            } break;
        }
    }

    // Checks if the the necessary fields were inserted
    // and if the info does not conflict with previously created activities
    private boolean validateInfo(){
        if(mEndCalendar.getTimeInMillis() < mStartCalendar.getTimeInMillis()) {
            Toast.makeText(getContext(), TIME_PARADOX_TOAST, Toast.LENGTH_LONG).show();
            return false;
        }

        if(mDayParent.findIntersection(mStartCalendar, mEndCalendar, mThisEvent.getId())){
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            OverwriteDialog dialog = new OverwriteDialog();
            dialog.setTargetFragment(EventCreatorFragment.this, OVERWRITE_DIALOG_REQUEST);
            dialog.show(fragmentManager, OVERWRITE_DIALOG_TAG);
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
