package com.siju.acexplorer.storage.model.task;

import com.bumptech.glide.load.Key;

import java.security.MessageDigest;


public class CustomKey implements Key {

    private String path;

    CustomKey(String path) {
        this.path = path;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return path.equals(obj);
        }
        return super.equals(obj);
    }
}
