package com.belaku.media;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.abdularis.civ.CircleImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import me.tankery.lib.circularseekbar.CircularSeekBar;
import tyrantgit.explosionfield.ExplosionField;

public class PlayerActivity extends AppCompatActivity {

    private Intent playerintent;
    private ImageButton ImgBtnPlayPause, ImgBtnNext, ImgBtnPrev, ImgBtnff, ImgBtnrev;
    public static MediaPlayer MyMediaPlayer;
    private static final String CHANNEL_ID = "myMusicService";
 //   public ArrayList<AudioSong> mAudioSongs;
    private int songPosition;
    private TextView TxTitle, TxArtist, TxAlbum, TxTotal, TxPresent;
    private CircleImageView AlbumArtImgV;
    private Handler mHandler = new Handler();
    private CircularSeekBar cSeekbar;
    private String songsAsString;
    private boolean repeat = true, repaetOne, shuffle;
    private int TimeToPlay = 0;
    RelativeLayout relativeLayout;
    float volume = 0;
    private Spinner TimerSpinner;
    private String artistName, albumName;
    private ArrayList<AudioSong> artistSongs = new ArrayList<>(), albumSongs = new ArrayList<>();
    private FloatingActionButton fabBg;
    private ImageView ImgvTimer, Imgvrepeat, ImgvShuffle, ImgvFav;
    private MainActivity mainActivity = new MainActivity();
    public ArrayList<AudioSong> mAudioSongs;



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        init();


        RotateAnimation rotateAnim;
        rotateAnim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.setRepeatCount(0); //Repeat animation indefinitely
        rotateAnim.setDuration(3000); //Put desired duration per anim cycle here, in milliseconds

            TimerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                    TimeToPlay = i * 60000;

