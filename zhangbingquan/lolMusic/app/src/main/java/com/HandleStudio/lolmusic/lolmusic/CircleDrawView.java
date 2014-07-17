package com.HandleStudio.lolmusic.lolmusic;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by 2bab on 14-7-11.
 *
 */
public class CircleDrawView extends View{

    private float radius;
    public static int color = Color.BLACK;

    public CircleDrawView(Context context, AttributeSet attrs){
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.CircleDrawView);
        radius = a.getDimensionPixelSize(R.styleable.CircleDrawView_diameter,0)/2;
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas){
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(radius,radius,radius,paint);
    }

}
