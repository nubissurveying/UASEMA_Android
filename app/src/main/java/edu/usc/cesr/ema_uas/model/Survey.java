package edu.usc.cesr.ema_uas.model;

import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import edu.usc.cesr.ema_uas.util.DateUtil;

@SuppressWarnings("WeakerAccess")
public class Survey implements Serializable {
    private int requestCode;
    private Calendar date;
    private boolean taken;
    private boolean closed;
    private Calendar lastClicked = null;


    private boolean internet;



    private boolean alarmed;

    public Survey(int requestCode, Calendar date) {
        this.requestCode = requestCode;
        this.date = date;
        this.taken = false;
        this.closed = false;
        this.alarmed = false;
        this.internet = false;
    }

    @Override
    public String toString() {
        return "Code: " + requestCode + " Alarmed: "+ isAlarmed() + " Internet: "+ isInternet()+ " Taken: " + isTaken() + " closed: " + closed + " Date: " + DateUtil.stringifyAll(date);
    }

    public int getRequestCode() {
        return requestCode;
    }

    public boolean isAlarmed() {
        return alarmed;
    }

    public void setAlarmed(boolean alarmed) {
        this.alarmed = alarmed;
    }
    public Calendar getDate() {
        return date;
    }

    public Calendar getLastClicked(){
        return this.lastClicked;
    }

    public void setLastClicked(Calendar lastClicked) { //this is actually 'first clicked'
        if (this.lastClicked == null) { //only set if null
            this.lastClicked = lastClicked;
        }
    }

    public void setAsTaken(){
        this.taken = true;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setClosed(){
        this.closed = true;
    }

    public boolean isClosed(){
        return closed;
    }


    public static int getSurveyCode(int requestCode){
        return requestCode - (requestCode % 3);
    }

    public boolean isInternet() {
        return internet;
    }

    public void setInternet(boolean internet) {
        this.internet = internet;
    }

    public String getNotificationTag(Calendar now, int timeToReminder) {
        int baseInd = (requestCode / 3) * 2 + 2;
//        int elapsed = (int)(now.getTimeInMillis() - date.getTimeInMillis()/ 1000);
        long elapsed = (now.getTimeInMillis() - date.getTimeInMillis());
        Log.d("getNotificatinTag",DateUtil.stringifyAll(now) + " " + DateUtil.stringifyAll(date) + " " + elapsed);
        if(elapsed < timeToReminder * 60 * 1000){
            baseInd -= 1;
        }
        return baseInd + "";

    }
}
