package edu.usc.cesr.ema_uas.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.usc.cesr.ema_uas.model.Settings;

import static android.content.ContentValues.TAG;

/**
 * Created by cal on 1/3/18.
 */

public class AcceFileManager {


    private static String filename = "";
    private static String initString = "";
    private static Settings settings;
    private static String rtid = "";
    static String HTTPReturnString = "";
    private Context context;


    public AcceFileManager(Context context, String rtid){
        this.context = context;
        filename = context.getFilesDir().getPath() + "/" + rtid + "_acce_data_android.txt";
        settings = Settings.getInstance(context);
    }

    public static void  initFile(Context context,String rtid){
        initString = rtid + "\n";
        filename = context.getFilesDir().getPath() + "/" + rtid + filename;
//        if(checkExist(context,rtid)) return ;
        FileOutputStream outputStream;

        try {
            outputStream = new FileOutputStream(filename);
            outputStream.write(initString.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("on create", "acc file created");


    }
    public static void appendFile(Context context, String str){

        FileOutputStream outputStream;

        try {
            outputStream = new FileOutputStream(filename,true);
            outputStream.write(str.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Log.d("on create", "acc file append");
    }
    public static boolean checkExist(Context context){
        File ex = new File(filename);
        return ex.exists();
    }
    public static String loadFile(Context context){

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String str = "";
        String line;
        try{
            while((line=bufferedReader.readLine()) != null){
                str += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            fileInputStream.getChannel().position(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("load file content", str);
        return str;
    }
    public static void uplaodFile(Context context){
        settings = Settings.getInstance(context);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Log.d("AccFileManager", "upload File");
        try {
            NubisDelayedAnswer delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_POST_FILE);
            delayedanswer.addGetParameter("version", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
            delayedanswer.addGetParameter("rtid", settings.getRtid());
            delayedanswer.addGetParameter("phonets", formatter.format(Calendar.getInstance().getTimeInMillis()));
            delayedanswer.addGetParameter("p", "uploadacceldata");
            delayedanswer.addGetParameter("ema", "1");
            delayedanswer.addFileName(filename);
            delayedanswer.setByteArrayOutputStream();
            upLoad(context,delayedanswer, true, -1, NubisHTTP.H_UPLOAD);


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void upLoad(Context context, NubisDelayedAnswer delayedAnswer, boolean wait, int deleteId, int communicationType) {
        //Context context, NubisDelayedAnswer delayedAnswer, NubisAsyncResponse delegate
        try {
            NubisHTTP httpCom = new NubisHTTP(context, delayedAnswer, null, deleteId, communicationType, settings);
            if (wait) {
                httpCom.serverInstructions = "";
                httpCom.execute(); //doInBackground();//.get(210000, TimeUnit.MILLISECONDS);

                HTTPReturnString = httpCom.serverInstructions;

            } else {
                httpCom.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetFile(Context context, String rtid){
        if(!checkExist(context)) initFile(context,rtid);
        try {
            FileOutputStream overWrite = new FileOutputStream(filename,false);
            overWrite.write((rtid +"\n").getBytes());
            overWrite.flush();
            overWrite.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



}
