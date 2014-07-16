package com.HandleStudio.lolmusic.lolmusic;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by 2bab on 14-7-10.
 *
 */

public class PlayingActivity extends Activity implements View.OnClickListener{

    private static final String TAG = "PlayingActivity";

    private FrameLayout layoutAlbumPicAndLrc;
    private DimensHelper dimensHelper;
    private BroadcastDeliverHelper bdHelper;

    private PlayingActivityReceiver receiver;
    private int mode = PlayingService.MODE_SEQUENCE;
    private int position;
    boolean playState=true;

    private SeekBar seekBar;
    private TextView textTitle;
    private TextView textArtist;
    private TextView textNowDuration;
    private TextView textDuration;
    private Button btnPlayAndPause;
    Button btnPre;
    Button btnNext;
    ModeIconDrawView modeIconDrawView;

    //播放控制
    private boolean isChanging = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);

        //init
        dimensHelper = new DimensHelper(this);
        bdHelper = new BroadcastDeliverHelper(this);
        initActionBar();
        initFindView();
        initAlbumPicAndLrcHeight();//画方形

        //获得歌曲在数组的顺序
        Intent intent = getIntent();
        position = intent.getIntExtra("index",-1);
    }


    public void registerUIReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayingService.ACTION_UPDATE_PLAY_PAUSE);
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

            if (action.equals(PlayingService.ACTION_UPDATE_PLAY_PAUSE)){
                int playAndPauseIcon;
                playAndPauseIcon = intent.getIntExtra("extra",1);
                if (playAndPauseIcon==1) {
                    playState = true;
                    btnPlayAndPause.setBackgroundResource(R.drawable.pause);
                }
                else {
                    playState = false;
                    btnPlayAndPause.setBackgroundResource(R.drawable.play);
                }
            }

            if (action.equals(PlayingService.ACTION_UPDATE_CURRENT_MUSIC)){
                Bundle bundle = intent.getBundleExtra("extra");
                textTitle.setText(bundle.getString("title"));
                textArtist.setText(bundle.getString("artist"));
            }

            if (action.equals(PlayingService.ACTION_UPDATE_MODE)){
                mode = intent.getIntExtra("extra",PlayingService.MODE_SEQUENCE);
            }

            if (action.equals(PlayingService.ACTION_UPDATE_DURATION)){
                Bundle bundle = intent.getBundleExtra("extra");
                textDuration.setText(bundle.getString("totalTime"));
                seekBar.setMax(bundle.getInt("duration"));
            }

            if (action.equals(PlayingService.ACTION_UPDATE_PROGRESS)){
                Bundle bundle = intent.getBundleExtra("extra");
                textNowDuration.setText(bundle.getString("currentTime"));
                if (!isChanging) seekBar.setProgress(bundle.getInt("currentDuration"));
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

            case R.id.modeIconDrawView:
                if (mode == PlayingService.MODE_SEQUENCE) mode = PlayingService.MODE_ONE_LOOP;
                else mode++;
                bdHelper.broadcastDeliver(PlayingService.CONTROL_MODE,mode);
                modeIconDrawView.changeModeIcon(mode);
                modeIconDrawView.invalidate();
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
        layoutAlbumPicAndLrc = (FrameLayout) findViewById(R.id.albumpic_and_lrc);
        btnPlayAndPause = (Button) findViewById(R.id.playing_song_play_pause);
        btnPre = (Button) findViewById(R.id.playing_song_pre);
        btnNext = (Button) findViewById(R.id.playing_song_next);
        seekBar = (SeekBar) findViewById(R.id.playing_song_seek_bar);
        textTitle = (TextView) findViewById(R.id.playing_song_title);
        textArtist = (TextView) findViewById(R.id.playing_song_artist);
        textNowDuration = (TextView) findViewById(R.id.playing_song_now_duration);
        textDuration = (TextView) findViewById(R.id.playing_song_duration);
        modeIconDrawView = (ModeIconDrawView) findViewById(R.id.modeIconDrawView);

        seekBar.setOnSeekBarChangeListener(new MySeekBar());
        btnPlayAndPause.setOnClickListener(this);
        btnPre.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        modeIconDrawView.setOnClickListener(this);
    }

    public void initAlbumPicAndLrcHeight(){
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = PlayingActivity.this.getWindowManager();
        wm.getDefaultDisplay().getMetrics(dm);
        ViewGroup.LayoutParams params = layoutAlbumPicAndLrc.getLayoutParams();
        params.height = dm.widthPixels + dimensHelper.getStatusBarHeight();
        layoutAlbumPicAndLrc.setLayoutParams(params);
    }


    class MySeekBar implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            isChanging=true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            bdHelper.broadcastDeliver(PlayingService.CONTROL_PROGRESS,PlayingActivity.this.seekBar.getProgress());
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
    }

    @Override
    public void onResume(){
        super.onResume();
        if(position==-1||position==PlayingService.position) {
            bdHelper.broadcastDeliver(PlayingService.CONTROL_ASK_FOR_STATE);
        }
        else bdHelper.broadcastDeliver(PlayingService.CONTROL_BEGIN, position);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public void showTip(String s){
        Toast.makeText(PlayingActivity.this, s, Toast.LENGTH_SHORT).show();
    }

}


