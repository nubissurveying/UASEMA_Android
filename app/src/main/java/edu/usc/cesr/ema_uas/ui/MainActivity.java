package edu.usc.cesr.ema_uas.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;

import butterknife.ButterKnife;
import edu.usc.cesr.ema_uas.BuildConfig;
import edu.usc.cesr.ema_uas.Constants;
import edu.usc.cesr.ema_uas.R;
import edu.usc.cesr.ema_uas.Service.AccelerometerService;
import edu.usc.cesr.ema_uas.alarm.MyAlarmManager;
import edu.usc.cesr.ema_uas.model.JSONParser;
import edu.usc.cesr.ema_uas.model.LocalCookie;
import edu.usc.cesr.ema_uas.model.Settings;
import edu.usc.cesr.ema_uas.model.Survey;
import edu.usc.cesr.ema_uas.model.Texts;
import edu.usc.cesr.ema_uas.model.UrlBuilder;
import edu.usc.cesr.ema_uas.util.AcceFileManager;
import edu.usc.cesr.ema_uas.util.LogUtil;
import edu.usc.cesr.ema_uas.webview.MyChromeViewClient;
import edu.usc.cesr.ema_uas.webview.MyWebViewClient;

@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends AppCompatActivity {
    public static String URL = "URL";
    public WebView webView;
    private ProgressDialog dialog;
    private Settings settings;



    private Texts texts;
    private MyAlarmManager alarmManager;

    public LocalCookie getLocalCookie() {
        return localCookie;
    }

    public void setLocalCookie(LocalCookie localCookie) {
        this.localCookie = localCookie;
    }

    private LocalCookie localCookie;
    //  private FirebaseAnalytics mFirebaseAnalytics;
    private boolean hasInternet = true;

    private NotificationManagerCompat manager;

    @SuppressWarnings({"deprecation", "ConstantConditions"})
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        alarmManager = MyAlarmManager.getInstance(this);
        //  mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //JSONParser tester
//        JSONParser.updateSettingSample();

        dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.main_loading));

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        getSupportActionBar().setIcon(R.drawable.uas_logo);

//        IntentFilter networkAvailableFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
//        registerReceiver(networkAvailableReceiver, networkAvailableFilter);

        //invalidateOptionsMenu();

//        Spannable text = new SpannableString("dddd");
//        text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//        getSupportActionBar().setTitle(text);

