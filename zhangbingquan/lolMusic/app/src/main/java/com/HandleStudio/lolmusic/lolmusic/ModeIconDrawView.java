package com.HandleStudio.lolmusic.lolmusic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by 2bab on 14-7-15.
 * To Draw Mode Icon in PlayingActivity
 */
public class ModeIconDrawView extends View {

    private Paint paintLine;
    private Paint paintTriangle;
    private int mode = PlayingService.MODE_SEQUENCE;

    public ModeIconDrawView(Context context,AttributeSet set) {
        super(context,set);

    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);                  //白色背景

        paintLine = new Paint();
        paintLine.setAntiAlias(true);                       //设置画笔为无锯齿
        paintLine.setColor(Color.BLACK);                    //设置画笔颜色
        paintLine.setStrokeWidth((float) 5.0);              //线宽
        paintLine.setStyle(Paint.Style.STROKE);

        paintTriangle = new Paint();
        paintTriangle.setAntiAlias(true);
        paintTriangle.setColor(Color.BLACK);
        paintTriangle.setStyle(Paint.Style.FILL);

        switch (mode){
            case PlayingService.MODE_ALL_LOOP:
                allLoop(canvas);
                break;
            case PlayingService.MODE_ONE_LOOP:
                oneLoop(canvas);
                break;
            case PlayingService.MODE_RANDOM:
                random(canvas);
                break;
            case PlayingService.MODE_SEQUENCE:
                sequence(canvas);
        }

    }

    public void allLoop(Canvas canvas){

        RectF arc1 = new RectF(10,10,40,40);
        RectF arc2 = new RectF(30,7,60,37);

        canvas.drawArc(arc1,150,120,false,paintLine);
        canvas.drawArc(arc2,330,120,false,paintLine);
        canvas.drawLine(25,10,45,10,paintLine);
        canvas.drawLine(25,37,45,37,paintLine);

        Path pathTriangle1 = new Path();
        pathTriangle1.moveTo(45, 2);
        pathTriangle1.lineTo(58.856F, 10);
        pathTriangle1.lineTo(45, 18);
        pathTriangle1.close();
        canvas.drawPath(pathTriangle1,paintTriangle);

        Path pathTriangle2 = new Path();
        pathTriangle2.moveTo(25, 29);
        pathTriangle2.lineTo(11.146F, 37);
        pathTriangle2.lineTo(25, 45);
        pathTriangle2.close();
        canvas.drawPath(pathTriangle2,paintTriangle);
    }


    public void oneLoop(Canvas canvas){
        allLoop(canvas);
        Paint paintText = new Paint();
        paintText.setAntiAlias(true);
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(20);
        canvas.drawText("1",30,30F,paintText);

    }

    public void sequence(Canvas canvas){
        canvas.drawLine(10,12,45,12,paintLine);
        canvas.drawLine(10,35,45,35,paintLine);
        twoTriangleToLeft(canvas);
    }

    public void random(Canvas canvas){
        float x,y;
        for (x=11;x<14.1F;x+=0.1)
        {
            y = (float)Math.sin(x)*12+23;
            canvas.drawPoint(x*13-138,y,paintLine);
        }
        for (x=14.1F;x<17.2F;x+=0.1){
            y = (float)Math.sin(x)*12+23;
            canvas.drawPoint(x*13-178,y,paintLine);
        }
        twoTriangleToLeft(canvas);
    }

    public void twoTriangleToLeft(Canvas canvas){

        Path pathTriangle1 = new Path();
        pathTriangle1.moveTo(45, 4);
        pathTriangle1.lineTo(58.856F, 12);
        pathTriangle1.lineTo(45, 20);
        pathTriangle1.close();
        canvas.drawPath(pathTriangle1,paintTriangle);

        Path pathTriangle2 = new Path();
        pathTriangle2.moveTo(45, 27);
        pathTriangle2.lineTo(58.856F, 35);
        pathTriangle2.lineTo(45, 43);
        pathTriangle2.close();
        canvas.drawPath(pathTriangle2,paintTriangle);
    }

    public void changeModeIcon(int m){
        mode = m;
    }

}
