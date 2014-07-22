package com.kexife.market;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

public class AppListActivity extends Activity implements OnItemClickListener, OnScrollListener{

    private MyLogger logger = MyLogger.kLog();

    private NetworkThread networkThread;

    private MyBaseAdapter myBaseAdapter;
    private ArrayList<HashMap<String,Object>> dataList;

    private ListView listView;
    private FooterView footerView;

    private boolean loading = false;
    private int lastVisibleItem = 0;
    private static int APP_LOAD_COUNT = 20;

    private SharedPreferences preferences;
    public static int updateTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        listView = (ListView) findViewById(R.id.app_list_view);

        //Add footer view
        footerView = new FooterView(this,null,FooterView.LOAD_MODE);
        footerView.setOnClickListener(null);
        listView.addFooterView(footerView);

        //Set adapter
        dataList = new ArrayList<HashMap<String, Object>>();
        myBaseAdapter = new MyBaseAdapter(this, dataList, R.layout.app_list_item,
                new int[]{R.id.app_list_item_icon,R.id.app_list_item_title,R.id.app_list_item_detail});
        listView.setAdapter(myBaseAdapter);

        //Set click and scroll listener
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(this);

        //Get update time of icons in cache
        preferences = getSharedPreferences("market",MODE_PRIVATE);
        updateTime = preferences.getInt("updateTime",0);

        //Thread to download app list
        Handler handler = new MessageHandler();
        networkThread = new NetworkThread(handler,this);
        networkThread.start();

        getApps(0,APP_LOAD_COUNT);
    }

    @Override
    public void onPause(){
        super.onPause();
        if(dataList.size()>0 ){
            int newUpdate = (Integer)dataList.get(0).get("updateTime");
            if(newUpdate>preferences.getInt("updateTime",0) ){
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("updateTime",newUpdate);
                editor.commit();
                logger.w("Update preference: old "+ updateTime +", new "+ newUpdate);
            }
        }
    }

    private void getApps(int start,int count){

        footerView.setDisplayMode(FooterView.LOAD_MODE);

        Message message = new Message();
        message.what = MessageType.GET_LIST;
        Bundle bundle = new Bundle();
        bundle.putInt("START",start);
        bundle.putInt("COUNT",count);
        message.setData(bundle);

        networkThread.getHandler().sendMessage(message);
    }

    private class MessageHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            loading = false;
            switch(msg.what){
                case MessageType.RESPONSE:
                    @SuppressWarnings("unchecked")
                    ArrayList<HashMap<String, Object>> appendList = (ArrayList<HashMap<String, Object>>)msg.getData().getSerializable("LIST");
                    dataList.addAll(appendList);
                    myBaseAdapter.updateList(dataList);
                    break;
                case MessageType.NO_DATA:
                    footerView.setDisplayMode(FooterView.NO_MORE_APPS_MODE);
                    break;
                case MessageType.EXCEPTION:
                    footerView.setDisplayMode(FooterView.NETWORK_ERROR_MODE);
                    break;
                default:
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        Intent intent = new Intent(AppListActivity.this,AppDetailActivity.class);
        intent.putExtra("DETAIL",dataList.get(position));
        startActivity(intent);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,int totalItemCount){
        //-1 because of footerView
        lastVisibleItem = firstVisibleItem + visibleItemCount - 1;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState){
        if(lastVisibleItem == dataList.size() && scrollState==SCROLL_STATE_IDLE ){
            if(!loading){
                loading = true;
                getApps(dataList.size(),APP_LOAD_COUNT);
            }
        }
    }

}
