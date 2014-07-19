package com.HandleStudio.lolmusic.lolmusic;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by 2bab on 14-7-10.
 *
 */

public class PlayingActivity extends Activity implements View.OnClickListener{

    private static final String TAG = "PlayingActivity";

    private DimensHelper dimensHelper;
    private BroadcastDeliverHelper bdHelper;

    private PlayingActivityReceiver receiver;
    private int mode = PlayingService.MODE_SEQUENCE;
    private int position;
    private boolean isChanging = false;
    boolean playState=true;

    private FrameLayout layoutAlbumPicAndLrc;
    private ModeIconDrawView modeIconDrawView;
    private TextView textTitle;
    private TextView textArtist;
    private TextView textNowDuration;
    private TextView textDuration;
    private FrameLayout fakeBtnPlayAndPause;
    private FrameLayout fakeBtnPre;
    private FrameLayout fakeBtnNext;
    private CircleDrawView circleDrawPre;
    private CircleDrawView circleDrawPlayPause;
    private CircleDrawView circleDrawNext;
    private ImageView btnPlayPause;
    private ImageView btnPre;
    private ImageView btnNxet;
    private SeekBar seekBar;
    private ImageView imageAlbumCover;

    public static Bitmap bitmap;
    private Handler handler;
    private static final int defaultColor = -3354940;
    private final int msgInvalidateColor = 0x1234;
    private static int newThreadFlag = 0;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);

        //init
        dimensHelper = new DimensHelper(this);
        bdHelper = new BroadcastDeliverHelper(this);
        initHandler();
        initActionBar();
        initFindView();
        initAlbumCoverAndLrcHeight();//画方形

        //获得歌曲在数组的顺序
        Intent intent = getIntent();
        position = intent.getIntExtra("index",-1);

        preferences = getSharedPreferences("lolmusic",MODE_PRIVATE);
        editor = preferences.edit();
    }


    public void registerUIReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayingService.ACTION_UPDATE_PLAY_PAUSE);
        filter.addAction(PlayingService.ACTION_UPDATE_CURRENT_MUSIC);
        filter.addAction(PlayingService.ACTION_UPDATE_MODE);
        filter.addAction(PlayingService.ACTION_UPDATE_DURATION);//总时间
        filter.addAction(PlayingService.ACTION_UPDATE_PROGRESS);//进度条
        filter.addAction(PlayingService.ACTION_UPDATE_ALBUM_COVER);
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
                    btnPlayPause.setBackgroundResource(R.drawable.pause);
                }
                else {
                    playState = false;
                    btnPlayPause.setBackgroundResource(R.drawable.play);
                }
            }

            if (action.equals(PlayingService.ACTION_UPDATE_CURRENT_MUSIC)){
                Bundle bundle = intent.getBundleExtra("extra");
                textTitle.setText(bundle.getString("title"));
                textArtist.setText(bundle.getString("artist"));
            }

            if (action.equals(PlayingService.ACTION_UPDATE_MODE)){
                mode = intent.getIntExtra("extra",PlayingService.MODE_SEQUENCE);
                modeIconDrawView.changeModeIcon(mode);
                modeIconDrawView.invalidate();
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

            if (action.equals(PlayingService.ACTION_UPDATE_ALBUM_COVER)){
                Bundle bundle = intent.getBundleExtra("extra");
                int songId = bundle.getInt("songId");
                int albumId = bundle.getInt("albumId");
                bitmap = AlbumCoverFinder.getAlbumCover(PlayingActivity.this,songId,albumId,true);
                imageAlbumCover.setImageBitmap(bitmap);
                if (!AlbumCoverFinder.defaultCover) {
                    int currentColor = preferences.getInt(String.valueOf(songId),0);
                    if (currentColor==0) {
                        Runnable runnableChangeColor = new ChangeColor(songId,newThreadFlag);
                        Thread threadChangeColor = new Thread(runnableChangeColor);
                        threadChangeColor.start();
                    }
                    else {
                        CircleDrawView.color = currentColor;
                        ModeIconDrawView.color = currentColor;
                        handler.sendEmptyMessage(msgInvalidateColor);
                    }
                }
                else {
                    CircleDrawView.color = defaultColor;
                    ModeIconDrawView.color = defaultColor;
                    handler.sendEmptyMessage(msgInvalidateColor);
                }


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
                newThreadFlag = (int)(Math.random()*10);
                break;

            case R.id.playing_song_next:
                bdHelper.broadcastDeliver(PlayingService.CONTROL_NEXT);
                newThreadFlag = (int)(Math.random()*10);
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


    public void initHandler(){
        handler = new Handler(){
          @Override
          public void handleMessage(Message msg){
              if (msg.what==msgInvalidateColor){
                  circleDrawPre.invalidate();
                  circleDrawNext.invalidate();
                  circleDrawPlayPause.invalidate();
                  modeIconDrawView.invalidate();
              }
          }
        };
    }

    public void initActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_USE_LOGO);
        actionBar.setIcon(R.drawable.left_arrow);
        actionBar.setHomeButtonEnabled(true);
    }


    public void initFindView(){
        layoutAlbumPicAndLrc = (FrameLayout) findViewById(R.id.albumpic_and_lrc);
        fakeBtnPlayAndPause = (FrameLayout) findViewById(R.id.playing_song_play_pause);
        fakeBtnPre = (FrameLayout) findViewById(R.id.playing_song_pre);
        fakeBtnNext = (FrameLayout) findViewById(R.id.playing_song_next);
        circleDrawPre = (CircleDrawView) findViewById(R.id.circle_draw_view_1);
        circleDrawPlayPause = (CircleDrawView) findViewById(R.id.circle_draw_view_2);
        circleDrawNext = (CircleDrawView) findViewById(R.id.circle_draw_view_3);
        btnPlayPause = (ImageView)findViewById(R.id.btn_play_pause);
        btnPre = (ImageView)findViewById(R.id.btn_pre);
        btnNxet = (ImageView)findViewById(R.id.btn_next);
        seekBar = (SeekBar) findViewById(R.id.playing_song_seek_bar);
        textTitle = (TextView) findViewById(R.id.playing_song_title);
        textArtist = (TextView) findViewById(R.id.playing_song_artist);
        textNowDuration = (TextView) findViewById(R.id.playing_song_now_duration);
        textDuration = (TextView) findViewById(R.id.playing_song_duration);
        modeIconDrawView = (ModeIconDrawView) findViewById(R.id.modeIconDrawView);
        imageAlbumCover = (ImageView) findViewById(R.id.album_picture);


        seekBar.setOnSeekBarChangeListener(new MySeekBar());
        fakeBtnPlayAndPause.setOnClickListener(this);
        fakeBtnPre.setOnClickListener(this);
        fakeBtnNext.setOnClickListener(this);
        modeIconDrawView.setOnClickListener(this);
    }


    public void initAlbumCoverAndLrcHeight(){
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = PlayingActivity.this.getWindowManager();
        wm.getDefaultDisplay().getMetrics(dm);
        ViewGroup.LayoutParams params = layoutAlbumPicAndLrc.getLayoutParams();
        params.height = dm.widthPixels;
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

        //开始播放或加载当前播放状态
        if(position==-1||position==PlayingService.position) {
            bdHelper.broadcastDeliver(PlayingService.CONTROL_ASK_FOR_STATE);
        }
        else bdHelper.broadcastDeliver(PlayingService.CONTROL_BEGIN, position);

        //改变按钮颜色
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

    public class ChangeColor implements Runnable{

        int songId;
        int newThreadFlag;

        public ChangeColor(int sid,int flag){
            songId = sid;
            newThreadFlag = flag;
        }

        @Override
        public void run(){
            List<int[]> result = new ArrayList<int[]>();
            try {
                result = PicMainColorHelper.compute(bitmap, 5);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int[] RGBMainColor = result.get(0);
            int intColor = Color.rgb(RGBMainColor[0],RGBMainColor[1],RGBMainColor[2]);
            CircleDrawView.color = intColor;
            ModeIconDrawView.color = intColor;
            if ((newThreadFlag!=0)&&(newThreadFlag == PlayingActivity.newThreadFlag))
                handler.sendEmptyMessage(msgInvalidateColor);
            editor.putInt(String.valueOf(songId),intColor);
            editor.commit();
        }
    }


}


