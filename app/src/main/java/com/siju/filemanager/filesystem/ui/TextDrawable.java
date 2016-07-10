package com.siju.filemanager.filesystem.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.siju.filemanager.R;
import com.siju.filemanager.common.Logger;

/**
 * Created by SIJU on 10-07-2016.
 */

public class TextDrawable extends Drawable {

    private final String text;
    private final Paint paint;

    public TextDrawable(Context context,String text) {

        this.text = text;
        int size = context.getResources().getDimensionPixelSize(R.dimen.drag_shadow_font);
         Logger.log("TAG","Size="+size);
        this.paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setTextSize(size);
        paint.setAntiAlias(true);
//        paint.setFakeBoldText(true);
//        paint.setShadowLayer(6f, 0, 0, Color.BLACK);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawText(text, 0, 0, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
