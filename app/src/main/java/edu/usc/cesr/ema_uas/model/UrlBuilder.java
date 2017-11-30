package edu.usc.cesr.ema_uas.model;

import android.net.Uri;

import java.util.Calendar;

import edu.usc.cesr.ema_uas.util.DateUtil;
import edu.usc.cesr.ema_uas.util.LogUtil;

@SuppressWarnings({"WeakerAccess", "unused", "FieldCanBeLocal"})
public class UrlBuilder {
    public static final String
            DOWNLOAD = "download",
            DOWNLOAD_APP = "download.app",
            NO_INIT = "noinit",                         //  Used
            OTHER_DAY = "other.day",
            OTHER_DAY_RES = "other.day.res",
            OTHERDAY = "otherday",
            OTHERDAY_RES = "otherday.res",
            PARTICIPATE = "participate",
            PARTICIPATE_RES = "participate.res",
            PHONE_ALARM = "phone.alarm",                //  Shows alarm if rtid != null
            PHONE_DEMO = "phone.demo",                  //  Demo (Different with testing?)
            PHONE_INIT_NODATE = "phone.init.nodate",    //  Prevent
            PHONE_INIT_NOID = "phone.init.noid",        //  Prevent
            PHONE_LOGIN_RES = "phone.login.res",
            PHONE_NOREACTION = "phone.noreaction",      //  Passed: Selected time and date passed
            PHONE_OPTOUT = "phone.optout",              //  Opt out
            PHONE_START = "phone.start",                //  Start: Used if no user
            PHONE_LOGOUT = "logout",
            SENDMAIL = "sendmail",
            SETTINGS_CHANGE = "settings.change",        //  Used for block_between, passed, (master)
            TEST = "test",
            TEST_MODE = "testmode",                     //  Testing
            VIDEO_BEEP_MESSAGE = "video.beepmessage";   //  Default?

    public static final String
            TIME_FIRST = "&first=1",
            TIME_MIDDLE = "&middle=1",
            TIME_LAST = "&last=1";

    private static String buildParams(String page, Settings settings, Calendar now){
        return "&rtid=" + (settings.getRtid() == null ? "" : Uri.encode(settings.getRtid())) +
            "&language=" + "en" +
            "&device=" + "android" +
            "&email=" +
            "&selecteddate=" + DateUtil.stringifyDate(settings.getBeginTime()) +    //  Not encoded?
            "&date=" + Uri.encode(DateUtil.stringifyAll(now)) +
            "&starttime=" + Uri.encode(DateUtil.stringifyTime(settings.getBeginTime())) +
            "&endtime=" + Uri.encode(DateUtil.stringifyTime(settings.getEndTime())) +
            "&pinginfo=" + (page.equals(PHONE_ALARM) ? Uri.encode(settings.alarmTags()) : "");
    }

    private static String baseURL = "https://uas.usc.edu/survey/uas/ema/daily/index.php";

    public static String build(String page, Settings settings, Calendar now, boolean includeParams){
        String response = baseURL + "?ema=1&p=" + page + (includeParams ? buildParams(page, settings, now) : "");
        LogUtil.e("TT", "UrlBuilder => build() == " + response);
        return response;
    }
}
