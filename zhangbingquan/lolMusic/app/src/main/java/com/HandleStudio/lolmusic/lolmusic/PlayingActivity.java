package com.HandleStudio.lolmusic.lolmusic;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.HandleStudio.lolmusic.lolmusic.R;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 2bab on 14-7-10.
 *
 */

public class PlayingActivity extends Activity implements View.OnClickListener{

    private static final String TAG = "PlayingActivity";

    private FrameLayout albumPicAndLrc;
    private DimensHelper dimensHelper;
    private BroadcastDeliveHelper bdHelper;

    private PlayingActivityReceiver receiver;
    private int position;
    private boolean playState=true;
    private int tempIdontknow;
    private int mode = PlayingService.MODE_SEQUENCE;

    private Button playAndPause;
    private Button pre;
    private Button next;
    private ImageView order;
    private SeekBar seekBar;
    private TextView title;
    private TextView artist;
    private TextView now_duration;
    private TextView duration;

    //播放控制
    private Timer timer;
    private TimerTask timerTask;
    private boolean isChanging = false;

    /*ServiceConnection connection;
    PlayingService.MusicBinder binder;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);

        //init
        dimensHelper = new DimensHelper(this);
        bdHelper = new BroadcastDeliveHelper(this);
        initActionBar();
        initFindView();
        initAlbumPicAndLrcHeight();//画方形
        //获得歌曲在数组的顺序
        Intent intent = getIntent();
        position = intent.getIntExtra("index",-1);

    }


    public void registerUIReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayingService.ACTION_UPDATE_STATE);
        filter.addAction(PlayingService.ACTION_UPDATE_CURRENT_MUSIC);
        filter.addAction(PlayingService.ACTION_UPDATE_MODE);
        filter.addAction(PlayingService.ACTION_UPDATE_DURATION);//总时间
        filter.addAction(PlayingService.ACTION_UPDATE_PROGRESS);//进度条
        receiver = new PlayingActivityReceiver();
        registerReceiver(receiver,filter);
    }


    public class PlayingActivityReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();

            if (action.equals(PlayingService.ACTION_UPDATE_STATE)){
                int playAndPauseIcon;
                playAndPauseIcon = intent.getIntExtra("extra",1);
                if (playAndPauseIcon==1) {
                    playState = true;
                    playAndPause.setBackgroundResource(R.drawable.pause);
                }
                else {
                    playState = false;
                    playAndPause.setBackgroundResource(R.drawable.play);
                }
            }

            if (action.equals(PlayingService.ACTION_UPDATE_CURRENT_MUSIC)){
                Bundle bundle = intent.getBundleExtra("extra");
                title.setText(bundle.getString("title"));
                artist.setText(bundle.getString("artist"));
            }

            if (action.equals(PlayingService.ACTION_UPDATE_MODE)){
                mode = intent.getIntExtra("extra",PlayingService.MODE_SEQUENCE);
                showTip(String.valueOf(mode));
            }

            if (action.equals(PlayingService.ACTION_UPDATE_DURATION)){
                Bundle bundle = intent.getBundleExtra("extra");
                duration.setText(bundle.getString("totalTime"));
                seekBar.setMax(bundle.getInt("duration"));
            }

            if (action.equals(PlayingService.ACTION_UPDATE_PROGRESS)){
                Bundle bundle = intent.getBundleExtra("extra");
                now_duration.setText(bundle.getString("currentTime"));
                seekBar.setProgress(bundle.getInt("currentDuration"));
            }
        }

    }


    @Override
    public void onClick(View v){
        switch (v.getId()){

            case R.id.playing_song_play_pause:
                bdHelper.broadcastDeliver(PlayingService.CONTROL_PLAY_PAUSE);
                break;

            case R.id.playing_song_pre:
                bdHelper.broadcastDeliver(PlayingService.CONTROL_PRE);
                break;

            case R.id.playing_song_next:
                bdHelper.broadcastDeliver(PlayingService.CONTROL_NEXT);
                break;

            case R.id.playing_song_order:
                if (mode == PlayingService.MODE_SEQUENCE) mode = PlayingService.MODE_ONE_LOOP;
                else mode++;
                showTip("模式："+mode);
                bdHelper.broadcastDeliver(PlayingService.CONTROL_MODE,mode);
                break;

        }
    }



    public void initActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_USE_LOGO);
        actionBar.setIcon(R.drawable.left_arrow);
        actionBar.setHomeButtonEnabled(true);
    }

    public void initFindView(){
        albumPicAndLrc = (FrameLayout) findViewById(R.id.albumpic_and_lrc);
        playAndPause = (Button) findViewById(R.id.playing_song_play_pause);
        pre = (Button) findViewById(R.id.playing_song_pre);
        next = (Button) findViewById(R.id.playing_song_next);
        order = (ImageView) findViewById(R.id.playing_song_order);
        seekBar = (SeekBar) findViewById(R.id.playing_song_seek_bar);
        title = (TextView) findViewById(R.id.playing_song_title);
        artist = (TextView) findViewById(R.id.playing_song_artist);
        now_duration = (TextView) findViewById(R.id.playing_song_now_duration);
        duration = (TextView) findViewById(R.id.playing_song_duration);

        seekBar.setOnSeekBarChangeListener(new MySeekBar());
        playAndPause.setOnClickListener(this);
        pre.setOnClickListener(this);
        next.setOnClickListener(this);
        order.setOnClickListener(this);
    }

    public void initAlbumPicAndLrcHeight(){
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = PlayingActivity.this.getWindowManager();
        wm.getDefaultDisplay().getMetrics(dm);
        ViewGroup.LayoutParams params = albumPicAndLrc.getLayoutParams();
        params.height = dm.widthPixels + dimensHelper.getStatusBarHeight();
        albumPicAndLrc.setLayoutParams(params);
    }


    class MySeekBar implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            isChanging=true;
        }

        public void onStopTrackingTouch(SeekBar seekbbbar) {
            bdHelper.broadcastDeliver(PlayingService.CONTROL_PROGRESS,seekBar.getProgress());
            isChanging=false;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.playing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart(){
        super.onStart();
        registerUIReceiver();

        Log.i(TAG,"onStart()");
    }

    @Override
    public void onResume(){
        super.onResume();
        bdHelper.broadcastDeliver(PlayingService.CONTROL_BEGIN, position);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public void showTip(String s){
        Toast.makeText(PlayingActivity.this, s, Toast.LENGTH_SHORT).show();
    }


    /*public void connectToService(){
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                binder = (PlayingService.MusicBinder) iBinder;
                binder.begin(position);
                Log.i(TAG,"onServiceConnected()");
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.i(TAG,"onServiceDisconnected()");
            }
        };
        Intent intent = new Intent(getApplicationContext(),PlayingService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }*///尝试后无法满足

}
