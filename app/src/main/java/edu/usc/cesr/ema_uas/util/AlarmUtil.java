package edu.usc.cesr.ema_uas.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;

import edu.usc.cesr.ema_uas.R;
import edu.usc.cesr.ema_uas.Constants;

import static android.content.Context.VIBRATOR_SERVICE;

public class AlarmUtil {
    public static void soundAlarm(Context context) {
        try {
            final MediaPlayer mediaPlayer = new MediaPlayer();
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            //  Previous settings
            final int previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            final int previousRingerMode = audioManager.getRingerMode();

            //  Set ot max volume
            int maxVolume  = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

            AssetFileDescriptor afd = context.getResources().openRawResourceFd(R.raw.pager_tone_112);
            if (afd == null) return;
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            int MAX_VOLUME = 120;
            int soundVolume = 50;
            float volume = (float) (1 - (Math.log(MAX_VOLUME - soundVolume) / Math.log(MAX_VOLUME)));
            mediaPlayer.setVolume(volume, volume);

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(false);
            mediaPlayer.prepare();
            if (!Constants.isDemo) {
                mediaPlayer.start();
            }

            final Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {200, 200, 200, 200};
            vibrator.vibrate(pattern, 0);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    vibrator.cancel();
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.release();

                    audioManager.setRingerMode(previousRingerMode);
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, previousVolume, 0);
                }
            }, 3000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
