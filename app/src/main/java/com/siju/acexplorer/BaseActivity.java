package com.siju.acexplorer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.siju.acexplorer.filesystem.theme.ThemeUtils;
import com.siju.acexplorer.filesystem.theme.Themes;

/**
 * Created by SJ on 18-01-2017.
 */

public class BaseActivity extends AppCompatActivity {

    private Themes currentTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
    }

    private void setTheme() {

        currentTheme = Themes.getTheme(ThemeUtils.getTheme(this));
        switch (currentTheme) {
            case DARK:
                setTheme(R.style.DarkAppTheme_NoActionBar);
                break;
            case LIGHT:
                setTheme(R.style.AppTheme_NoActionBar);
                break;

        }
    }

    public Themes getCurrentTheme() {
        return currentTheme;
    }

}
