package com.siju.acexplorer.storage.model.task;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Created by sj on 29/10/17.
 */

@GlideModule
public class CustomGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        registry.append(ApplicationInfo.class, Drawable.class, new AppIconDecoder(context));
        registry.append(String.class, ApplicationInfo.class, new PassAppModelLoader.PassAppModelLoaderFactory());
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}