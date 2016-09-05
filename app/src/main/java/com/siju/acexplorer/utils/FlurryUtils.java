package com.siju.acexplorer.utils;

import com.flurry.android.FlurryAgent;

/**
 * Created by Siju on 05-09-2016.
 */
public class FlurryUtils {
    public static final String HOME_ENABLED = "homescreen";
    public static final String STORAGE_DIRS = "storage_dirs";


    public static final String RINGTONE_PICKER = "ringtone_picker";
    public static final String RINGTONE_PICKER_RESULT = "ringtone_picker_result";

    public static void logOperation (String tag,String method,String operation) {
        FlurryAgent.logEvent(tag + " " + method + " " + operation);

    }

}
