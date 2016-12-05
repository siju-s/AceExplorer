package com.siju.acexplorer;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;


public class AceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
/*        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
        }*/
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
/*        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);*/

        Factory.setInstance(this);
    }
}
