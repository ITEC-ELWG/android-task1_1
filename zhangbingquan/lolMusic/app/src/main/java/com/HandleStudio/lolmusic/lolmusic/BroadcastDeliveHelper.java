package com.HandleStudio.lolmusic.lolmusic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by 2bab on 14-7-13.
 */
public class BroadcastDeliveHelper {

    private Context context;

    public BroadcastDeliveHelper(Context con){
        context = con;
    }

    public void broadcastDeliver(String action){
        Intent intent = new Intent();
        intent.setAction(action);
        context.sendBroadcast(intent);
    }

    public void broadcastDeliver(String action,int extra){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("extra",extra);
        context.sendBroadcast(intent);
    }

    public void broadcastDeliver(String action,String extra){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("extra",extra);
        context.sendBroadcast(intent);
    }
}
