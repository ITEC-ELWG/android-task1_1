package com.HandleStudio.lolmusic.lolmusic;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

public class PlayingService extends Service {


    public static final String TAG = "PlayingService";
    public static final int MODE_ONE_LOOP = 10001;  //单曲循环
    public static final int MODE_ALL_LOOP = 10002;  //全部循环
    public static final int MODE_RANDOM = 10003;    //随机播放
    public static final int MODE_SEQUENCE = 10004;  //顺序播放

    public static final String CONTROL_BEGIN = "com.HandleStudio.lolmusic.lolmusic.CONTROL_BEGIN";
    public static final String CONTROL_PLAY_PAUSE = "com.HandleStudio.lolmusic.lolmusic.CONTROL_PLAY_PAUSE";
    public static final String CONTROL_PRE = "com.HandleStudio.lolmusic.lolmusic.CONTROL_PRE";
    public static final String CONTROL_NEXT = "com.HandleStudio.lolmusic.lolmusic.CONTROL_NEXT";
    public static final String CONTROL_MODE = "com.HandleStudio.lolmusic.lolmusic.CONTROL_MODE";
    public static final String CONTROL_PROGRESS = "com.HandleStudio.lolmusic.lolmusic.CONTROL_PROGRESS";

    public static final String ACTION_UPDATE_PROGRESS = "com.HandleStudio.lolmusic.lolmusic.UPDATE_PROGRESS";
    public static final String ACTION_UPDATE_DURATION = "com.HandleStudio.lolmusic.lolmusic.UPDATE_DURATION";
    public static final String ACTION_UPDATE_CURRENT_MUSIC = "com.HandleStudio.lolmusic.lolmusic.UPDATE_CURRENT_MUSIC";
    public static final String ACTION_UPDATE_MODE = "com.HandleStudio.lolmusic.lolmusic.UPDATE_MODE";
    public static final String ACTION_UPDATE_STATE = "com.HandleStudio.lolmusic.lolmusic.UPDATE_STATE";


    private ControlReceiver receiver;
    /*Binder musicBinder = new MusicBinder();    //这里要声明称IBinder或者Binder不能用MusicBinder，大坑*/
    private FileSearchHelper fileSearchHelper;
    private BroadcastDeliveHelper bdhelp;

    //播放控制
    private int position;
    private int currentTime;
    private MediaPlayer mediaPlayer;
    private boolean playState = false;
    private static int playMode = MODE_SEQUENCE;
    private Timer timer;
    private TimerTask timerTask;

    public PlayingService() {
        Log.i(TAG, "MusicService Construction");
    }

    @Override
    public void onCreate(){
        Log.i(TAG, "MusicService onCreate()");
        fileSearchHelper = new FileSearchHelper(this);
        bdhelp = new BroadcastDeliveHelper(this);
        registerControlReceiver();
    }

