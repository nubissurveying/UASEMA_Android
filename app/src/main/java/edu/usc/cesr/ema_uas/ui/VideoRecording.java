package edu.usc.cesr.ema_uas.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import edu.usc.cesr.ema_uas.R;
import edu.usc.cesr.ema_uas.model.Settings;
import edu.usc.cesr.ema_uas.util.NubisDelayedAnswer;
import edu.usc.cesr.ema_uas.util.NubisHTTP;

public class VideoRecording extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = "MainActivity";
    private SurfaceView mSurfaceview;
    private Button mBtnStartStop;
    private Button mBtnPlay;
    private Button mBtnSwitch;
    private boolean mStartedFlg = false;//是否正在录像
    private boolean mIsPlay = false;//是否正在播放录像
    private MediaRecorder mRecorder;
    private SurfaceHolder mSurfaceHolder;
    private ImageView mImageView;
    private Camera camera;
    private MediaPlayer mediaPlayer;
    private String path;
    private TextView textView;
    private int text = 0;
    private Activity currentActivity;
    private int currentCameraId;
    private boolean videoUploadedPermsiion;
    private Settings settings;
    private String HTTPReturnString = "";

    private android.os.Handler handler = new android.os.Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            text++;
            int sec = text % 60;
            String secText = (sec < 10? "0" : "") + sec;
            int min = text / 60;
            String minText = (min < 10? "0" : "") + min;
            textView.setText(minText + ":" + secText);
            handler.postDelayed(this,1000);
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_recording);


        PackageManager pm = this.getPackageManager();

        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this.getBaseContext(), "No camera to use", Toast.LENGTH_LONG).show();
            final Intent intent2 = new Intent(this, MainActivity.class);
            startActivity(intent2);
        }
        currentActivity = this;
        videoUploadedPermsiion = false;
        settings = Settings.getInstance(this);
        mSurfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        mImageView = (ImageView) findViewById(R.id.imageview);
        mBtnStartStop = (Button) findViewById(R.id.btnStartStop);
        mBtnPlay = (Button) findViewById(R.id.btnPlayVideo);
        mBtnSwitch = (Button) findViewById(R.id.btnswitch);
        textView = (TextView)findViewById(R.id.text);
        currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        mBtnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text = 0;
                textView.setText("00:00");
                handler.postDelayed(runnable,1000);
                if (mIsPlay) {
                    if (mediaPlayer != null) {
                        mIsPlay = false;
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
                if (!mStartedFlg) {

                    mImageView.setVisibility(View.GONE);
                    if (mRecorder == null) {
                        mRecorder = new MediaRecorder();
                    }

                    camera = Camera.open(currentCameraId);
                    if (camera != null) {
                        camera.setDisplayOrientation(90);

                        camera.unlock();
                        mRecorder.setCamera(camera);
                    }

                    try {
                        // 这两项需要放在setOutputFormat之前
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                        // Set output file format
                        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

                        // 这两项需要放在setOutputFormat之后
                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

                        mRecorder.setVideoSize(640, 480);
                        mRecorder.setVideoFrameRate(30);
                        mRecorder.setVideoEncodingBitRate(2 * 1024 * 1024);
                        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) mRecorder.setOrientationHint(90);
                        else mRecorder.setOrientationHint(270);
                        //设置记录会话的最大持续时间（毫秒）
                        mRecorder.setMaxDuration(30 * 1000);
                        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

                        path = getSDPath();
                        if (path != null) {
                            File dir = new File(path + "/recordtest");
                            if (!dir.exists()) {
                                dir.mkdir();
                            }
                            path = dir + "/" + "UAS" + ".mp4";
                            mRecorder.setOutputFile(path);
                            mRecorder.prepare();
                            mRecorder.start();
                            mStartedFlg = true;
                            mBtnStartStop.setText("Stop");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //stop
                    if (mStartedFlg) {
                        try {
                            handler.removeCallbacks(runnable);
                            mRecorder.stop();
                            mRecorder.reset();
                            mRecorder.release();
                            mRecorder = null;
                            if (text < 10){
                                Activity activity = currentActivity;
                                new AlertDialog.Builder(activity)
                                        .setMessage("Too short, please record again")
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show();
                            } else if(text > 60* 4){
                                Activity activity = currentActivity;
                                new AlertDialog.Builder(activity)
                                        .setMessage("Too long, please record again")
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show();
                            } else {
                                videoUploadedPermsiion = true;
                            }
                            mBtnStartStop.setText("Start");
                            if (camera != null) {
                                camera.release();
                                camera = null;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mStartedFlg = false;
                }
            }
        });

        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mStartedFlg){
                    if (path != null){
                        mIsPlay = true;
                        text = 0;
                        textView.setText("00:00");
                        handler.postDelayed(runnable,1000);
                        mImageView.setVisibility(View.GONE);
                        if (mediaPlayer == null) {
                            mediaPlayer = new MediaPlayer();
                        }
                        mediaPlayer.reset();
                        Uri uri = Uri.parse(path);
                        mediaPlayer = MediaPlayer.create(VideoRecording.this, uri);
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDisplay(mSurfaceHolder);
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                handler.removeCallbacks(runnable);
                                Activity activity = currentActivity;
                                new AlertDialog.Builder(activity)
                                        .setMessage("upload?")
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Log.d("VideoRecording", "here to upload");
                                                uploadVideo();
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .show();
                            }
                        });
                        try{
                            mediaPlayer.prepare();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        mediaPlayer.start();

                    } else {
                        Activity activity = currentActivity;
                        new AlertDialog.Builder(activity)
                                .setMessage("Please record first")
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    }
                } else {
                    Activity activity = currentActivity;
                    new AlertDialog.Builder(activity)
                            .setMessage("Please stop record first")
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }


            }
        });

        mBtnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStartedFlg){
                    Activity activity = currentActivity;
                    if (null != activity) {
                        new AlertDialog.Builder(activity)
                                .setMessage("cannot switch while recording")
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    }
                } else {
//                    camera.stopPreview();
                    if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                        currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                        mBtnSwitch.setText("FRONT");
                    }
                    else {
                        currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                        mBtnSwitch.setText("BACK");
                    }
                }

            }
        });
        SurfaceHolder holder = mSurfaceview.getHolder();
        holder.addCallback(this);
        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mStartedFlg) {
            mImageView.setVisibility(View.VISIBLE);
        }
    }



    /**
     * 获取SD path
     *
     * @return
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }

        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceview = null;
        mSurfaceHolder = null;
        handler.removeCallbacks(runnable);
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
            Log.d(TAG, "surfaceDestroyed release mRecorder");
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    public void uploadVideo(){
        try {

            if(videoUploadedPermsiion) {

                File file = new File(path);
                Log.d("videouploading", "path:" + file.getPath());

                Calendar now = Calendar.getInstance();
                now.setTimeInMillis(System.currentTimeMillis());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                NubisDelayedAnswer delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_POST_FILE);
                delayedanswer.addGetParameter("version", this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
                delayedanswer.addGetParameter("rtid", settings.getRtid());
                delayedanswer.addGetParameter("phonets", formatter.format(now.getTimeInMillis()));
                delayedanswer.addGetParameter("p", "uploadvideo");
                delayedanswer.addGetParameter("ema", "1");
                delayedanswer.addFileName(path);
                delayedanswer.setByteArrayOutputStream();
//                Toast.makeText(getApplicationContext(), "Video uploading", Toast.LENGTH_LONG).show();
                this.upLoad(delayedanswer, true, -1, NubisHTTP.H_UPLOAD);
                final Intent intent2 = new Intent(this, MainActivity.class);
                startActivity(intent2);
                videoUploadedPermsiion = false;
            } else {
                Toast.makeText(getApplicationContext(), "Video length not qualify ", Toast.LENGTH_LONG).show();
            }


            //else {

            //}

        }
        catch(Exception e){
            e.printStackTrace();
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

}
