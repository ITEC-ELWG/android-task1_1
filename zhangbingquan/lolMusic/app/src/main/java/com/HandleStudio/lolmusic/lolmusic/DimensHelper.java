package com.HandleStudio.lolmusic.lolmusic;

import android.content.Context;
import android.graphics.RectF;
import android.os.Build;
import android.util.TypedValue;

import java.lang.reflect.Field;

/**
 * Created by 2bab on 14-7-11.
 *
 */

public class DimensHelper {

    private int actionBarHeight;
    private Context context;
    private TypedValue TypedValue = new TypedValue();

    public DimensHelper(Context con){
        context = con;
    }

    public int getStatusBarHeight(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Class<?> c = null;
            Object obj = null;
            Field field = null;
            int x = 0, sbar = 0;
            try {
                c = Class.forName("com.android.internal.R$dimen");
                obj = c.newInstance();
                field = c.getField("status_bar_height");
                x = Integer.parseInt(field.get(obj).toString());
                sbar = context.getResources().getDimensionPixelSize(x);
                return sbar;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;//todo: check more devices and throw a message
    }

    public int getActionBarHeight() {
        //减少重复获取
        if (actionBarHeight != 0) {
            return actionBarHeight;
        }
        //
        context.getTheme().resolveAttribute(android.R.attr.actionBarSize, TypedValue, true);
        actionBarHeight = TypedValue.complexToDimensionPixelSize(TypedValue.data,
                context.getResources().getDisplayMetrics());
        return actionBarHeight;
    }

    //4.4下ActionBar透明时不会把home icon垂直居中
    public float getLackOfKitkatHeight(RectF r){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return getActionBarHeight() - r.height();
        }
        return 0F;
    }

}
