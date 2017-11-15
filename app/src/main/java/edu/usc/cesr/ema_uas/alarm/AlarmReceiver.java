package edu.usc.cesr.ema_uas.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import edu.usc.cesr.ema_uas.util.AlarmUtil;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    @Override
    public void onReceive(final Context context, Intent intent) {
        int requestCode = intent.getIntExtra(MyAlarmManager.REQUEST_CODE, 0);

        Intent i = new Intent(context, AlarmService.class);
        i.putExtra(MyAlarmManager.REQUEST_CODE, requestCode);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startWakefulService(context, i);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("Reminders", "Reminders", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        Log.i(TAG, "notification sent " + id);

        notificationManager.notify(id, notification);

        if(requestCode % 3 == 0 || requestCode % 3 == 1){
            AlarmUtil.soundAlarm(context);
        }

        Log.e("TT", "AlarmReceiver => onReceive() => requestCode == " + requestCode + " type: " + (requestCode % 3));
    }
}
