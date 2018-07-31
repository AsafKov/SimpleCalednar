package com.example.android.calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public class NotificationPublisher extends BroadcastReceiver{

    private static final String NOTIFICATION_CHANNEL_ID = "notificationChannelId";
    private static final String NOTIFICATION_CHANNEL_NAME = "eventsNotificationChannel";

    public static final String EXTRA_ID = "extraId";
    private final Calendar mCalendar = Calendar.getInstance();

    private static HashMap<UUID, Notification> notifications = new HashMap<>();
    private static AlarmManager alarmManager;
    private static int counter = 0;

    public NotificationPublisher(){

    }

    public void createNotificationChannel(Context context){
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 100});
        notificationChannel.enableLights(true);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    }

    public void createNotification(int minutesBefore, PendingIntent activityIntent,
                                   PendingIntent notificationIntent, Context context, Event event){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).
                setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(event.getLabel()).
                setContentText(event.getComment()).setContentIntent(activityIntent);

        if(notifications.get(event.getId()) != null) {
            notifications.remove(event.getId());
            cancelNotification(event, context);
        }
        notifications.put(event.getId(), mBuilder.build());

        Calendar dueTime = mCalendar;
        dueTime.setTime(event.getTime());
        if(dueTime.get(Calendar.MINUTE) < minutesBefore)
            dueTime.set(Calendar.HOUR_OF_DAY, dueTime.get(Calendar.HOUR_OF_DAY)-1);
        dueTime.set(Calendar.MINUTE, (dueTime.get(Calendar.MINUTE) - minutesBefore + 60)%60);
        alarmManager.set(AlarmManager.RTC_WAKEUP, dueTime.getTimeInMillis(), notificationIntent);
    }

    public void cancelNotification(Event event, Context context){
        Intent intent = new Intent(context, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
        notifications.remove(event.getId());
    }

    @Override
    public void onReceive(Context context, Intent intent){
        UUID id = (UUID) intent.getSerializableExtra(EXTRA_ID);
        Notification notification = notifications.get(id);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(counter++, notification);
    }
}