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
import com.example.android.calendar.Database.EventViewModel;
import com.example.android.calendar.Dialogs.LabelPickerDialog;
import com.example.android.calendar.Dialogs.NotificationDelayDialog;
import com.example.android.calendar.Dialogs.OverwriteDialog;
import com.example.android.calendar.Dialogs.TimePickerDialog;
import com.example.android.calendar.R;
import com.example.android.calendar.Helpers.RecyclerViewAdapter;
import java.util.Calendar;
import java.util.UUID;

public class EventCreatorFragment extends Fragment {

    // Toasts
    private static final String TIME_PARADOX_TOAST = "Your event starts after it ends. " +
            "Please deliver your time machine to the nearest police station";

    // TimePickerDialog tags, codes and keys
    public static final int FROM_TIME_REQUEST_CODE = 113;
    public static final String FROM_TIME_DIALOG_TAG = "fromTimePickerDialog";
    public static final int TO_TIME_REQUEST_CODE = 114;
    public static final String TO_TIME_DIALOG_TAG = "toTimePickerDialog";
    public static final String EXTRA_HOUR_OF_DAY = "hourOfDay";
    public static final String EXTRA_MINUTES = "minutes";

    // OverwriteDialog tags and code
    public static final String OVERWRITE_DIALOG_TAG = "overrideDialogTag";
    public static final int OVERWRITE_DIALOG_REQUEST = 115;

    // NotificationDelayDialog tags and codes
    public static final String NOTIFICATION_DELAY_DIALOG_TAG = "notificationDelayDialog";
    public static final int NOTIFICATION_DELAY_DIALOG_REQUEST = 117;

    public static final String TAG_LABEL_PICKER = "labelPickerDialog";
    private static final int RQ_LABEL_PICKER = 116;

    // Widgets
    private EditText mNewEventComment;
    private Button mSaveButton, mNewEventLabel, mFromTimeButton, mToTimeButton, mAddNotificationButton;
    private ActionBar mActionBar;

    private EventViewModel mEventViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        mEventViewModel = DayViewFragment.mEventViewModel;

