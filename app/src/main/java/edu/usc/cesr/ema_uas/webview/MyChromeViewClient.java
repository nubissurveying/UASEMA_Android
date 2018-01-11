package edu.usc.cesr.ema_uas.webview;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.usc.cesr.ema_uas.Service.AccelerometerService;
import edu.usc.cesr.ema_uas.alarm.MyAlarmManager;
import edu.usc.cesr.ema_uas.model.JSONParser;
import edu.usc.cesr.ema_uas.model.LocalCookie;
import edu.usc.cesr.ema_uas.model.Settings;
import edu.usc.cesr.ema_uas.ui.AlarmActivity;
import edu.usc.cesr.ema_uas.ui.MainActivity;
import edu.usc.cesr.ema_uas.ui.SoundRecordingActivity;
import edu.usc.cesr.ema_uas.util.AcceFileManager;

public class MyChromeViewClient  extends WebChromeClient {
    private static final String END = "end", BEEP = "beep:",
            RTID = "rtid", VIDEO = "video", VIDEO_URL = "https://survey.usc.edu/ptus/index.php?p=showvideo",
            SOUNDRECORDING = "soundrecording", TAG = "Chrome View Client";

    private MainActivity activity;

    public MyChromeViewClient(MainActivity activity) {
        this.activity = activity;
    }

    // V155
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        //            Toast.makeText(getBaseContext(), "WebView JS Error: " + consoleMessage.message() + " @ line " + consoleMessage.lineNumber() + " of " + consoleMessage.sourceId(), Toast.LENGTH_LONG ).show();
        //  FirebaseCrash.report(new Exception( "WebView JS Error: " + consoleMessage.message() + " @ line " + consoleMessage.lineNumber() + " of " + consoleMessage.sourceId() ));
        return super.onConsoleMessage(consoleMessage);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
        //noinspection StatementWithEmptyBody
        Log.d("ChromeViewClient", message);
        if (message.equals(END)) {
            //  webView.setVisibility(View.GONE);
        } else if (message.startsWith(BEEP)){
            Intent intent = new Intent(activity, AlarmActivity.class);
            intent.putExtra(AlarmActivity.DEMO, true);
            activity.startActivity(intent);
        } else if(message.contains(RTID)){
            Settings settings = activity.getSettings();
            Settings jsonSetting = JSONParser.updateSettingSample(message);
            settings.updateAndSave(activity,jsonSetting.getRtid(),jsonSetting.getBeginTime(),jsonSetting.getEndTime(),jsonSetting.getBeginTime(), jsonSetting.getSurveys(), jsonSetting.getAccelrecording(), jsonSetting.getVideorecording());
            MyAlarmManager alarmManager = activity.getAlarmManager();
            alarmManager.cancelAllAlarms(activity.getBaseContext());
            //  Set up alarms
            alarmManager.setAllAlarms(activity.getBaseContext(), settings.getSurveys());
            activity.invalidateOptionsMenu();
            activity.startAcceService();
//            activity.logEvent(settings, MainActivity.SIGN_UP_EVENT);
//            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd:hh:mm");
//            try {
//
//                Log.e("TT", "MyChomeWebViewClient => " + message);
//                Date beginDate = format.parse(date + ":" + "10:00"); //begin
//                //Date endDate = format.parse(date + ":" + end);
//                Calendar beginCal = Calendar.getInstance();
//                Calendar endCal = Calendar.getInstance();
//                beginCal.setTime(beginDate);
//                endCal.setTime(beginDate);
//                endCal.add(Calendar.DAY_OF_MONTH, 7); //just add 7 days..
//              endCal.setTime(endDate);
//
//
//
//
//                Settings settings = activity.getSettings();
//                LocalCookie localCookie = activity.getLocalCookie();
//
//                if (true){
//                //if (beginCal.getTimeInMillis() != settings.getBeginTime().getTimeInMillis()) { //only if different!
//
//                    MyAlarmManager alarmManager = activity.getAlarmManager();
//
//                    //  Cancel settings
//                    alarmManager.cancelAllAlarms(activity.getBaseContext());
//
//                    //  Update settings
////                    settings.updateAndSave(activity, rtid, beginCal, endCal, Calendar.getInstance());
//
//                    //  Set up alarms
//                    alarmManager.setAllAlarms(activity.getBaseContext(), settings.getSurveys());
//
//                    //  Redraw menu
//                    activity.invalidateOptionsMenu();
//
//
//
//
//                }
//                //  activity.logEvent(settings, MainActivity.SIGN_UP_EVENT);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
        } else if (message.startsWith(VIDEO)){
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(VIDEO_URL));
                activity.startActivity(i);
            } catch(Exception e){ //this will happen if no browser is installed at all on the device
                e.printStackTrace();
            }
        }
        else if (message.startsWith(SOUNDRECORDING)) {
            try {
                Intent intent = new Intent(activity, SoundRecordingActivity.class);
                //intent.setData(Uri.parse(VIDEO_URL));
                activity.startActivity(intent);
            } catch (Exception e) { //this will happen if no browser is installed at all on the device
                e.printStackTrace();
            }
        }
        result.cancel();
        return true;
    }


}
