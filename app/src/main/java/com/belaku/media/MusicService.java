package com.belaku.media;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by naveenprakash on 07/11/18.
 */

public class MusicService extends Service {


    private static final String TAG = "BackgroundSoundService";
    public static MediaPlayer mMediaPlayer;
    private String songName;
    private Intent rintent;
    private Notification notification;


    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "onBind()");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }


    public int onStartCommand(Intent intent, int flags, int startId) {

        rintent = intent;

        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
            } catch (Exception ex) {

            }
            //     mMediaPlayer = null;
        }


        if (intent != null) {
            songName = intent.getExtras().get("song_name").toString();
            mMediaPlayer = MediaPlayer.create(this, Uri.parse(rintent.getExtras().get("song_path").toString()));
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d("HereAmI", "completedSinging :)");

                }
            });

            PlayerActivity.MyMediaPlayer = mMediaPlayer;

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);
            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.mm_icon)
                    .setContentTitle(songName)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_MIN)
                    .setContentText("Playing . . . ")
                    .setContentIntent(pendingIntent).build();
            startForeground(1337, notification);

            mMediaPlayer.start();

            PlayerActivity.MyMediaPlayer = mMediaPlayer;
        }









        return Service.START_STICKY;
    }

    public IBinder onUnBind(Intent arg0) {
        Log.i(TAG, "onUnBind()");
        return null;
    }

    public void onStop() {
        Log.i(TAG, "onStop()");
    }

    public void onPause() {
        Log.i(TAG, "onPause()");
    }


    @Override
    public void onDestroy() {
        mMediaPlayer.stop();
        mMediaPlayer.release();

    }



    @Override
    public void onLowMemory() {
        Log.i(TAG, "onLowMemory()");
    }

    public static MediaPlayer getInstance() {
        return mMediaPlayer;
    }
}