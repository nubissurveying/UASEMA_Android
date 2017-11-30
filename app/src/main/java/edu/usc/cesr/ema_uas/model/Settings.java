package edu.usc.cesr.ema_uas.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import edu.usc.cesr.ema_uas.Constants;
import edu.usc.cesr.ema_uas.util.DateUtil;

@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
@SuppressLint("CommitPrefEdits")
public class Settings {
    private static Settings mInstance;
    private static String PTUS_SETTINGS = "PTUS_SETTINGS";
    private static Gson gson = new Gson();

    private boolean loggedIn = false;
    private String rtid;
    private Calendar beginTime;
    private Calendar endTime;
    private List<Survey> surveys;
    private Calendar setAtTime;

    public String serverURL = "https://uas.usc.edu/survey/uas/ema/daily/index.php";

    private Settings() {
    }

    /** Used to create new Settings Object from Admin panel; this object is not saved; only used to build url parameters */
    public Settings(String rtid, Calendar beginTime, Calendar endTime) {
        this.rtid = rtid;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }

    /** Getters && Setters */
    /** Update Settings (Only updated by ChromeView Alert; does not overwrite rtid unless rtid == null || "" ) */
    public void updateAndSave(Context context, String rtid, Calendar beginTime, Calendar endTime, Calendar setAtTime){
        this.loggedIn = true;
        this.rtid = (rtid.equals("")) ? this.rtid : rtid;   //  rtid == "" when changing settings after logging out;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.surveys = (Constants.isDemo) ? buildSurveysDev(beginTime, endTime) : buildSurveysPro(beginTime, endTime);
        this.setAtTime = setAtTime;
        save(context);
    }
    public void updateUserIdAndSave(Context context, String rtid){
        loggedIn = true;
        this.rtid = rtid;
        save(context);
    }
    public List<Survey> getSurveys() {
        return surveys;
    }
    public Calendar getBeginTime() {
        return beginTime;
    }
    public Calendar getEndTime() {
        return endTime;
    }
    public String getRtid() {
        return rtid;
    }
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /** Should take survey; Set survey as taken */
    public void setTakenSurveyAndSave(Context context, int requestCode){
        Survey currentSurvey = getSurveyByCode(requestCode);
        if(currentSurvey != null){
            currentSurvey.setAsTaken();
        }
        save(context);
    }
    public void setClosedSurveyAndSave(Context context, int requestCode){
        Survey currentSurvey = getSurveyByCode(requestCode);
        if(currentSurvey != null){
            currentSurvey.setClosed();
        }
        save(context);
    }
    public Survey getSurveyByCode(int requestCode){
        int surveyCode = Survey.getSurveyCode(requestCode);
        for(int i = 0; i < surveys.size(); i++){
            if(surveys.get(i).getRequestCode() == surveyCode) return surveys.get(i);
        } return null;
    }
    public Survey getSurveyByTime(Calendar now){
        for(int i = 0; i < surveys.size(); i++){
            double timeDiffInMin = ((double) (now.getTimeInMillis() - surveys.get(i).getDate().getTimeInMillis())) /  (60 * 1000);
            if(0 < timeDiffInMin && timeDiffInMin < Constants.TIME_TO_TAKE_SURVEY) return surveys.get(i);
            else if( 0 < timeDiffInMin) surveys.get(i).setClosed();
        } return null;
    }
    public boolean shouldShowSurvey(Calendar calendar){
        Survey currentSurvey = getSurveyByTime(calendar);
        return currentSurvey != null && !currentSurvey.isClosed();
//        return currentSurvey != null && !currentSurvey.isTaken() && !currentSurvey.isClosed();
    }

    /** User logged in && ready to take surveys */
    public boolean allFieldsSet(){
        return !(rtid == null || beginTime == null || endTime == null);
    }

    /** Time Tag */
    public String getTimeTag(int requestCode){
        int position = requestCode / 3;
        if(position == surveys.size() - 1){
            return UrlBuilder.TIME_LAST;
        } else if (position == 0) {
            return UrlBuilder.TIME_FIRST;
        } else if (surveys.size() / 2 <= position){
            return UrlBuilder.TIME_MIDDLE;
        } else return null;
    }

    /**Build alarm times as url param*/
    public String alarmTags(){
        JsonObject json = new JsonObject();
        if(surveys != null){
            for(int i = 0; i < surveys.size(); i++){
                Survey cur = surveys.get(i);
                json.addProperty(String.valueOf(i + 1), DateUtil.stringifyTime(cur.getDate()) + (cur.isAlarmed()?"T":"F") + (cur.isTaken()? "T":"F") +(cur.isClosed()? "T" :"F") );
            }
        }
        return json.toString();
    }

