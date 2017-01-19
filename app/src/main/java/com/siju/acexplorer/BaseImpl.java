package com.siju.acexplorer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.siju.acexplorer.filesystem.FileConstants;
import com.siju.acexplorer.filesystem.utils.ThemeUtils;

/**
 * Created by SJ on 18-01-2017.
 */

public class BaseImpl extends AppCompatActivity {

    private int mCurrentTheme = FileConstants.THEME_LIGHT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
    }

    private void setTheme() {
        mCurrentTheme = ThemeUtils.getTheme(this);

        if (mCurrentTheme == FileConstants.THEME_DARK) {
            setTheme(R.style.DarkAppTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }

    }

}
