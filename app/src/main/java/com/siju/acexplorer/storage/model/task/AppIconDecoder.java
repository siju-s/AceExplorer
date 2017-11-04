package com.siju.acexplorer.storage.model.task;

/**
 * Created by sj on 29/10/17.
 */

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.drawable.DrawableResource;
import com.bumptech.glide.util.Util;

import java.io.IOException;

public class AppIconDecoder implements ResourceDecoder<ApplicationInfo, Drawable> {
    private final Context context;

    AppIconDecoder(Context context) {
        this.context = context;
    }

    @Override
    public boolean handles(ApplicationInfo source, Options options) throws IOException {
        return true;
    }

    @Nullable
    @Override
    public Resource<Drawable> decode(ApplicationInfo applicationInfo, int width, int height, Options
            options) throws IOException {
        if (applicationInfo == null) {
            return null;
        }
        Drawable icon = applicationInfo.loadIcon(context.getPackageManager());

        return new DrawableResource<Drawable>(icon) {

            @Override
            public Class<Drawable> getResourceClass() {
                return Drawable.class;
            }

            @Override
            public int getSize() {
                if (drawable instanceof BitmapDrawable) {
                    return Util.getBitmapByteSize(((BitmapDrawable) drawable).getBitmap());
                } else {
                    return 1;
                }
            }

            @Override
            public void recycle() {

            }
        };
    }
}