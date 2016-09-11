package com.siju.acexplorer;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;

import io.fabric.sdk.android.Fabric;


/**
 * Created by Siju on 05-09-2016.
 */
public class AceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FlurryAgent.setLogEnabled(true);
        FlurryAgent.init(this, "NBH7DY8FPN4MFXJ274QP");

        Factory.setInstance(this);
    }
}
