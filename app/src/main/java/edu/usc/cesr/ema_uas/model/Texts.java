package edu.usc.cesr.ema_uas.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.usc.cesr.ema_uas.util.LogUtil;

/**
 * Created by cal on 4/15/18.
 */

public class Texts {
    private static Texts mInstance;
    private static String PTUS_TEXTS = "PTUS_TEXTS";
    private static Gson gson = new Gson();

    private String[] defaultNotificaton = {"Survey is ready", "No need to click after "};
    private String[] toast = {"Logout", "Wrong password", "Please login first"};



    private String[] defaultMenu = {"Admin","Refresh","Sound recording","Technical issue","Logout","Menu","Cancel","Please enter the admin password"};
    private String[] recording = {"Press the microphone button to start recording. Press the button again to stop and save.",
            "After recording is done, you can save the recording. You can also start over with a new recording by pressing the microphone button again.",
            "Make sure your video is longer then 10s and less then 4min. After recording, click use video to save and upload in recording result screen."};


    private static final String TAG = "Texts";
    private static final String NOTIFICATION_KEY = "notification";
    private static final String MENU_KEY = "menu";
    private static final String TOAST_KEY = "toast";
    private static final String RECORDING_KEY = "recording";

    private Texts(){}

    public enum MenuContent{
        Admin(0),
        Refresh(1),
        SoundRecording(2),
        TechIssue(3),
        Logout(4),
        Menu(5),
        Cancel(6),
        AdminPassword(7);
        private int index;
        private MenuContent(int index){
            this.index = index;
        }
        public int getIndex(){
            return index;
        }
    }
    public enum NotificationContent{
        title(0),
        message(1);

        private int index;
        private NotificationContent(int index){this.index = index;}
        public int getIndex(){return index;}

    }
    public enum ToastContent {
        Logout(0),
        WrongPassword(1),
        LoginAlert(2);
        private int index;
        private ToastContent(int index){this.index = index;}
        public int getIndex(){return index;}
    }
    public enum RecordingContent{

    }

    public String getToast(ToastContent type){
        if(type.getIndex() < toast.length){
            return toast[type.getIndex()];
        } else return "ToastType error";
    }

    public String getNotification(NotificationContent type){
        if(type.getIndex() < defaultNotificaton.length)
            return defaultNotificaton[type.getIndex()];
        else return "notificationType error";
    }
    public String getNotification(int index){
        if(index < defaultNotificaton.length)
            return defaultNotificaton[index];
        else return "notificationType error";
    }

    public String getMenu(MenuContent type){
        if(type.getIndex() < defaultMenu.length)
            return defaultMenu[type.getIndex()];
        else return "menuType error";
    }

    public String getMenu(int index){
        if(index < defaultMenu.length)
            return defaultMenu[index];
        else return "menuType error";
    }


    public void updateTextSettings(Context context, String input){
        Log.d(TAG , input);
        try {
            JSONObject json = new JSONObject(input).getJSONObject("text");
            Log.d(TAG, json.toString());
            JSONArray notificationTemp = json.getJSONArray(NOTIFICATION_KEY);

            int len = notificationTemp.length();
            String[] Notificaton = new String[len];
            for(int i = 0; i < len; i++){
                Notificaton[i] = notificationTemp.getString(i);
                Log.d(TAG + "notification", i + ":" + Notificaton[i]);
            }
            JSONArray menuTemp = json.getJSONArray(MENU_KEY);
            len = menuTemp.length();
            String[] Menu = new String[len];
            for(int i = 0; i < len; i++){
                Menu[i] = menuTemp.getString(i);
                Log.d(TAG + "menu", i + ":" + Menu[i]);
            }

            JSONArray toastTemp = json.getJSONArray(TOAST_KEY);
            len = toastTemp.length();
            String[] Toast = new String[len];
            for(int i = 0; i < len; i++){
                Toast[i] = toastTemp.getString(i);
                Log.d(TAG + "toast", i + ":" + Toast[i]);
            }

            JSONArray recordTemp = json.getJSONArray(RECORDING_KEY);
            len = recordTemp.length();
            String[] record = new String[len];
            for(int i = 0; i < len; i++){
                record[i] = recordTemp.getString(i);
                Log.d(TAG + "record", i + ":" + record[i]);
            }

            this.setDefaultMenu(Menu);
            this.setDefaultNotificaton(Notificaton);
            this.setToast(Toast);
            this.setRecording(record);

            save(context);

        } catch (JSONException e) {
            e.printStackTrace();
            LogUtil.e(TAG,"jsonObject not exist");
        }


    }
    /** Load, Save, Clear */
    public static Texts getInstance(Context context){
        if(mInstance == null) {
            mInstance = build(loadTexts(context));
        } return mInstance;
    }
    private static String loadTexts(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PTUS_TEXTS, "");
    }
    private static Texts build(String json){
        if(json.equals("")){
            return new Texts();
        } else {
            return gson.fromJson(json, Texts.class);
        }
    }
    public void clearAndSave(Context context){
        String[] resetNotificaton = {"Survey is ready", "No need to click after "};
        String[] resettoast = {"Logout", "Wrong password", "Please login first"};
        String[] resetMenu = {"Admin","Refresh","Sound recording","Technical issue","Logout","Menu","Cancel","Please enter the admin password"};
        String[] resetrecording = {"Press the microphone button to start recording. Press the button again to stop and save.",
                "After recording is done, you can save the recording. You can also start over with a new recording by pressing the microphone button again.",
                "Make sure your video is longer then 10s and less then 4min. After recording, click use video to save and upload in recording result screen."};


        defaultNotificaton = resetNotificaton;
        defaultMenu = resetMenu;
        toast = resettoast;
        recording = resetrecording;
        save(context);
    }

    private void save(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PTUS_TEXTS, toJson());
        editor.commit();
    }
    private String toJson(){
        return gson.toJson(this);
    }


    public String[] getDefaultNotificaton() {
        return defaultNotificaton;
    }


    public void setDefaultNotificaton(String[] defaultNotificaton) {
        this.defaultNotificaton = defaultNotificaton;
    }

    public String[] getDefaultMenu() {
        return defaultMenu;
    }

    public void setDefaultMenu(String[] defaultMenu) {
        this.defaultMenu = defaultMenu;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("notificationMessage =>");
        for(String str: defaultNotificaton) sb.append(str).append(" ");
        sb.append('\n');
        sb.append("MenuMessage =>");
        for (String str : defaultMenu) sb.append(str).append(" ");
        sb.append('\n');
        sb.append("toast =>");
        for (String str : toast) sb.append(str).append(" ");
        sb.append('\n');
        sb.append("record =>");
        for (String str : recording) sb.append(str).append(" ");
        return sb.toString();
    }
    public void setToast(String[] toast) {
        this.toast = toast;
    }

    public void setRecording(String[] recording) {
        this.recording = recording;
    }
}
