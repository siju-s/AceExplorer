package com.siju.acexplorer;

import android.app.Application;

import com.flurry.android.FlurryAgent;

/**
 * Created by Siju on 05-09-2016.
 */
public class AceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlurryAgent.setLogEnabled(true);
        FlurryAgent.init(this, "NBH7DY8FPN4MFXJ274QP");
    }
}
