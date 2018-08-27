package com.example.android.calendar.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.example.android.calendar.Fragments.EventCreatorFragment;
import com.example.android.calendar.R;

public class NotificationDelayDialog extends DialogFragment {

    public static final String EX_DELAY_CHOSEN = "chosenItemId";

    public static final int[] options = new int[]{R.string.atTimeOfEvent, R.string.on5MinutesPrior,
    R.string.on10MinutesPrior, R.string.on15MinutesPrior, R.string.on20MinutesPrior, R.string.on25MinutesPrior,
    R.string.on30MinutesPrior, R.string.noNotification};

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        final String[] optionsText = new String[options.length];
        for(int i=0; i<optionsText.length; i++){
            optionsText[i] = getString(options[i]);
        }

        alertDialog.setItems(optionsText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResult(options[which], Activity.RESULT_OK);
            }
        });

        alertDialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResult(-1, Activity.RESULT_CANCELED);
            }
        });
        return alertDialog.create();
    }

    private void sendResult(int chosenItemId, int resultCode){
        Intent intent = new Intent();
        intent.putExtra(EX_DELAY_CHOSEN, chosenItemId);
        getTargetFragment().onActivityResult(EventCreatorFragment.NOTIFICATION_DELAY_DIALOG_REQUEST, resultCode, intent);
    }
}
