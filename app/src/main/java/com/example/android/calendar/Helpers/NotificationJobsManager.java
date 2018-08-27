package com.example.android.calendar.Helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NotificationJobsManager {

    public static final String EX_TITLE = "exNotificationTitle";
    public static final String EX_INFO = "exNotificationInfo";
    public static final String EX_JOB_EVENT_ID = "exJobEventId";
    public static final String CHANNEL_ID = "notificationChannelId";

    private static HashMap<UUID, Integer> mNotificationJobs = new HashMap<>();

    public static void createNotificationChannel(Context applicationContext){
        NotificationManager mNM = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mNC = new NotificationChannel(CHANNEL_ID, "Notification Channel", NotificationManager.IMPORTANCE_HIGH);
        mNC.enableLights(true);
        mNC.enableVibration(true);
        mNC.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 100});
        mNM.createNotificationChannel(mNC);
    }

    public static void addJob(JobInfo jobInfo, UUID eventId){
        mNotificationJobs.put(eventId, jobInfo.getId());
    }

    public static int removeJob(UUID eventId){
        int deletedJobId = mNotificationJobs.get(eventId);
        mNotificationJobs.remove(eventId);

        return deletedJobId;
    }

    public static void loadPendingJobs(Context applicationContext){
        JobScheduler jobScheduler = (JobScheduler) applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> pendingJobs = jobScheduler.getAllPendingJobs();
        String eventId;
        for(JobInfo j : pendingJobs) {
            eventId = j.getExtras().getString(EX_JOB_EVENT_ID);
            mNotificationJobs.put(UUID.fromString(eventId), j.getId());
        }
    }
}
