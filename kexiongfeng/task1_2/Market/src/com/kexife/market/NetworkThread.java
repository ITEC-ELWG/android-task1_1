package com.kexife.market;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NetworkThread extends Thread{

    private static final String API_URL="http://192.168.191.1/WdjApps/api.php";

    private MyLogger logger = MyLogger.kLog();

    private Handler uiHandler;
    private Handler mHandler;

    private static Context mContext;

    public NetworkThread(Handler handler,Context context){
        this.uiHandler = handler;
        mContext = context;
    }

    public void run(){
        Looper.prepare();
        synchronized(this) {
            mHandler = new MessageHandler();
            notifyAll();
        }
        Looper.loop();
    }

    //Using Thread.mHandle.sendMessage(...) immediately after Thread.start() may cause NullPointException, because mHandle may have not be instantiated yet.
    //Use synchronized to solve this problem, just like android.os.HandlerThread class.
    public Handler getHandler(){
        if(!isAlive()){
            return null;
        }

        synchronized(this){
            while(isAlive() && mHandler==null){
                try{
                    wait();
                }catch(InterruptedException e){
                }
            }
        }
        return mHandler;
    }

    private class MessageHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MessageType.GET_LIST:
                    getList(msg);
                    break;
                case MessageType.GET_DETAIL:
                    getDetail(msg);
                    break;
                default:
            }
        }
    }

    private void getList(Message msg){
        Bundle revBundle = msg.getData();
        int start = revBundle.getInt("START");
        int count = revBundle.getInt("COUNT");
        String apiUrl = getListApiUrl(start, count);
        try {
            String jsonStr = NetworkUtils.getApiResponse(apiUrl);
            JSONArray jsonArray = new JSONArray(jsonStr);
            @SuppressWarnings("unchecked")
            ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) JsonHelper.toList(jsonArray);
            ArrayList<HashMap<String, Object>> appendList = getCacheList(list);

            if(appendList.size()>0){
                Bundle bundle = new Bundle();
                bundle.putSerializable("LIST", appendList);
                sendMessageToUi(MessageType.RESPONSE,bundle);
            }else{
                sendMessageToUi(MessageType.NO_DATA,null);
            }
        }catch (Exception e) {
            logger.e(e);
            sendMessageToUi(MessageType.EXCEPTION,null);
        }
    }

    private void getDetail(Message msg){
        Bundle revBundle = msg.getData();
        int id = revBundle.getInt("ID");
        String apiUrl =  getDetailApiUrl(id);
        try{
            String jsonStr = NetworkUtils.getApiResponse(apiUrl);
            JSONObject jsonObject = new JSONObject(jsonStr);
            HashMap<String, Object> moreDetail = (HashMap<String, Object>) JsonHelper.toMap(jsonObject);

            Bundle bundle = new Bundle();
            bundle.putSerializable("MORE", moreDetail);
            sendMessageToUi(MessageType.RESPONSE,bundle);

        }catch(Exception e){
            logger.e(e);
            sendMessageToUi(MessageType.EXCEPTION,null);
        }
    }

    private void sendMessageToUi(int what, Bundle bundle){
        Message msg = new Message();
        msg.what = what;
        if(bundle != null) msg.setData(bundle);
        uiHandler.sendMessage(msg);
    }

    private String getListApiUrl(int start,int count){
        return API_URL + "?start=" + start + "&count=" + count;
    }

    private String getDetailApiUrl(int id){
        return API_URL + "?id=" + id;
    }

    //Save bitmap to cache and return list with file path
    private ArrayList<HashMap<String,Object>> getCacheList(ArrayList<HashMap<String,Object>> list) throws Exception{

        ArrayList<HashMap<String,Object>> cacheList = new ArrayList<HashMap<String, Object>>();

        int updateTime=0;

        for(HashMap<String,Object> item : list){

            HashMap<String,Object> newItem = new HashMap<String, Object>();
            for(Map.Entry<String,Object> entry :item.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();

                if(key.equals("icon")){
                    String filename = ((String)value).substring(((String)value).lastIndexOf('/') + 1);

                    File file = new File(mContext.getCacheDir(),filename);
                    if(AppListActivity.updateTime>=updateTime && file.exists() ){
                        newItem.put(key,file.getPath());
                    }else{
                        Bitmap bitmap = NetworkUtils.downloadImage((String) value);
                        saveBitmapToCache(bitmap,file.getPath());
                        bitmap.recycle();

                        newItem.put(key,file.getPath());
                    }
                }else{
                    newItem.put(key,value);
                    if(key.equals("updateTime")) updateTime=(Integer)value;
                }
            }
            cacheList.add(newItem);
        }
        return cacheList;
    }

    private void saveBitmapToCache(Bitmap bitmap, String filePath) throws Exception{

        File file = new File(filePath);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } finally {
            if (out != null) out.close();
        }
    }

}
