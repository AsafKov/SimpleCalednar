package com.example.android.calednar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

public class OverrideDialog extends DialogFragment {

    public static final int RESUME_EDITING_CODE = 3;
    public static final int OVERRIDE_CODE = 4;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        super.onCreateDialog(savedInstanceState);

        View v = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog, null);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).setView(v);
        alertDialog.setPositiveButton(R.string.overrideOption, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResult(OVERRIDE_CODE);
            }
        });
        alertDialog.setNegativeButton(R.string.resumeEditingOption, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendResult(RESUME_EDITING_CODE);
            }

        });
        return alertDialog.create();
    }

    private void sendResult(int resultCode){
        Intent intent = new Intent();
        getTargetFragment().onActivityResult(3, resultCode, intent);
    }
}
