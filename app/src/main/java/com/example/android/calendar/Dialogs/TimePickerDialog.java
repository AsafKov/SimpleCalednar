package com.example.android.calendar.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TimePicker;
import com.example.android.calendar.Fragments.EventCreatorFragment;
import com.example.android.calendar.R;

public class TimePickerDialog extends DialogFragment {

    private int mHourOfDay, mMinutes;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        super.onCreateDialog(savedInstanceState);

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_time_picker, null);

        mHourOfDay = getArguments().getInt(EventCreatorFragment.EXTRA_HOUR_OF_DAY);
        mMinutes = getArguments().getInt(EventCreatorFragment.EXTRA_MINUTES);

        TimePicker timePicker = v.findViewById(R.id.timePickerDialog);
        timePicker.setIs24HourView(true);
        timePicker.setHour(mHourOfDay);
        timePicker.setMinute(mMinutes);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mHourOfDay = hourOfDay;
                mMinutes = minute;
            }
        });

        return new AlertDialog.Builder(getActivity()).setView(v).
                setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResults();
                    }
                }).create();
    }

    public void sendResults(){
        Intent intent = new Intent();
        intent.putExtra(EventCreatorFragment.EXTRA_HOUR_OF_DAY, mHourOfDay);
        intent.putExtra(EventCreatorFragment.EXTRA_MINUTES, mMinutes);

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }

    public static TimePickerDialog newInstance(int mHourOfDay, int mMinutes){
        Bundle bundle = new Bundle();
        bundle.putSerializable(EventCreatorFragment.EXTRA_HOUR_OF_DAY, mHourOfDay);
        bundle.putSerializable(EventCreatorFragment.EXTRA_MINUTES, mMinutes);

        TimePickerDialog timePicker = new TimePickerDialog();
        timePicker.setArguments(bundle);
        return timePicker;
    }
}
