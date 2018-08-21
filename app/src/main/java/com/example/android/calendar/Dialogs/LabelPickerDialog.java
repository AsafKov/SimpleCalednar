package com.example.android.calendar.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.example.android.calendar.Model.Event;
import com.example.android.calendar.R;

public class LabelPickerDialog extends DialogFragment {

    public static final String EX_LABEL = "extraLabel";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setItems(Event.mLabelsTypes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResult(Event.mLabelsTypes[which]);
            }
        });
        alertDialogBuilder.setCustomTitle(getActivity().getLayoutInflater().inflate(R.layout.label_picker_headline, null));
        AlertDialog alertDialog = alertDialogBuilder.create();
        return alertDialog;
    }

    private void sendResult(String type){
        Intent intent = new Intent();
        intent.putExtra(EX_LABEL, type);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }
}
