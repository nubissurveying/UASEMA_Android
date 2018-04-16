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

    private String[] defaultNotificaton = {"调查问卷准备好了"};



    private String[] defaultMenu = {"管理员","刷新","录音","技术问题","登出"};


    private static final String TAG = "Texts";
    private static final String NOTIFICATION_KEY = "notification";
    private static final String MENU_KEY = "menu";

    private Texts(){}

    public enum MenuContent{
        Admin(0),
        Refresh(1),
        SoundRecording(2),
        TechIssue(3),
        Logout(4);
        private int index;
        private MenuContent(int index){
            this.index = index;
        }
        public int getIndex(){
            return index;
        }
    }
    public enum NotificationContent{
        title(0);

        private int index;
        private NotificationContent(int index){this.index = index;}
        public int getIndex(){return index;}

    }

    public String getNotification(NotificationContent type){
        if(type.getIndex() < defaultNotificaton.length)
            return defaultNotificaton[type.getIndex()];
        else return "notificationType error";
    }

    public String getMenu(MenuContent type){
        if(type.getIndex() < defaultMenu.length)
            return defaultMenu[type.getIndex()];
        else return "menuType error";
    }


    public void updateTextSettings(Context context, String input){

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
            this.setDefaultMenu(Menu);
            this.setDefaultNotificaton(Notificaton);
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
        String[] resetNotificaton =  {"调查问卷准备好了"};
        String[] resetMenu = {"管理员","刷新","录音","技术问题","登出"};
        defaultNotificaton = resetNotificaton;
        defaultMenu = resetMenu;
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
        return sb.toString();
    }

}