        if(intent != null && savedInstanceState == null){
            mEventViewModel.setTimeStamp((long) intent.getSerializableExtra(RecyclerViewAdapter.EX_TIME_STAMP));
            mEventViewModel.findEventById((UUID)intent.getSerializableExtra(RecyclerViewAdapter.EX_EVENT_ID));
            mEventViewModel.adjustCalendars();
        }
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
        int colorId = mEventViewModel.getBlockDefaultColor();
        Color color = Color.valueOf(getResources().getColor(colorId, getContext().getTheme()));
        color = Color.valueOf(color.red(), color.green(), color.blue(), 1);
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(color.toArgb()));

        mNewEventLabel = v.findViewById(R.id.newEventLabel);
        mNewEventLabel.setText(mEventViewModel.getEventLabel());
        mNewEventLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewEventLabel.setClickable(false);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                LabelPickerDialog labelPickerDialog = new LabelPickerDialog();
                labelPickerDialog.setTargetFragment(EventCreatorFragment.this, RQ_LABEL_PICKER);
                labelPickerDialog.show(fragmentManager, TAG_LABEL_PICKER);
                mNewEventLabel.setClickable(true);
            }
        });

        mNewEventComment = v.findViewById(R.id.newEventComment);
        mNewEventComment.setText(mEventViewModel.getEventComment());
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
        mFromTimeButton.setText(DateFormat.format("HH:mm", mEventViewModel.getStartCalendar().getTime()));
        mFromTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimePicking(v);
            }
        });

        mToTimeButton = v.findViewById(R.id.eventEndsAt);
        if(!mEventViewModel.isNewEvent())
            mToTimeButton.setText(DateFormat.format("HH:mm", mEventViewModel.getEndCalendar().getTime()));
        mToTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimePicking(v);
            }
        });

        initColorPicking(v);

        mAddNotificationButton = v.findViewById(R.id.addNotificationButton);
        if(!mEventViewModel.isNewEvent() && mEventViewModel.getNotificationDelay() != -1)
            mAddNotificationButton.setText(getString(NotificationDelayDialog.options[mEventViewModel.getNotificationDelay()/5]));

        mAddNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                NotificationDelayDialog notificationDialog = new NotificationDelayDialog();
                notificationDialog.setTargetFragment(EventCreatorFragment.this, NOTIFICATION_DELAY_DIALOG_REQUEST);
                notificationDialog.show(fragmentManager, NOTIFICATION_DELAY_DIALOG_TAG);
            }
        });

        mSaveButton = v.findViewById(R.id.saveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSaveButton.setClickable(false);
                switch (mEventViewModel.validateInfo()){
                    case EventViewModel.VALID_DATA:{
                        mEventViewModel.setEventComment(mNewEventComment.getText().toString());
                        mEventViewModel.changeForCurrentEvent();
                        getActivity().finish();
                    } break;
                    case EventViewModel.ERR_INVALID_TIME:{
                        Toast.makeText(getContext(), TIME_PARADOX_TOAST, Toast.LENGTH_LONG).show();
                    } break;
                    case EventViewModel.ERR_REQUIRE_OVERWRITE:{
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        OverwriteDialog dialog = new OverwriteDialog();
                        dialog.setTargetFragment(EventCreatorFragment.this, OVERWRITE_DIALOG_REQUEST);
                        dialog.show(fragmentManager, OVERWRITE_DIALOG_TAG);
                    }
                }
                mSaveButton.setClickable(true);
            }
        });
    }

    private void initColorPicking(View v){
        final int[] colorIds = new int[]{R.color.eventBlockBackgroundDefault,
                R.color.eventBlockBackgroundBlue, R.color.eventBlockBackgroundGreen,
                R.color.eventBlockBackgroundOrange, R.color.eventBlockBackgroundPink,
                R.color.eventBlockBackgroundRed, R.color.eventBlockBackgroundYellow};

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
                    mEventViewModel.setBlockDefaultColor(colorIds[index]);
                    Color color = Color.valueOf(getResources().getColor(colorIds[index], getContext().getTheme()));
                    color = Color.valueOf(color.red(), color.green(), color.blue(), 1);
                    mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    mActionBar.setBackgroundDrawable(new ColorDrawable(color.toArgb()));
                }
            });
        }
    }

    // Initiates TimePickerDialog with respect to the relevant time-section (FROM or TO)
    private void onTimePicking(View v){
        mToTimeButton.setClickable(false);
        mFromTimeButton.setClickable(false);
        int requestCode;
        String tag;
        TimePickerDialog timePicker;
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        if(v.getId() == R.id.eventStartAt){
            requestCode = FROM_TIME_REQUEST_CODE;
            tag = FROM_TIME_DIALOG_TAG;
            timePicker = TimePickerDialog.newInstance(mEventViewModel.getStartCalendar().
                    get(Calendar.HOUR_OF_DAY), mEventViewModel.getStartCalendar().get(Calendar.MINUTE));
        }
        else{
            requestCode = TO_TIME_REQUEST_CODE;
            tag = TO_TIME_DIALOG_TAG;
            timePicker = TimePickerDialog.
                    newInstance(mEventViewModel.getEndCalendar().
                            get(Calendar.HOUR_OF_DAY), mEventViewModel.getEndCalendar().get(Calendar.MINUTE));
        }

        mFromTimeButton.setClickable(true);
        mToTimeButton.setClickable(true);

        timePicker.setTargetFragment(EventCreatorFragment.this, requestCode);
        timePicker.show(fragmentManager, tag);
    }

    // Handles results from dialogs etc.
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode != Activity.RESULT_OK)
            return;
        int hourOfDay, minute;
        switch(requestCode){
            case TO_TIME_REQUEST_CODE: {
                hourOfDay = data.getIntExtra(EXTRA_HOUR_OF_DAY, mEventViewModel.getEndCalendar().get(Calendar.HOUR_OF_DAY));
                minute = data.getIntExtra(EXTRA_MINUTES, mEventViewModel.getEndCalendar().get(Calendar.MINUTE));

                mEventViewModel.setEndTime(hourOfDay, minute);
                mToTimeButton.setText(DateFormat.format("HH:mm", mEventViewModel.getEndCalendar().getTime()));
            } break;
            case FROM_TIME_REQUEST_CODE: {
                hourOfDay = data.getIntExtra(EXTRA_HOUR_OF_DAY, mEventViewModel.getStartCalendar().get(Calendar.HOUR_OF_DAY));
                minute = data.getIntExtra(EXTRA_MINUTES, mEventViewModel.getStartCalendar().get(Calendar.MINUTE));
                mEventViewModel.setStartTime(hourOfDay, minute);
                mFromTimeButton.setText(DateFormat.format("HH:mm", mEventViewModel.getStartCalendar().getTime()));
                int endTime;
                if(mToTimeButton.getText().equals(getString(R.string.untilNextEvent))) {
                    endTime = mEventViewModel.readjustEndTime();
                    mEventViewModel.setEndTime(endTime/60, endTime%60);
                }
            } break;
            case RQ_LABEL_PICKER: {
                String label = data.getStringExtra(LabelPickerDialog.EX_LABEL);
                mEventViewModel.setEventLabel(label);
                mNewEventLabel.setText(label);
            } break;
            case OVERWRITE_DIALOG_REQUEST: {
                mEventViewModel.overwrite();
                mEventViewModel.setEventComment(mNewEventComment.getText().toString());
                mEventViewModel.changeForCurrentEvent(); // No need to validate info
                getActivity().finish();
            } break;
            case NOTIFICATION_DELAY_DIALOG_REQUEST: {
                int optionId = data.getIntExtra(NotificationDelayDialog.EX_DELAY_CHOSEN, -1);
                handleHandleDelayResult(optionId);

            }
        }
    }

    // Handling the result from NotificationDelayDialog
    private void handleHandleDelayResult(int optionId){
        int notificationDelay = 0;
        int notificationStatus = optionId;
        switch (optionId){
            case R.string.atTimeOfEvent: {
                notificationDelay = 0;
            } break;
            case R.string.on5MinutesPrior: {
                notificationDelay = 5;
            } break;
            case R.string.on10MinutesPrior: {
                notificationDelay = 10;
            } break;
            case R.string.on15MinutesPrior: {
                notificationDelay = 15;
            } break;
            case R.string.on20MinutesPrior: {
                notificationDelay = 20;
            } break;
            case R.string.on25MinutesPrior: {
                notificationDelay = 25;
                notificationStatus = R.string.on25MinutesPrior;
            } break;
            case R.string.on30MinutesPrior: {
                notificationDelay = 30;
            } break;
            case R.string.noNotification: {
                notificationDelay = -1;
            } break;
        }
        mEventViewModel.setNotificationDelay(notificationDelay);
        mAddNotificationButton.setText(getString(notificationStatus));
    }
}
