package edu.usc.cesr.ema_uas.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import edu.usc.cesr.ema_uas.R;
import edu.usc.cesr.ema_uas.model.Settings;
import edu.usc.cesr.ema_uas.util.NubisDelayedAnswer;
import edu.usc.cesr.ema_uas.util.NubisHTTP;

import static edu.usc.cesr.ema_uas.R.id.saveVideoButton;

public class SoundRecordingActivity extends AppCompatActivity {

    private Settings settings;

    private ImageButton recordButton;
    private ImageButton openEndedRecordVedioButton;
    private com.beardedhen.androidbootstrap.BootstrapButton saveButton;
    private com.beardedhen.androidbootstrap.BootstrapButton saveVideoButton ;
    private MediaRecorder mRecorder = null;
    private boolean recording = false;
    private boolean soundrecorded = false;
    private int numberOfRecordings = 0;
    private String mFileName;
    private String mVedioFileName;
    private boolean videoUploaded = true;

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
        saveVideoButton = (com.beardedhen.androidbootstrap.BootstrapButton) findViewById(R.id.saveVideoButton);

        final Context context = this;

        recordButton = (ImageButton) findViewById(R.id.openEndedRecordSoundPictureButton);
        recordButton.setImageResource(R.drawable.microphone);
        openEndedRecordVedioButton = (ImageButton) findViewById(R.id.openEndedRecordVedioButton);
        openEndedRecordVedioButton.setImageResource(R.drawable.video);
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
        mVedioFileName = "/storage/emulated/0/DCIM/Camera/VID_";

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
    private static final int REQUEST_IMAGE_CAPTURE = 10;
    private File videofile;

    public void record_vedio(View view){
//        Intent intent = new Intent();
//        intent.setClass(this,VideoRecorderActivity.class);
//        startActivity(intent);
        Intent intent = new Intent();
        intent.setAction("android.media.action.VIDEO_CAPTURE");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,4*60);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0);

        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }


    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_IMAGE_CAPTURE){
            if(resultCode == RESULT_OK){
                Toast.makeText(getApplicationContext(), "Video Successfully Recorded", Toast.LENGTH_LONG).show();
                try {
                    mVedioFileName = getPath(this,data.getData());
                    Log.d("Videosaving","video path " + mVedioFileName);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

//                Log.d("savingVideo", "there is file on location " + file.geta);
                videoUploaded = false;
            } else {
                Toast.makeText(getApplicationContext(), "Video Capture Failed...", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void uploadVideo(View view){
        try {


            File file = new File(mVedioFileName);
            Log.d("videouploading", "path:" + file.getPath());
            if(!videoUploaded) {
                openEndedRecordVedioButton.setEnabled(false);

                saveVideoButton.setEnabled(false);
                Calendar now = Calendar.getInstance();
                now.setTimeInMillis(System.currentTimeMillis());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                NubisDelayedAnswer delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_POST_FILE);
                delayedanswer.addGetParameter("version", this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
                delayedanswer.addGetParameter("rtid", settings.getRtid());
                delayedanswer.addGetParameter("phonets", formatter.format(now.getTimeInMillis()));
                delayedanswer.addGetParameter("p", "uploadvideo");
                delayedanswer.addGetParameter("ema", "1");
                delayedanswer.addFileName(mVedioFileName);
                delayedanswer.setByteArrayOutputStream();
//                Toast.makeText(getApplicationContext(), "Video uploading", Toast.LENGTH_LONG).show();
                this.upLoad(delayedanswer, true, -1, NubisHTTP.H_UPLOAD);
                final Intent intent2 = new Intent(this, MainActivity.class);
                startActivity(intent2);
                videoUploaded = true;
            } else {
                Toast.makeText(getApplicationContext(), "No video to upload", Toast.LENGTH_LONG).show();
            }


            //else {

            //}

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it  Or Log it.
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


}
