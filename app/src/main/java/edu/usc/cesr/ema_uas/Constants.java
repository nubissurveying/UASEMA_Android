package edu.usc.cesr.ema_uas;

public class Constants {

    public static boolean isDemo = false;

//    public static int TIME_BETWEEN_SURVEYS_PRO = 45;
    public static int TIME_BETWEEN_SURVEYS_PRO = 90;
    private static int TIME_TO_TAKE_SURVEY_PRO = 15;     //was 8
    private static int TIME_TO_REMINDER_PRO = 6;

    public static final int TIME_BETWEEN_SURVEYS_DEV = 5;
    private static final int TIME_TO_TAKE_SURVEY_DEV = 2;
    private static final int TIME_TO_REMINDER_DEV = 1;

    public static  int TIME_TO_TAKE_SURVEY = isDemo ? TIME_TO_TAKE_SURVEY_DEV : TIME_TO_TAKE_SURVEY_PRO;
    public static  int TIME_TO_REMINDER = isDemo ? TIME_TO_REMINDER_DEV : TIME_TO_REMINDER_PRO;
    public static final String CookieKey = "CookieKeyForEMA";
}