    /** Load, Save, Clear */
    public static Settings getInstance(Context context){
        if(mInstance == null) {
            mInstance = build(loadSettings(context));
        } return mInstance;
    }
    private static String loadSettings(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PTUS_SETTINGS, "");
    }
    private static Settings build(String json){
        if(json.equals("")){
            return new Settings();
        } else {
            return gson.fromJson(json, Settings.class);
        }
    }

    public void clearAndSave(Context context){
        loggedIn = false;
        beginTime = null;
        endTime = null;
        surveys = null;
        save(context);
    }
    private void save(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PTUS_SETTINGS, toJson());
        editor.commit();
    }
    private String toJson(){
        return gson.toJson(this);
    }

    /** Set alarms from dates */
    private static List<Survey> buildSurveysDev(Calendar start, Calendar end){

        int minute = 60 * 1000;
        int minutesDiff = (int) (end.getTimeInMillis() - start.getTimeInMillis()) / minute;
        List<Integer> beepMinDiff = new ArrayList<>();

        for(int count = 0,i = 1; count < 60 && i < minutesDiff; count ++ ,i += Constants.TIME_BETWEEN_SURVEYS_DEV) {
            beepMinDiff.add(i);
        }

        List<Survey> alarmTimes = new ArrayList<>();
        for(int i = 0; i < beepMinDiff.size(); i++){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, beepMinDiff.get(i));
            Log.e("Survey generate", "for time " + DateUtil.stringifyAll(calendar));
            alarmTimes.add(new Survey(i * 3, calendar));
        }
        return alarmTimes;
    }
    public static List<Survey> buildSurveysPro(Calendar start, Calendar end){

        int minute = 60 * 1000;
        int timeDiffInMin = (int) (end.getTimeInMillis() - start.getTimeInMillis()) / minute;
        int counter = 0;

        Random rand = new Random();
        List<Integer> randomBeepsMinDiff = new ArrayList<>();
        while(counter <= timeDiffInMin){
            int randNum = (counter == 0) ? 5 : rand.nextInt(15) + Constants.TIME_BETWEEN_SURVEYS_PRO;
            counter += randNum;
            if(counter <= timeDiffInMin) randomBeepsMinDiff.add(counter);
        }

        List<Survey> surveys = new ArrayList<>();
        for(int i = 0; i < randomBeepsMinDiff.size(); i++){
            Calendar calendar = (Calendar) start.clone();
            calendar.add(Calendar.MINUTE, randomBeepsMinDiff.get(i));
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            if (hours >= 10 && hours <= 20) { //don't add before 10 and after 9
                surveys.add(new Survey(i * 3, calendar));
            }
        }
        return surveys;
    }

    /** Logging */
    @Override
    public String toString() {
        return "TT Settings => " +
                "\n allFieldsSet: " + allFieldsSet() +
                "\n loggedIn: " + loggedIn +
                "\n rtid: " + rtid +
                "\n begin: " + DateUtil.stringifyAll(beginTime) +
                "\n end: " + DateUtil.stringifyAll(endTime) +
                "\n surveys: " + ((surveys != null) ? surveys.size() : "null") +
                "\n" + stringifyAlarms(surveys);
    }
    private String stringifyAlarms(List<Survey> surveys){
        if(surveys != null){
            String string = "";
            for(int i = 0; i < surveys.size(); i++){
                string += (surveys.get(i) + "\n");
            }
            return string;
        } else return "null";
    }

    public boolean skippedPrevious(Calendar now){
        Survey previous = getPreviousSurvey(now);
        //  If no previous surveys, obviously no surveys have been skipped
        if(previous == null){
            Log.e("TT", "Settings => A");
            return false;
        //  Last survey is last, don't show NO_REACTION screen
        } else if (previous.getRequestCode() == surveys.get(surveys.size() - 1).getRequestCode()) {
            Log.e("TT", "Settings => B");
            return false;
        //  Don't show NO_REACTION if setAtTime is between previous survey time and now
        } else if (previous.getDate().getTimeInMillis() < setAtTime.getTimeInMillis() && setAtTime.getTimeInMillis() < now.getTimeInMillis()){
            Log.e("TT", "Settings => C");
            return false;
        //  Show NO_REACTION if previous survey skipped
        } else {
            Log.e("TT", "Settings => D");
            return !previous.isTaken();
        }
    }
    private Survey getPreviousSurvey(Calendar now){
        for(int i = surveys.size() - 1; i >= 0; i--){
            if(surveys.get(i).getDate().before(now)) return surveys.get(i);
        } return null;
    }
}
