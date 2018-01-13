package com.siju.acexplorer.ui.autoplay;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;


public class AutoPlayContainer extends FrameLayout {
    private AutoPlayView autoPlayView;

    public AutoPlayContainer(Context context) {
        super(context);
        init();
    }

    public AutoPlayContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoPlayContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AutoPlayContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public AutoPlayView getCustomVideoView() {
        return autoPlayView;
    }


    private void init() {
        autoPlayView = new AutoPlayView(getContext());
        ImageView image = new ImageView(getContext());
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        this.addView(autoPlayView);
        this.addView(image);
    }
}
