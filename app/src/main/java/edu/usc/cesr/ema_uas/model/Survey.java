package edu.usc.cesr.ema_uas.model;

import java.io.Serializable;
import java.util.Calendar;

import edu.usc.cesr.ema_uas.util.DateUtil;

@SuppressWarnings("WeakerAccess")
public class Survey implements Serializable {
    private int requestCode;
    private Calendar date;
    private boolean taken;
    private boolean closed;



    private boolean alarmed;

    public Survey(int requestCode, Calendar date) {
        this.requestCode = requestCode;
        this.date = date;
        this.taken = false;
        this.closed = false;
        this.alarmed = false;
    }

    @Override
    public String toString() {
        return "Code: " + requestCode + " Alarmed: "+ isAlarmed()+  " Taken: " + isTaken() + " closed: " + closed + " Date: " + DateUtil.stringifyAll(date);
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
}
