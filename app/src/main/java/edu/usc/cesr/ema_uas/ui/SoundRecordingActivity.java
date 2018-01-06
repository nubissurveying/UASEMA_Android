package edu.usc.cesr.ema_uas.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import edu.usc.cesr.ema_uas.R;
import edu.usc.cesr.ema_uas.model.Settings;
import edu.usc.cesr.ema_uas.util.NubisDelayedAnswer;
import edu.usc.cesr.ema_uas.util.NubisHTTP;

public class SoundRecordingActivity extends AppCompatActivity {

    private Settings settings;

    private ImageButton recordButton;
    private com.beardedhen.androidbootstrap.BootstrapButton saveButton;
    private MediaRecorder mRecorder = null;
    private boolean recording = false;
    private boolean soundrecorded = false;
    private int numberOfRecordings = 0;
    private String mFileName;

    private Timer RecordingTimer;

    String HTTPReturnString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_recording);

        settings = Settings.getInstance(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
            getSupportActionBar().setIcon(R.drawable.uas_logo);
        }

        saveButton = (com.beardedhen.androidbootstrap.BootstrapButton) findViewById(R.id.saveOpenEndedButton);
        saveButton.setEnabled(false);
        //saveButton.setText(((NubisApplication)getApplicationContext()).settings.texts.getText("saveResponsesButton"));
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchSaveIntent(v);
            }
        });

        final Context context = this;

        recordButton = (ImageButton) findViewById(R.id.openEndedRecordSoundPictureButton);
        recordButton.setImageResource(R.drawable.microphone);
        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (numberOfRecordings > 1 && numberOfRecordings % 2 == 0) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("This will overwrite your previously recorded message. Are you sure you want to continue?");
                    //alert.setTitle("请输入密码");
                    // Set an EditText view to get user input
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            numberOfRecordings++;
                            dispatchRecordIntent();
                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });

                    alert.show();
                }
                else { //first click
                    numberOfRecordings++;
                    dispatchRecordIntent();
                }
            }
        });

        mFileName = Environment.getExternalStorageDirectory().getPath() + "/" + "sound" + ".3gp";

    }

    public void dispatchSaveIntent(View v) {
        try {
            this.stopRecording(); //make sure it is not recording anymore..

            recordButton.setEnabled(false);

            saveButton.setEnabled(false);
            Calendar now = Calendar.getInstance();
            now.setTimeInMillis(System.currentTimeMillis());

            String fileName;
            ByteArrayOutputStream out;
            OutputStream outputStreamFile;


            if (soundrecorded){
                //add sound
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                NubisDelayedAnswer delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_POST_FILE);
                delayedanswer.addGetParameter("version", this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
                delayedanswer.addGetParameter("rtid", settings.getRtid());
                delayedanswer.addGetParameter("phonets", formatter.format(now.getTimeInMillis()));
                delayedanswer.addGetParameter("p", "openendedsound");
                delayedanswer.addGetParameter("ema", "1");
                delayedanswer.addFileName(mFileName);
                delayedanswer.setByteArrayOutputStream();

//                ((NubisApplication)getApplicationContext()).communication.addNubisDelayedAnswer(delayedanswer);

                this.upLoad(delayedanswer, true, -1, NubisHTTP.H_UPLOAD);

            }
            //else {
                final Intent intent2 = new Intent(this, MainActivity.class);
                startActivity(intent2);
            //}

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void stopRecording(){
        if (recording){
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            recording = false;
            soundrecorded = true;
            recordButton.setImageResource(R.drawable.microphone_check);
        }
    }


    public void dispatchRecordIntent(){
        if (recording){  //stop recording
            try {
                RecordingTimer.cancel();
                RecordingTimer.purge();
            }
            catch (Exception e){

            }
            this.stopRecording();
            saveButton.setEnabled(true);

        }
        else { //start recording
            saveButton.setEnabled(false);
            this.startRecording();
            Toast.makeText(this.getBaseContext(), "Recording started...", Toast.LENGTH_LONG).show();
            // recordButton.setImageResource(R.drawable.icon);
        }
    }


    public void startRecording(){
        if (!recording){
            recordButton.setImageResource(R.drawable.microphone_recording);
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(mFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                //       Log.e(LOG_TAG, "prepare() failed");
            }
            mRecorder.start();
            recording = true;

            //Try in 6 minutes
            int delay2 = 60 * 4; //1 minute
            RecordingTimer = new Timer();
            RecordingTimer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    //
                    if (recording) {  //stop recording

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dispatchRecordIntent();
                            }
                        });


                    }
                }
            }, 1000 * delay2, 1000000);

        }
    }

    public void upLoad(NubisDelayedAnswer delayedAnswer, boolean wait, int deleteId, int communicationType) {
        //Context context, NubisDelayedAnswer delayedAnswer, NubisAsyncResponse delegate
        try {
            NubisHTTP httpCom = new NubisHTTP(this, delayedAnswer, null, deleteId, communicationType, settings);
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

    @Override
    protected void onDestroy() {
        try {
            RecordingTimer.cancel();
            RecordingTimer.purge();
        }
        catch (Exception e){
        }
        super.onDestroy();
    }


}
