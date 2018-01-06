package edu.usc.cesr.ema_uas.model;

import android.content.Context;
import android.util.Log;

import org.json.*;

import java.util.Calendar;
import java.util.Date;

import edu.usc.cesr.ema_uas.Constants;
import edu.usc.cesr.ema_uas.model.Settings;

/**
 * Created by cal on 12/8/17.
 */

public class JSONParser {
    static String inputJson = "{\"rtid\":65,\"pings\":[\"2017-12-07 10:20:00\",\"2017-12-07 11:15:00\",\"2017-12-07 12:11:00\",\"2017-12-07 13:14:00\"," +
            "\"2017-12-07 14:33:00\",\"2017-12-07 15:56:00\",\"2017-12-07 16:31:00\"," +
            "\"2017-12-08 08:43:00\",\"2017-12-08 09:23:00\"]," +
            "\"reminder\":120," +
            "\"windowopen\":560}";
    static public void updateSettingSample(){
        try {
            JSONObject json = new JSONObject(inputJson);
            Log.d("JSONParser", json.toString());
            Log.d("JSONParser ", json.getString("rtid"));
            Date beginDate = new Date();
            Calendar beginCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();
            beginCal.setTime(beginDate);
            endCal.setTime(beginDate);
            endCal.add(Calendar.DAY_OF_MONTH, 7); //just add 7 days..
            Settings settings = new Settings(json.getString("rtid"), beginCal, endCal, beginCal, Settings.buildSurveysFromJson(json));
            Log.d("JSONParser", settings.toString());
            Constants.TIME_TO_REMINDER = json.getInt("reminder")/60;
            Constants.TIME_TO_TAKE_SURVEY = json.getInt("windowopen")/60;
            Log.d("JSONParser", "reminder and open should change" + Constants.TIME_TO_REMINDER + " " + Constants.TIME_TO_TAKE_SURVEY);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
