package edu.usc.cesr.ema_uas.Service;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import edu.usc.cesr.ema_uas.R;
import edu.usc.cesr.ema_uas.model.Settings;
import edu.usc.cesr.ema_uas.ui.MainActivity;
import edu.usc.cesr.ema_uas.util.DateUtil;
import edu.usc.cesr.ema_uas.util.AcceFileManager;

/**
 * Created by cal on 1/3/18.
 */

public class AccelerometerService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    static int count=0;
    private Sensor mySensor;
    private Calendar start;
    private Calendar startUpload;
    private Calendar startAppend;
    private  double acc = 0;
    private double stantardGravity = 9.81;
    private long SVMCalInterval = 1000;
    private long uploadInterval = 60*60*1000;
    private long appendInterval = 60*1000/2;
    private boolean hasInternet = true;
    private String appendBuffer = "";

    @Nullable
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //registering Sensor
        mySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);

        //then you should return sticky
        return Service.START_STICKY;
    }
    @Override
    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("UASEma")
                .setContentText("Collecting accelerometer data")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);
    }
    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get sensor when Change
//        Log.d("accelerometer ", "X: " + event.values[0]+ " Y: " + event.values[1] + " Z: " + event.values[2]);

        if (start == null) start = Calendar.getInstance();
        if (startUpload == null) startUpload = Calendar.getInstance();
        if (startAppend == null) startAppend = Calendar.getInstance();

        long diff = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis() ;
        long diffUpload = Calendar.getInstance().getTimeInMillis() - startUpload.getTimeInMillis() ;

        long diffAppend = Calendar.getInstance().getTimeInMillis() - startAppend.getTimeInMillis() ;
        if(diff > SVMCalInterval) {
            appendBuffer += DateUtil.stringifyAllDash(Calendar.getInstance()) + " " + acc +"\n";
//            Log.d("show curr acc", acc + "");
            acc = 0;
            start = Calendar.getInstance();
//            AcceFileManager.loadFile(getApplicationContext());

        }
        else if(AcceFileManager.checkExist(getApplicationContext()) && diffAppend > appendInterval){
            AcceFileManager.appendFile(getApplicationContext(),appendBuffer);
            appendBuffer = "";
            startAppend = Calendar.getInstance();
        }
        else if (diffUpload > uploadInterval && hasInternet){
            Log.d("AccServece","time to upload");
            AcceFileManager.uplaodFile(getApplicationContext());
//            AcceFileManager.initFile(getApplicationContext(), Settings.getInstance(getApplicationContext()).getRtid());
//            AcceFileManager.loadFile(getApplicationContext());
            AcceFileManager.resetFile(getApplicationContext(),Settings.getInstance(getApplicationContext()).getRtid());
            startUpload = Calendar.getInstance();
        }
        calculateSVM(event.values[0]/stantardGravity, event.values[1]/stantardGravity, event.values[2]/stantardGravity);

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    public void calculateSVM(double x, double y, double z){
//        Log.d("calculate svm", "" + (Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2)) - 1));
        acc += Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2)) - 1;

    }
    BroadcastReceiver networkAvailableReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            hasInternet = (cm.getActiveNetworkInfo() != null);
        }
    };

}
