package com.siju.acexplorer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public final class AdMobActivity extends Activity {

    public static AdMobActivity AdMobMemoryLeakWorkAroundActivity;

    public AdMobActivity() {
        super();
        if (AdMobMemoryLeakWorkAroundActivity != null) {
            throw new IllegalStateException("This activity should be created only once during the entire application life");
        }
        AdMobMemoryLeakWorkAroundActivity = this;
        Log.d("AdmobActivity", "AdMobActivity: "+AdMobMemoryLeakWorkAroundActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("CHAT", "in onCreate - AdMobActivity");
        finish();
    }

/*    public static final void startAdMobActivity(Activity activity) {
        Log.i("CHAT", "in startAdMobActivity");
        Intent i = new Intent(activity,AdMobActivity.class);
//        i.setComponent(new ComponentName(activity.getApplicationContext(), AdMobActivity.class));
        activity.startActivity(i);
    }*/
}
