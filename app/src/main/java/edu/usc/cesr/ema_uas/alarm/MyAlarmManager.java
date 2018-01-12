package edu.usc.cesr.ema_uas.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.usc.cesr.ema_uas.Constants;
import edu.usc.cesr.ema_uas.R;
import edu.usc.cesr.ema_uas.model.Survey;
import edu.usc.cesr.ema_uas.ui.MainActivity;
import edu.usc.cesr.ema_uas.util.DateUtil;

import static android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES;

@SuppressWarnings({"WeakerAccess"})
public class MyAlarmManager {
    public static String REQUEST_CODE = "REQUEST_CODE";
    public static String TIME_TAG = "TIME_TAG";

    private static MyAlarmManager mInstance = null;

    private AlarmManager alarmManager;

    /** Initialize/ Getters && Setters */
    public static MyAlarmManager getInstance(Context context){
        if(mInstance == null) {
            mInstance = new MyAlarmManager((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
        } return mInstance;
    }
    public MyAlarmManager(AlarmManager alarmManager) {
        this.alarmManager = alarmManager;
    }

    /** Set alarm */
    public void setAllAlarms(Context context, List<Survey> surveys){
        List<Survey> futureSurveys = futureSurveys(surveys);

        for(int i = 0; i < futureSurveys.size(); i++){
            Survey survey = futureSurveys.get(i);
            setAlarmReminderAndCancel(context, survey.getDate(), survey.getRequestCode());
        }
    }
    private void setSingleAlarm(Context context, Calendar calendar, int requestCode){
        SimpleDateFormat format = new SimpleDateFormat("kk:mm");
        Log.e("TT", "MyAlarmManager => setSingleAlarm() => Code: " + requestCode + " Date: " + DateUtil.stringifyAll(calendar));
        //  Build pending intent
        PendingIntent pendingIntent = buildPendingIntent(context, requestCode);

        //  Set alarm
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
    private void setAlarmReminderAndCancel(Context context, Calendar calendar, int requestCode){
        setSingleAlarm(context, calendar, requestCode);

        Calendar reminderTime = (Calendar) calendar.clone();
        reminderTime.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + Constants.TIME_TO_REMINDER);
        setSingleAlarm(context, reminderTime, requestCode + 1);

//        Calendar cancelTime = (Calendar) calendar.clone();
//        cancelTime.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + Constants.TIME_TO_TAKE_SURVEY);
//        setSingleAlarm(context, cancelTime, requestCode + 2);
    }

    /** Cancel */
    public void cancelAllAlarms(Context context){
        Log.e("TT", "MyAlarmManager => cancelAllAlarms()");

        for(int i = 0; i < 1000; i ++){
            cancelSingleAlarm(context, i);
        }
    }
    public void cancelSingleAlarm(Context context, int requestCode){
        PendingIntent pendingIntent = buildPendingIntent(context, requestCode);
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
        Log.e("TT", "MyAlarmManager => cancelSingleAlarm => Code: " + requestCode);
    }

    /** Helpers */
    private PendingIntent buildPendingIntent(Context context, int requestCode){

        //  Build pending intent
        Intent intent = new Intent(context, MainActivity.class);


        PendingIntent goBackPendingIntent = PendingIntent.getActivity(context,requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent notificationIntent = new Intent(context, AlarmReceiver.class);
        notificationIntent.putExtra(AlarmReceiver.NOTIFICATION_ID, requestCode);
//        notificationIntent.putExtra(AlarmReceiver.NOTIFICATION, mBuilder.build());
        notificationIntent.addFlags(FLAG_INCLUDE_STOPPED_PACKAGES);
        notificationIntent.putExtra(REQUEST_CODE, requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public List<Survey> futureSurveys(List<Survey> surveys){
        //  Filter alarms so only future alarms are set
        Calendar now = Calendar.getInstance();
        List<Survey> filteredSurveys = new ArrayList<>();
        for(int i = 0; i < surveys.size(); i++){
            Survey survey = surveys.get(i);
            if(now.compareTo(survey.getDate()) < 0){
                filteredSurveys.add(survey);
            }
        }
        return filteredSurveys;
    }


}