    public void registerControlReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(CONTROL_BEGIN);
        filter.addAction(CONTROL_PLAY_PAUSE);
        filter.addAction(CONTROL_PRE);
        filter.addAction(CONTROL_NEXT);
        filter.addAction(CONTROL_MODE);
        filter.addAction(CONTROL_PROGRESS);
        receiver = new ControlReceiver();
        registerReceiver(receiver,filter);
    }

    class ControlReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();

            if(action.equals(CONTROL_BEGIN)){
                position = intent.getIntExtra("extra",0);
                begin(position);
            }

            if(action.equals(CONTROL_PLAY_PAUSE)){
                if (playState) pause();
                else continuePlay();
            }

            if (action.equals(CONTROL_PRE)){
                toPrevious();
            }

            if (action.equals(CONTROL_NEXT)){
                toNext();
            }

            if (action.equals(CONTROL_MODE)){
                playMode = intent.getIntExtra("extra",MODE_SEQUENCE);
                Log.e(TAG,String.valueOf(playMode));
            }

            if (action.equals(CONTROL_PROGRESS)){
                mediaPlayer.seekTo(intent.getIntExtra("extra",0));
            }

        }
    }


    public void begin(int p){
        position = p;
        if (!playState) {
            play();
            playState = true;
            notifyMode();
            notifyState();
            notifyCurrentMusic();
        }
        else {
            timer.cancel();
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            play();
            playState = true;
            notifyMode();
            notifyState();
            notifyCurrentMusic();
        }
    }

    public void pause(){
        mediaPlayer.pause();
        playState = false;
        notifyState();
    }

    public void continuePlay(){
        mediaPlayer.start();
        playState = true;
        notifyState();
    }

    public void toNext(){
        if(playMode==MODE_RANDOM){
            position = getRandomPosition();
        }
        else {
            if (position == (fileSearchHelper.getFileCount() - 1)) {
                position = 0;
            } else position++;
        }
        begin(position);
    }

    public void onFinishToNext(){
        switch (playMode){
            case MODE_RANDOM:
                position = getRandomPosition();
                begin(position);

            case MODE_ALL_LOOP:
                if (position == (fileSearchHelper.getFileCount() - 1)) {
                    position = 0;
                }
                else position++;
                begin(position);

            case MODE_SEQUENCE:
                if (position != (fileSearchHelper.getFileCount() - 1))
                    position++;
                begin(position);

            case MODE_ONE_LOOP:
                begin(position);
        }
    }

    public void toPrevious(){
        if(playMode==MODE_RANDOM){
            position = getRandomPosition();
        }
        else {
            if (position == 0) {
                position = fileSearchHelper.getFileCount() - 1;
            } else position--;
        }
        begin(position);
    }

    public void notifyMode(){
        bdhelp.broadcastDeliver(ACTION_UPDATE_MODE,playMode);
    }

    public void notifyState(){
        if(playState)
            bdhelp.broadcastDeliver(ACTION_UPDATE_STATE,1);
        else bdhelp.broadcastDeliver(ACTION_UPDATE_STATE,0);
    }

    public void notifyProgress(Bundle b){
        Bundle bundle = b;
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_PROGRESS);
        intent.putExtra("extra",bundle);
        sendBroadcast(intent);
    }

    public void notifyDuration(){
        Bundle bundle = fileSearchHelper.getFileDuration(position);
        int minute = bundle.getInt("minute");
        int second = bundle.getInt("second");
        String totalTime;
        int totalDuration;
        totalTime = timeTransform(minute,second);
        totalDuration = mediaPlayer.getDuration();
        Bundle durationBundle = new Bundle();
        durationBundle.putString("totalTime",totalTime);
        durationBundle.putInt("duration",totalDuration);

        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_DURATION);
        intent.putExtra("extra",durationBundle);
        sendBroadcast(intent);
    }

    public void notifyCurrentMusic(){
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_CURRENT_MUSIC);
        Bundle bundle = new Bundle();
        bundle.putString("title",fileSearchHelper.getFileTitle(position));
        bundle.putString("artist",fileSearchHelper.getFileArtist(position));
        intent.putExtra("extra", bundle);
        sendBroadcast(intent);
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.i(TAG,"MusicService onUnbind()");
        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);
        Log.i(TAG,"Destroy");
    }

    public String timeTransform(int minute, int second){
        String mm,ss;
        if (minute<10) mm = "0" + minute;
        else mm = String.valueOf(minute);
        if (second<10) ss = "0" + second;
        else ss = String.valueOf(second);
        return mm+":"+ss;
    }


    public void play(){
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(fileSearchHelper.getFilePath(position));
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    timer.cancel();
                    mediaPlayer.release();
                    PlayingService.this.mediaPlayer = null;
                    playState = false;
                    Log.e(TAG, "The song end!Begin a new one");
                    onFinishToNext();
                }
            });

            notifyDuration();
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    currentTime = mediaPlayer.getCurrentPosition()/1000;
                    int minute = currentTime/60;
                    int second = currentTime%60;
                    Bundle bundle = new Bundle();
                    bundle.putString("currentTime",timeTransform(minute,second));
                    bundle.putInt("currentDuration",mediaPlayer.getCurrentPosition());
                    notifyProgress(bundle);
                }
            };
            timer.schedule(timerTask,0,100);

        } catch (Exception e) {
            Log.e(TAG,"can't find file!");
        }
    }

    public int getRandomPosition(){
        return  (int)(Math.random()*(fileSearchHelper.getFileCount()-1));
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "MusicService onBind() success");
        return null;
    }

    @Override
    public void onRebind(Intent intent){
        super.onRebind(intent);
        Log.e(TAG, "MusicService onReBind() success");
    }

}
