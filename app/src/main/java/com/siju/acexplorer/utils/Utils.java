package com.siju.acexplorer.utils;

import android.content.Context;

import com.siju.acexplorer.R;

/**
 * Created by Siju on 17-09-2016.
 */
public class Utils {

    public static  boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }
}
