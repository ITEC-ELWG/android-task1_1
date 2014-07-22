package com.HandleStudio.lolmusic.lolmusic;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    int actionBarTitleColor;
    int headerHeight;
    private int minHeaderTranslation;
    private ListView listView;
    private ImageView headerLogo;
    private View header;
    private View placeHolderView;
    private AccelerateDecelerateInterpolator smoothInterpolator;
    private KenBurnsView headerPicture;

    //辅助矩形，1是大头像，2是小头像
    private RectF rect1 = new RectF();
    private RectF rect2 = new RectF();

    //ActionBar的标题透明
    private static String actionBarTitle = "lolMusic";
    private AlphaForegroundColorSpan mAlphaForegroundColorSpan;
    private SpannableString mSpannableString;

    private FileSearchHelper fileSearchHelper;
    private DimensHelper dimensHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMinHeaderTranslation();
        initFindView();
        initAlphaTitleParams();
        initActionBar();
        initListView();
        startPlayingService();

        headerLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PlayingService.position>=0){
                    Intent intent = new Intent(MainActivity.this, PlayingActivity.class);
                    startActivity(intent);
                }
                else Toast.makeText(MainActivity.this,"当前没有正在播放的歌曲",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        if (PlayingService.position!=-1) {
            actionBarTitle = fileSearchHelper.getFileTitle(PlayingService.position);
            mSpannableString = new SpannableString(actionBarTitle);
            mSpannableString.setSpan(mAlphaForegroundColorSpan, 0, mSpannableString.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            getActionBar().setTitle(mSpannableString);
        }

        if (PlayingService.position == -1)
            headerPicture.setResourceIds(R.raw.album_pic_default, R.raw.album_pic_default);
        else headerPicture.setImageBitmap(PlayingActivity.bitmap,PlayingActivity.bitmap);

    }

    private void initMinHeaderTranslation(){
        dimensHelper = new DimensHelper(this);
        smoothInterpolator = new AccelerateDecelerateInterpolator();
        headerHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        //HeaderView高度减去ActionBar的高度得到最多能卷动的距离
        minHeaderTranslation = -headerHeight + dimensHelper.getActionBarHeight()+ dimensHelper.getStatusBarHeight();

    }

    private void initAlphaTitleParams(){
        //todo: color protect(1dp drop shadow)
        actionBarTitleColor = getResources().getColor(R.color.actionbar_title_color);
        mSpannableString = new SpannableString(actionBarTitle);
        mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(actionBarTitleColor);
    }

    private void initFindView(){
        listView = (ListView) findViewById(R.id.listview);
        header = findViewById(R.id.header);
        headerLogo = (ImageView) findViewById(R.id.header_logo);
        headerPicture = (KenBurnsView) findViewById(R.id.header_picture);
    }

    private void startPlayingService(){
        Intent serviceIntent = new Intent();
        serviceIntent.setAction("com.HandleStudio.lolmusic.lolmusic.PlayingService");
        startService(serviceIntent);
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_transparent);
    }

    private void initListView() {

        fileSearchHelper = new FileSearchHelper(MainActivity.this);

        //初始化 HeaderView
        placeHolderView = getLayoutInflater().inflate(R.layout.view_header_placeholder, listView, false);
        listView.addHeaderView(placeHolderView);

        listView.setAdapter(fileSearchHelper.getMusicListAdapter());

        //移动 HeaderView 和 Logo（头像）
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int scrollY = getScrollY();
                //移动并最终附着于 Actionbar，注意两个参数都是负数
                header.setTranslationY(Math.max(-scrollY, minHeaderTranslation));
                //header_logo --> actionbar icon 过程中 1.变化比率 2.图标变化 3.Title浮现
                float ratio = clamp(header.getTranslationY() / minHeaderTranslation, 0.0f, 1.0f);//变化比例在0F-1F
                interpolate(headerLogo, getActionBarIconView(), smoothInterpolator.getInterpolation(ratio));
                setTitleAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, PlayingActivity.class);
                intent.putExtra("index", i - 1);
                //Log.e("position",String.valueOf(i));
                startActivity(intent);
            }
        });
    }




    private void setTitleAlpha(float alpha) {
        mAlphaForegroundColorSpan.setAlpha(alpha);
        mSpannableString.setSpan(mAlphaForegroundColorSpan, 0, mSpannableString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActionBar().setTitle(mSpannableString);
    }

    //比率限制
    public static float clamp(float value, float max, float min) {
        return Math.max(Math.min(value, min), max);
    }

    //动画的路径和缩放大小
    private void interpolate(View view1, View view2, float interpolation) {
        getOnScreenRect(rect1, view1);
        getOnScreenRect(rect2, view2);

        float scaleX = 1.0F + interpolation * (rect2.width() / rect1.width() - 1.0F);
        float scaleY = 1.0F + interpolation * (rect2.height() / rect1.height() - 1.0F);
        float translationX = 0.5F * (interpolation * (rect2.left + rect2.right - rect1.left - rect1.right));
        float translationY = 0.5F * (interpolation * (rect2.top + rect2.bottom - rect1.top - rect1.bottom
                                    + dimensHelper.getStatusBarHeight() + dimensHelper.getLackOfKitkatHeight(rect2)));

        view1.setTranslationX(translationX);
        view1.setTranslationY(translationY - header.getTranslationY());
        view1.setScaleX(scaleX);
        view1.setScaleY(scaleY);
    }

    //辅助的矩形
    private RectF getOnScreenRect(RectF rect, View view) {
        rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        return rect;
    }

    //计算listView的滚动距离
    public int getScrollY() {
        View c = listView.getChildAt(0);
        if (c == null) {
            return 0;
        }

        int firstVisiblePosition = listView.getFirstVisiblePosition();
        int top = c.getTop();

        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = placeHolderView.getHeight();
        }

        return -top + firstVisiblePosition * c.getHeight() + headerHeight;
    }

    private ImageView getActionBarIconView() {
        return (ImageView) findViewById(android.R.id.home);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        //stopService(serviceIntent);
    }

}
