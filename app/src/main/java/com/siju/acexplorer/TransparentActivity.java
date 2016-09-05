package com.siju.acexplorer;

import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.siju.acexplorer.filesystem.ui.DialogBrowseFragment;
import com.siju.acexplorer.utils.FlurryUtils;

/**
 * Created by Siju on 04-09-2016.
 */
public class TransparentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null && intent.getAction() != null && intent.getAction().equals(RingtoneManager
                .ACTION_RINGTONE_PICKER)) {
//            mRingtonePickerIntent = true;
            showRingtonePickerDialog();
            FlurryAgent.logEvent(FlurryUtils.RINGTONE_PICKER);
        }
    }

    private void showRingtonePickerDialog() {

        DialogBrowseFragment dialogFragment = new DialogBrowseFragment();
        Bundle args = new Bundle();
        args.putBoolean("ringtone_picker", true);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "Browse Fragment");


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("TAG", "On activity result");
        super.onActivityResult(requestCode, resultCode, data);
        FlurryAgent.logEvent(FlurryUtils.RINGTONE_PICKER_RESULT, resultCode == RESULT_OK);
        finish();
    }
}