                    if (TimeToPlay != 0) {
                        makeToast("Each song will be played for " + i + " minute");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startFadeOut();
                              //  explosionField.explode(AlbumArtImgV);
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        repeat = true;
                                        if (MyMediaPlayer.isPlaying())
                                        nextSong();
                                    }
                                }, 5000);
                            }
                        }, TimeToPlay);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });



        ImgvFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mainActivity.mAudioSongs.get(songPosition).isFavorite) {
                    ImgvFav.setImageResource(R.drawable.fav_disabled);
                    mainActivity.mAudioSongs.get(songPosition).isFavorite = false;
                }
                else {
                    ImgvFav.setImageResource(R.drawable.fav);
                    mainActivity.mAudioSongs.get(songPosition).isFavorite = true;
                }
            }
        });

        ImgvTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TimerSpinner != null) {
                    makeToast("Set timer for each Song");
                    TimerSpinner.setVisibility(View.VISIBLE);
                }
            }
        });

        ImgvShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shuffle) {
                    shuffle = false;
                    ImgvShuffle.setImageResource(R.drawable.shuffle_disabled);
                } else {
                    shuffle = true;
                    ImgvShuffle.setImageResource(R.drawable.shuffle);
                }

            }
        });

        Imgvrepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (repeat) {
                    repeat = false;
                    Imgvrepeat.setImageResource(R.drawable.repeat_one);
                    repaetOne = true;
                } else if (repaetOne) {
                 repaetOne = false;
                 repeat = false;
                    Imgvrepeat.setImageResource(R.drawable.repeat_all_disabled);
                } else {
                    repeat = true;
                    Imgvrepeat.setImageResource(R.drawable.repeat_all);
                    repaetOne = false;
                }
            }
        });




        ImgBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeToast("Yet2Impl");
                nextSong();
                updateProgressBar();
            }
        });

        ImgBtnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeToast("Yet2Impl");
                previousSong();
                updateProgressBar();
            }
        });

        ImgBtnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyMediaPlayer.getCurrentPosition() < MyMediaPlayer.getDuration() - 5000)
                    MyMediaPlayer.seekTo(MyMediaPlayer.getCurrentPosition() + 5000);
                else if (MyMediaPlayer.getCurrentPosition() < MyMediaPlayer.getDuration() - 3000)
                    MyMediaPlayer.seekTo(MyMediaPlayer.getCurrentPosition() + 5000);
                else makeToast("Can't ff to 3s / 5s");

            }
        });

        ImgBtnrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (MyMediaPlayer.getCurrentPosition() > 5000)
                    MyMediaPlayer.seekTo(MyMediaPlayer.getCurrentPosition() - 5000);
                else if (MyMediaPlayer.getCurrentPosition() < 3000)
                    MyMediaPlayer.seekTo(MyMediaPlayer.getCurrentPosition() - 5000);
                else makeToast("Can't rewind to 3s / 5s");

            }
        });

        playerintent = getIntent();
     //   if (playerintent.getExtras() != null)
        if (playerintent != null && playerintent.getExtras() != null) {
            if (playerintent.getExtras().get("position") != null) {
                Bundle bundle = getIntent().getExtras();
                String jsonString = bundle.getString("KEY");
                songPosition = (int) bundle.get("position");

                Gson gson = new Gson();
                Type listOfdoctorType = new TypeToken<List<AudioSong>>() {
                }.getType();
                mAudioSongs = gson.fromJson(jsonString, listOfdoctorType);


                if (bundle.get("Pageposition") != null) {
                    if (Integer.valueOf((Integer) bundle.get("Pageposition")) == 0) {
                        makeToast("ARTISTpageINTENT");
                        artistName = mainActivity.mAudioSongs.get(songPosition).getArtist();
                    } else if (Integer.valueOf((Integer) bundle.get("Pageposition")) == 1) {
                        makeToast("SONGpageINTENT");
                    } else if (Integer.valueOf((Integer) bundle.get("Pageposition")) == 2) {
                        makeToast("ALBUMpageINTENT");
                        albumName = mainActivity.mAudioSongs.get(songPosition).getAlbum();
                    }
                }


                makeToast("starting service from playerActivity");

                Gson gson2 = new Gson();
                songsAsString = gson2.toJson(mAudioSongs);


                playSong(songPosition);


                ImgBtnPlayPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MyMediaPlayer = MusicService.getInstance();

                        if (MyMediaPlayer.isPlaying()) {
                            MyMediaPlayer.pause();
                            ImgBtnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                        } else {
                            MyMediaPlayer.start();
                            ImgBtnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                        }

                    }
                });


            }
        }
    }

    private void init() {

        relativeLayout = findViewById(R.id.p_layout);
        AlbumArtImgV = findViewById(R.id.imgv_albumart);
        cSeekbar = findViewById(R.id.c_seekbar);

        TxTitle = findViewById(R.id.tx_title);
        TxArtist = findViewById(R.id.tx_artist);
        TxAlbum = findViewById(R.id.tx_album);
        fabBg = findViewById(R.id.fab_bg);

        TimerSpinner = findViewById(R.id.timer_spinner);
        TimerSpinner.setVisibility(View.INVISIBLE);
        Integer[] items = new Integer[]{0, 1, 2, 3, 4};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(PlayerActivity.this, R.layout.spinner_item, items);
        TimerSpinner.setAdapter(adapter);
        TimerSpinner.setSelection(0, false);

        TxTotal = findViewById(R.id.tx_total_duration);
        TxPresent = findViewById(R.id.tx_current_duration);
        TxTotal.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "digital-7.ttf")));
        TxPresent.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "digital-7.ttf")));
        ImgBtnPlayPause = findViewById(R.id.img_btn_playpause);
        ImgBtnNext = findViewById(R.id.imgbtn_next);
        ImgBtnPrev = findViewById(R.id.imgbtn_prev);
        ImgBtnff = findViewById(R.id.imgbtn_ff);
        ImgBtnrev = findViewById(R.id.imgbtn_rev);
        ImgvTimer = findViewById(R.id.imgv_timer);
        Imgvrepeat = findViewById(R.id.imgv_repeat);
        ImgvShuffle = findViewById(R.id.imgv_shuffle);
        ImgvFav = findViewById(R.id.imgv_fav);
    }


    @Override
    protected void onResume() {
        super.onResume();
        makeToast("onResume");
    }

    private void SetTextTypeface() {

        int random = new Random().nextInt((5 - 1) + 1) + 1;
        // makeSnack("Random - " + random);

        switch (random) {
            case 1:
                TxTitle.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "berkshireswash-regular.ttf")));
                TxArtist.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "berkshireswash-regular.ttf")));
                break;
            case 2:
                TxTitle.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "LobsterTwo-BoldItalic.otf")));
                TxArtist.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "LobsterTwo-BoldItalic.otf")));
                break;
            case 3:
                TxTitle.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "Sofia-Regular.otf")));
                TxArtist.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "Sofia-Regular.otf")));
                break;
            case 4:
                TxTitle.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "Pacifico.ttf")));
                TxArtist.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "Pacifico.ttf")));
                break;
            case 5:
                TxTitle.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "LobsterTwo-Bold.otf")));
                TxArtist.setTypeface((Typeface.createFromAsset(getApplicationContext().getAssets(), "LobsterTwo-Bold.otf")));
                break;
        }


    }


    public void updateProgressBar() {

        cSeekbar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);
                int totalDuration = MyMediaPlayer.getDuration();
                int currentPosition = utils.progressToTimer((int) seekBar.getProgress(), totalDuration);

                // forward or backward to certain seconds
                MyMediaPlayer.seekTo(currentPosition);

                // update timer progress again
                updateProgressBar();
            }
        });

        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private void previousSong() {
        if (songPosition > 0) {
            playSong(songPosition - 1);
            songPosition = songPosition - 1;
        } else {
            // play last song
            playSong(mainActivity.mAudioSongs.size() - 1);
            songPosition = mainActivity.mAudioSongs.size() - 1;
        }

    }


    private void nextSong() {
        //   fadeIn(MyMediaPlayer, 10000);

        if (artistName != null){
            for (int i = 0 ; i < mainActivity.mAudioSongs.size() ; i++) {
                if (mainActivity.mAudioSongs.get(i).getArtist().equals(artistName)) {
                    artistSongs.add(mainActivity.mAudioSongs.get(i));
                    playSong(i);
                }
            }
        } else if (albumName != null){
            for (int i = 0 ; i < mainActivity.mAudioSongs.size() ; i++) {
                if (mainActivity.mAudioSongs.get(i).getAlbum().equals(albumName)) {
                    albumSongs.add(mainActivity.mAudioSongs.get(i));
                    playSong(i);
                }
            }
        }

        // check if next song is there or not
        if (songPosition < (mainActivity.mAudioSongs.size() - 1)) {

            playSong(songPosition + 1);
            songPosition = songPosition + 1;

            if (TimeToPlay != 0) {
             //   TxTimer.setText("Each song will be played for " + i + " minute");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startFadeOut();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                repeat = true;
                                nextSong();
                            }
                        }, 5000);
                    }
                }, TimeToPlay);
            }
        } else {
            // play first song
            playSong(0);
            songPosition = 0;
        }
    }

    private void startFadeOut() {
        final int FADE_DURATION = 10000; //The duration of the fade
        //The amount of time between volume changes. The smaller this is, the smoother the fade
        final int FADE_INTERVAL = 250;
        final float MIN_VOLUME = (float) 0.1; //The volume will increase from 0 to 1
        int numberOfSteps = FADE_DURATION / FADE_INTERVAL; //Calculate the number of fade steps
        //Calculate by how much the volume changes each step
        final float deltaVolume = MIN_VOLUME / (float) numberOfSteps;

        //Create a new Timer and Timer task to run the fading outside the main UI thread
        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                fadeOutStep(deltaVolume); //Do a fade step
                //Cancel and Purge the Timer if the desired volume has been reached
                if (volume <= 1f) {
                    timer.cancel();
                    timer.purge();
                }
            }
        };

        timer.schedule(timerTask, FADE_INTERVAL, FADE_INTERVAL);
    }

    // working fine !
    private void startFadeIn() {
        final int FADE_DURATION = 10000; //The duration of the fade
        //The amount of time between volume changes. The smaller this is, the smoother the fade
        final int FADE_INTERVAL = 250;
        final int MAX_VOLUME = 1; //The volume will increase from 0 to 1
        int numberOfSteps = FADE_DURATION / FADE_INTERVAL; //Calculate the number of fade steps
        //Calculate by how much the volume changes each step
        final float deltaVolume = MAX_VOLUME / (float) numberOfSteps;

        //Create a new Timer and Timer task to run the fading outside the main UI thread
        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                fadeInStep(deltaVolume); //Do a fade step
                //Cancel and Purge the Timer if the desired volume has been reached
                if (volume >= 1f) {
                    timer.cancel();
                    timer.purge();
                }
            }
        };

        timer.schedule(timerTask, FADE_INTERVAL, FADE_INTERVAL);
    }

    private void fadeInStep(float deltaVolume) {
      //  if (MyMediaPlayer != null)
        try {
            MyMediaPlayer.setVolume(volume, volume);
        } catch (Exception ex) {
            Log.d("MyMediaPlayerEXCP", ex.toString());
        }
        volume += deltaVolume;
        Log.d("Vol217 - fadeInStep", String.valueOf(volume));

    }

    private void fadeOutStep(float deltaVolume) {
    //    if (MyMediaPlayer != null)
        try {
            MyMediaPlayer.setVolume(volume, volume);
        } catch (Exception ex) {
         Log.d("MyMediaPlayerEXCP", ex.toString());
        }
        volume -= deltaVolume;
        Log.d("Vol217 - fadeOutStep", String.valueOf(volume));
    }


    private int getDeviceVolume() {

        // Get the AudioManager instance
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);

        int music_volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        return music_volume_level;
    }

    public void playSong(int songIndex) {

        if (cSeekbar.getCircleProgressColor() == android.R.color.black)
            cSeekbar.setCircleProgressColor(android.R.color.white);
        else if (cSeekbar.getCircleProgressColor() == android.R.color.white)
            cSeekbar.setCircleProgressColor(android.R.color.black);

        startFadeIn();
        ImgBtnPlayPause.setImageResource(android.R.drawable.ic_media_pause);

        getAlbumArt(songIndex);

        TxTitle.setText(mAudioSongs.get(songIndex).getTitle());
        TxArtist.setText(mAudioSongs.get(songIndex).getArtist());
        TxAlbum.setText(mAudioSongs.get(songIndex).getAlbum());

        if (mAudioSongs.get(songIndex).isFavorite)
            ImgvFav.setImageResource(R.drawable.fav);
        else ImgvFav.setImageResource(R.drawable.fav_disabled);

        startService(new Intent(PlayerActivity.this, MusicService.class)
                .putExtra("song_path", mAudioSongs.get(songIndex).getPath())
                .putExtra("song_name", mAudioSongs.get(songIndex).getTitle())
                .putExtra("song_position", songPosition)
                .putExtra("KEY", songsAsString));


        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (MusicService.getInstance() != null) {
                    MyMediaPlayer = MusicService.getInstance();
                    updateProgressBar();
                }
                MyMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Log.d("HereAmI", "completedSinging :)");

                        stopService(new Intent(PlayerActivity.this, MusicService.class));
                        if (songPosition < mAudioSongs.size() - 1)
                            playSong(songPosition + 1);
                        else if (repeat) {
                            playSong(0);
                            songPosition = 0;
                        }
                    }
                });
            }
        }, 3000);
    }


    private void getAlbumArt(int songIndex) {



        if (mAudioSongs.get(songIndex).getAlbumArt() != null) {
            AlbumArtImgV.setImageURI(Uri.parse(mAudioSongs.get(songIndex).getAlbumArt()));
        //    ImgBtnPlayPause.setImageURI(Uri.parse(mAudioSongs.get(songIndex).getAlbumArt()));
            fabBg.setImageURI(Uri.parse(mAudioSongs.get(songIndex).getAlbumArt()));
        }
        else {
            AlbumArtImgV.setImageResource(R.drawable.mm_icon);
            fabBg.setImageResource(R.drawable.mm_icon);
        }


    //    if (fabBg.getDrawable() != null)
        relativeLayout.setBackground( new BitmapDrawable(getResources(), BlurBuilder(fabBg)));


        Animation shake;
        shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        AlbumArtImgV.startAnimation(shake);
//    AlbumArtImgV.startAnimation(rotateAnim);


    }

    public Bitmap BlurBuilder(FloatingActionButton fab) {
         final float BITMAP_SCALE = 2.5f;
         final float BLUR_RADIUS = 25f;


            int width = Math.round(fab.getWidth() * BITMAP_SCALE);
            int height = Math.round(fab.getHeight() * BITMAP_SCALE);

            Bitmap inputBitmap=((BitmapDrawable)fab.getDrawable()).getBitmap();

            Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

            RenderScript rs = RenderScript.create(getApplicationContext());
            ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
            Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
            theIntrinsic.setRadius(BLUR_RADIUS);
            theIntrinsic.setInput(tmpIn);
            theIntrinsic.forEach(tmpOut);
            tmpOut.copyTo(outputBitmap);

            return outputBitmap;

    }



    private Utilities utils = new Utilities();
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = MyMediaPlayer.getDuration();
            long currentDuration = MyMediaPlayer.getCurrentPosition();

            // Displaying Total Duration time
            TxTotal.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            TxPresent.setText("" + utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            cSeekbar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 1000);
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (MyMediaPlayer.isPlaying())
        Note();
    }


    private void Note() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "myChannel";
            String description = "myChannelDesc";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel nChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            nChannel.setDescription(description);
            nChannel.setSound(null, null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(nChannel);

            Intent intent = new Intent(this, PlayerActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.mm_icon);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.mm_icon)
                    .setLargeIcon(bitmap)
                    .setContentTitle(mainActivity.mAudioSongs.get(songPosition).getTitle())
                    .setContentText(mainActivity.mAudioSongs.get(songPosition).getArtist())
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);




// notificationId is a unique int for each notification that you must define
            int notificationId = 21;
            notificationManager.notify(notificationId, mBuilder.build());
        }
    }


    private void makeToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

}
