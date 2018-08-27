package com.example.android.calendar.Services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.android.calendar.Database.EventViewModel;
import com.example.android.calendar.Fragments.DayViewFragment;
import com.example.android.calendar.Helpers.NotificationJobsManager;
import com.example.android.calendar.Model.Event;
import com.example.android.calendar.R;

public class NotificationJobService extends JobService {

    private static int mNotificationId = -1;

    public NotificationJobService(){

    }

    @Override
    public boolean onStartJob(JobParameters params) {
        PersistableBundle bundle = params.getExtras();
        String[] data = new String[]{bundle.getString(NotificationJobsManager.EX_TITLE),
                bundle.getString(NotificationJobsManager.EX_INFO)};
        new NotificationPublisher().execute(data);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private class NotificationPublisher extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... data) {
            String title = data[0];
            String info = data[1];

            NotificationManagerCompat mNMC = NotificationManagerCompat.from(getApplicationContext());

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NotificationJobsManager.CHANNEL_ID);
            builder.setAutoCancel(true).setContentTitle(title).setContentText(info).setSmallIcon(R.drawable.ic_launcher_foreground);

            mNMC.notify(mNotificationId++, builder.build());

            return null;
        }
    }
}
