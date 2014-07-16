package com.HandleStudio.lolmusic.lolmusic;

import android.graphics.Color;
import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;

public class AlphaForegroundColorSpan extends ForegroundColorSpan {

    private float alpha;

    public AlphaForegroundColorSpan(int color) {
        super(color);
	}

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(alpha);
    }

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(getAlphaColor());
	}

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    private int getAlphaColor() {
        int foregroundColor = getForegroundColor();
        return Color.argb((int) (alpha * 255), Color.red(foregroundColor), Color.green(foregroundColor), Color.blue(foregroundColor));
    }
}