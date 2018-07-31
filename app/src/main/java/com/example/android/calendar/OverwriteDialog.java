package com.example.android.calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

public class OverwriteDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        super.onCreateDialog(savedInstanceState);

        View v = getActivity().getLayoutInflater().inflate(R.layout.overwrite_dialog, null);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).setView(v);
        alertDialog.setPositiveButton(R.string.overwriteOption, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResult(Activity.RESULT_OK);
            }
        });
        alertDialog.setNegativeButton(R.string.resumeEditingOption, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResult(Activity.RESULT_CANCELED);
            }

        });
        return alertDialog.create();
    }

    private void sendResult(int resultCode){
        Intent intent = new Intent();
        getTargetFragment().onActivityResult(EventCreatorFragment.OVERWRITE_DIALOG_REQUEST, resultCode, intent);
    }
}
