package com.siju.acexplorer;

import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.ui.DialogBrowseFragment;


/**
 * Created by Siju on 04-09-2016.
 */
public class TransparentActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private DialogBrowseFragment dialogFragment;
    private final String FRAGMENT_TAG = "Browse_Frag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null && intent.getAction() != null && intent.getAction().equals(RingtoneManager
                .ACTION_RINGTONE_PICKER)) {
//            mRingtonePickerIntent = true;
            showRingtonePickerDialog(intent);
        }
    }

    private void showRingtonePickerDialog(Intent intent) {

        dialogFragment = new DialogBrowseFragment();
        dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, checkTheme());
        Bundle args = new Bundle();
        args.putBoolean("ringtone_picker", true);
        args.putInt("ringtone_type",intent.getIntExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, 0));
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), FRAGMENT_TAG);
    }


    private int checkTheme() {
        int mCurrentTheme = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(FileConstants.CURRENT_THEME, FileConstants.THEME_LIGHT);

        if (mCurrentTheme == FileConstants.THEME_DARK) {
            return R.style.Dark_AppTheme_NoActionBar;
        } else {
            return R.style.AppTheme_NoActionBar;
        }
    }

/*    @Override
    public void onBackPressed() {
        if (dialogFragment.checkIfRootDir())
            super.onBackPressed();
        else
            dialogFragment.reloadData();
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "On activity result");
        finish();
    }
}