//        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
//        TextView abTitle = (TextView) findViewById(titleId);
//        abTitle.setTextColor(getResources().getColor(R.color.colorPrimaryDark));// (new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        webView = (WebView) findViewById(R.id.main_webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.setWebViewClient(new MyWebViewClient(this));
        webView.setWebChromeClient(new MyChromeViewClient(this));

        settings = Settings.getInstance(this);
        texts = Texts.getInstance(this);
        LogUtil.e("TT", "MainActivity => settings == " + settings);
        LogUtil.e("TT", "MainActivity => Texts == " + texts);

        Constants.TIME_TO_REMINDER = settings.getTimeToReminder();
        Constants.TIME_TO_TAKE_SURVEY = settings.getTimeToTakeSurvey();
        Constants.TIME_TO_TAKE_SURVEY_DISPLAY = settings.getTimeToTakeSurveyDispay();

        // No longer reading participant id from downloaded apk
        // if(settings.getRtid() == null){
        //    loadUserFromAPK();
        // }

        //  Handle finishing AlarmActivity
        int requestCode = getIntent().getIntExtra(MyAlarmManager.REQUEST_CODE, 0);
        if(requestCode % 3 == 2){
            int surveyCode = Survey.getSurveyCode(requestCode);
            settings.setClosedSurveyAndSave(this, surveyCode);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 10 * 1000);
        }
        if(requestCode % 3 == 0){
            MyAlarmManager myAlarmManager = MyAlarmManager.getInstance(this);
            myAlarmManager.cancelSingleAlarm(this,requestCode + 1);
        }

        //  Handles change settings and alarms
        String url = getIntent().getStringExtra(URL);
        if(url == null) route(settings);
        else showWebView(url);

        startAcceService();

        manager = NotificationManagerCompat.from(this);
        checkNotificaitonPremission();


    }


    @Override
    protected void onResume() {

        super.onResume();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        checkNotificaitonPremission();
        IntentFilter networkAvailableFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkAvailableReceiver, networkAvailableFilter);

        route(settings);


    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkAvailableReceiver);
    }

    @Override
    protected void onDestroy() {
        dismissDialog();
        super.onDestroy();
    }
    private void checkNotificaitonPremission(){
//        if(manager.areNotificationsEnabled()){
//            Log.d("NotificaitonPremission", "Notification enabled");
//        } else {
//            Log.d("NotificaitonPremission", "Notification not enabled");
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
//
//                ComponentName componetName = new ComponentName(
//                        "com.android.settings",
//                        "com.android.settings.applications.InstalledAppDetails");
//                Intent intent= new Intent();
//                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
//                intent.setData(Uri.parse("package:com.plusub.diapersapp"));
//                intent.setComponent(componetName);
//                startActivity(intent);
//                return;
//            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                startActivity(intent);
//                return;
//            }
//            return;
//            dismissDialog();
//            AlertDialog.Builder alert = new AlertDialog.Builder(this);
//            alert.setTitle(getResources().getString(R.string.main_permissions_title_before));
//            alert.setMessage(getResources().getString(R.string.main_permissions_body_before));
//            alert.setPositiveButton(getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int whichButton) {
//
//                    ActivityCompat.requestPermissions(getActivity(),
//                            new String[]{ Manifest.permission.Noti },
//                            ALL_PERMISSIONS_REQUEST_CODE);
//
//                }
//            });
//            alert.show();
//        }
    }
    private void route(Settings settings){
        if (!hasInternet){
            this.showMessage("No internet connection detected. Make sure you are connected to the cellular network or wifi.");
        }
        else {


            Calendar now = Calendar.getInstance();

            //        UrlBuilder.build(UrlBuilder.PHONE_ALARM, settings, Calendar.getInstance(), true);
            //  User is logged in and during survey
            if (settings.isLoggedIn() && settings.allFieldsSet() && settings.shouldShowSurvey(now)) {
                //        if(true) {
                //        if(settings.isLoggedIn() && settings.allFieldsSet() && settings.shouldShowSurvey(now)) {
                //            Survey survey = settings.getSurveyByTime(Calendar.getInstance());
                //            Intent i = new Intent(this, AlarmActivity.class);
                //            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //            int requestCode =
                //                    (now.getTimeInMillis() - survey.getDate().getTimeInMillis() < Constants.TIME_TO_REMINDER * 60 * 1000) ?
                //                    survey.getRequestCode() : survey.getRequestCode() + 1;
                //
                //            i.putExtra(MyAlarmManager.REQUEST_CODE, requestCode);
                //            startActivity(i);
                //            finish();

                settings = Settings.getInstance(this);
                int requestCode = getIntent().getIntExtra(MyAlarmManager.REQUEST_CODE, 0);
                int surveyCode = Survey.getSurveyCode(requestCode);
                String timeTag = settings.getTimeTag(surveyCode);
                Survey curr = settings.getSurveyByTime(now);
                curr.setAlarmed(true);
                curr.setInternet(hasInternet);

                curr.setLastClicked(now); //set this to now.. 8 minutes start after this click

                if (hasInternet) {
                    settings.setTakenSurveyAndSave(this, curr.getRequestCode());
                }
                else {
                    settings.setSurveyAndSave(this, curr.getRequestCode());
                }

                alarmManager.cancelSingleAlarm(this, curr.getRequestCode() + 1);

                String respondingTo = curr.getNotificationTag(now, settings.getTimeToReminder());
                showWebView(UrlBuilder.build(UrlBuilder.PHONE_ALARM, settings, Calendar.getInstance(), true, respondingTo));
                //            showWebView(UrlBuilder.build(UrlBuilder.PHONE_ALARM, settings, Calendar.getInstance(), true)
                //                    + (timeTag == null ? "" : timeTag));
                Log.e("MainActivity", "show survey");

                //  User is logged in, is not during survey, and has not skipped previous but has no alarms set
            } else if (settings.isLoggedIn() && settings.hasNoAlarms() && settings.allFieldsSet() && !settings.shouldShowSurvey(now) && !settings.skippedPrevious(now)) {
                showWebView(UrlBuilder.build(UrlBuilder.PHONE_NOALARMS, settings, now, true));


                //  User is logged in, is not during survey, and has not skipped previous
            } else if (settings.isLoggedIn() && settings.allFieldsSet() && !settings.shouldShowSurvey(now) && !settings.skippedPrevious(now)) {
                showWebView(UrlBuilder.build(UrlBuilder.PHONE_START, settings, now, true));


                //  User is logged in, but has skipped previous
            } else if (settings.isLoggedIn() && settings.allFieldsSet() && !settings.shouldShowSurvey(now) && settings.skippedPrevious(now)) {
                showWebView(UrlBuilder.build(UrlBuilder.PHONE_NOREACTION, settings, now, true));

                //  UserId set from APK; is logged in, but has no start and end times;
            } else if (settings.isLoggedIn() && !settings.allFieldsSet()) {
                showWebView(UrlBuilder.build(UrlBuilder.PHONE_INIT_NODATE, settings, now, true));

                //  No user; either opted out, or started with APK with no RTID
            } else if (!settings.isLoggedIn()) {
                showWebView(UrlBuilder.build("testandroid", settings, now, false));
                //showWebView(UrlBuilder.build(UrlBuilder.PHONE_START, settings, now,  false));
            }
        }
    }

    public ProgressDialog getDialog() {
        return dialog;
    }

    public Settings getSettings() {
        return settings;
    }
    public Texts getTexts() {
        return texts;
    }

    public MyAlarmManager getAlarmManager() {
        return alarmManager;
    }

    private void showWebView(String url){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String cookies = sharedPref.getString(Constants.CookieKey,"");
        String[] temp=cookies.split(";");
        String cookie = "";
        for (String ar1 : temp ){
            if(ar1.contains("PHPSESSID")){
                cookie = ar1;
            }
        }
        Log.i("MainActivity", "cookies come from default " + cookie);
        try {
            if (hasInternet) {

                CookieManager cookieManager = CookieManager.getInstance();

                cookieManager.setAcceptCookie(true);
                cookieManager.acceptCookie();
                cookieManager.setAcceptFileSchemeCookies(true);
                cookieManager.setCookie(url, cookie);
                Log.i("MainActivity", "add PHPSESSID to url " + cookie);




                dialog.show();
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl(url);

            } else {
                this.showMessage("No internet connection detected. Make sure you are connected to the cellular network or wifi.");
                //webView.loadDataWithBaseURL(null, "<html><body><h3><font face=arial color=#5691ea>" +  "No internet connection detected. Make sure you are connected to the cellular network or wifi." + "</font></h3></body></html>", "text/html", "utf-8", null);
            }
        }
        catch (Exception e){

        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

        for(int i = 0; i < menu.size(); i++){
            menu.getItem(i).setTitle(texts.getMenu(i + (i > 2 ? -1 : 0)));
        }

        if(settings.allFieldsSet()){  //
            menu.getItem(2).setEnabled(hasInternet); //depending on internet connection
            menu.getItem(5).setEnabled(true);  //logout button
        } else {
            menu.getItem(2).setEnabled(false);
            menu.getItem(5).setEnabled(false);  //logout button
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                break;
            case R.id.menu_admin:
                    showAdminPasswordDialog();
                break;
            case R.id.menu_refresh:


               // String timeTag = null;
               // showWebView(UrlBuilder.build(UrlBuilder.PHONE_ALARM, settings, Calendar.getInstance(), true) + (timeTag == null ? "" : timeTag));

                //soundAlarm


                route(settings);
                break;
            case R.id.menu_optout:
                alarmManager.cancelAllAlarms(MainActivity.this.getBaseContext());
                settings.clearAndSave(this);
                showWebView(UrlBuilder.build(UrlBuilder.PHONE_OPTOUT, settings, Calendar.getInstance(), true));
                invalidateOptionsMenu();

                //  logEvent(settings, OPT_OUT_EVENT);
                break;
            case R.id.menu_technicalissues:
                    showTechnicalIssuesDialog();
                break;
            case R.id.menu_logout:
                settings.clearAndSave(this);
                texts.clearAndSave(this);
                this.getAlarmManager().cancelAllAlarms(getBaseContext());
                webView.loadUrl(UrlBuilder.build(UrlBuilder.PHONE_LOGOUT, settings, Calendar.getInstance(), true));
                webView.clearCache(true);
                break;
            case R.id.menu_sound:
                    Intent intent2 = new Intent(MainActivity.this, SoundRecordingActivity.class);
                startActivity(intent2);
                break;



        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserFromAPK(){
        try {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File[] files = directory.listFiles();

            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });

            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                if (fileName.startsWith("Zemi_Ptus_")) {
                    String rtid = fileName.substring(10);
                    rtid = rtid.substring(0, rtid.indexOf('_'));
                    if(!rtid.equals("")) settings.updateUserIdAndSave(this, rtid);
                }
            }
        }  catch (Exception e){
            e.printStackTrace();
        }
    }

    /** Request Permissions */
    private final int ALL_PERMISSIONS_REQUEST_CODE = 0;

    private void requestPermissionIfRequired(Activity activity){
        int readStoragePermissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStoragePermissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(readStoragePermissionCheck != PackageManager.PERMISSION_GRANTED ||
                writeStoragePermissionCheck != PackageManager.PERMISSION_GRANTED){
            showAskPermissionDialog(activity);
        }
    }

    // @Override
    // public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    //    switch (requestCode) {
    //        case ALL_PERMISSIONS_REQUEST_CODE: {
    //            // If request is cancelled, the result arrays are empty.
    //            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
    //                startActivity(new Intent(this, MainActivity.class));
    //            } else {
    //                showPermissionDeniedDialog();
    //            }
    //        }
    //    }
    // }

    /** Dialog boxes */
    private void showPermissionDeniedDialog(){
        dismissDialog();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.main_permissions_title_denied));
        alert.setMessage(getResources().getString(R.string.main_permissions_body_denied));
        alert.setPositiveButton(getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dismissDialog();
            }
        });
        alert.show();
    }

    private void showAskPermissionDialog(final Activity activity){
        dismissDialog();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.main_permissions_title_before));
        alert.setMessage(getResources().getString(R.string.main_permissions_body_before));
        alert.setPositiveButton(getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                ActivityCompat.requestPermissions(activity,
                        new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        ALL_PERMISSIONS_REQUEST_CODE);

            }
        });
        alert.show();
    }

    private void showAdminPasswordDialog(){
        texts = Texts.getInstance(this);
        LogUtil.e("admin", texts.toString());
        dismissDialog();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(texts.getMenu(Texts.MenuContent.AdminPassword));
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        alert.setView(input);
        alert.setPositiveButton(getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String password = "bas";
                Editable value = input.getText();
                if (value.toString().equals(password)) {
                    startActivity(new Intent(MainActivity.this, AdminActivity.class));
                    finish();
                } else {
                    Toast.makeText(getBaseContext(), texts.getToast(Texts.ToastContent.WrongPassword), Toast.LENGTH_LONG).show();
                }
            }
        });
        alert.show();
    }

    private void showTechnicalIssuesDialog(){
        String versionName = BuildConfig.VERSION_NAME;
        String unformatted = getResources().getString(R.string.main_technicalissues_body);

        String formated = String.format(unformatted, versionName, (settings.getRtid() == null) ? "" : settings.getRtid(), buildSystemInfo());
        new AlertDialog.Builder(this)
                .setTitle(texts.getMenu(Texts.MenuContent.TechIssue))
                .setMessage(formated)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void dismissDialog(){
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    /** System Info **/
    private String buildSystemInfo(){
        return  "OS Version: " + System.getProperty("os.version") + "(" + Build.VERSION.INCREMENTAL + ")" +
                "\n OS API Level: "+ Build.VERSION.RELEASE + "("+ Build.VERSION.SDK_INT+")" +
                "\n Device: " + Build.DEVICE +
                "\n Model (and Product): " + Build.MODEL + " ("+ Build.PRODUCT + ")";
    }

    //    public static String
    //            SIGN_UP_EVENT = "SIGN_UP_EVENT",
    //            OPT_OUT_EVENT = "OPT_OUT_EVENT";

    //    public void logEvent(Settings settings, String type){
    //        String SETTINGS = "SETTINGS";
    //        Bundle bundle = new Bundle();
    //        bundle.putString(SETTINGS, settings.toString());
    //        mFirebaseAnalytics.logEvent(type, bundle);
    //    }

    BroadcastReceiver networkAvailableReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            hasInternet = (cm.getActiveNetworkInfo() != null);
            //reload page!
            route(settings);
        }
    };

//    protected void onStop()
//    {
//        unregisterReceiver(this);
//        super.onStop();
//    }
    public void  startAcceService(){
        Log.d("TryToStartAccService", "enter start Acc");
//        Intent accelermoterIntent = new Intent(this, AccelerometerService.class);
//        startService(accelermoterIntent);
//        AcceFileManager.initFile(this,settings.getRtid());
        if(settings.getAccelrecording() == 1){
            // start accelermoter service
            Intent accelermoterIntent = new Intent(this, AccelerometerService.class);
            startService(accelermoterIntent);
            AcceFileManager.initFile(this,settings.getRtid());
        } else {
            Log.d("MainActivity", "acc service is not required to start");
        }

    }

    public void showMessage(String message){
        webView.loadDataWithBaseURL(null, "<html><body><h3><font face=arial color=#5691ea>" +  message + "</font></h3></body></html>", "text/html", "utf-8", null);
    }

}
