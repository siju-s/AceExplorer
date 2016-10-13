package com.siju.acexplorer.utils;

import android.content.Context;
import android.os.Build;

import com.siju.acexplorer.R;

/**
 * Created by Siju on 17-09-2016.
 */
public class Utils {

    public static  boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    public static boolean isAtleastLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
