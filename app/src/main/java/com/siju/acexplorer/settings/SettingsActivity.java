package com.siju.acexplorer.settings;


import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.siju.acexplorer.R;
import com.siju.acexplorer.filesystem.FileConstants;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private int mIsTheme = FileConstants.THEME_LIGHT; // Default is Light
    private RelativeLayout mRelativeLayoutSettings;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_holder);
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
        mRelativeLayoutSettings = (RelativeLayout) findViewById(R.id.relativeLayoutSettings);
        setupActionBar();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsPreferenceFragment())
                .commit();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private void checkTheme() {
      int theme = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(FileConstants.CURRENT_THEME, FileConstants.THEME_LIGHT);

//        int  stringValue = PreferenceManager.getDefaultSharedPreferences(this).getInt(FileConstants.PREFS_THEME, 0);

            if (theme == FileConstants.THEME_DARK) {
                setTheme(R.style.Settings_BlackTheme);
                mIsTheme = FileConstants.THEME_DARK;
            }


    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    public void setupActionBar() {
        AppBarLayout bar;
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        bar = (AppBarLayout) LayoutInflater.from(this).inflate(R.layout.toolbar, root,
                false);
        root.addView(bar, 0); // insert at top
        mToolbar = (Toolbar) bar.getChildAt(0);

        if (mIsTheme == FileConstants.THEME_DARK) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(this,R.color.dark_colorPrimary));

            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_colorPrimaryDark));

            }
        }

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            // Show the Up button in the action bar.
            //*        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color
//                    .colorPrimary)));*//*
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    void setApplicationTheme(int theme) {
        if (mIsTheme != theme) {
            mIsTheme = theme;
            if (theme == FileConstants.THEME_LIGHT) {
        /*        getListView().setBackgroundColor(ContextCompat.getColor(this, R.color
                        .color_light_bg));*/
                mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
                mRelativeLayoutSettings.setBackgroundColor(ContextCompat.getColor(this, R.color
                        .color_light_bg));
                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.color_light_status_bar));

                }
            } else if (theme == FileConstants.THEME_DARK) {
         /*       getListView().setBackgroundColor(ContextCompat.getColor(this, R.color
                        .color_dark_bg));*/
                mToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_bg));
                mRelativeLayoutSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_bg));
                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.color_dark_status_bar));

                }
            }
        }
    }

/*
    private void setApplicationTheme(boolean themeLight) {
        if (themeLight) {
            this.mToolbar.setBackgroundColor(getResources().getColor(R.color.color_lt_background));
            this.mRelativeLayoutSettings.setBackgroundColor(getResources().getColor(R.color.color_lt_background_motiv));
            if (VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.color_lt_status_bar));
                return;
            }
            return;
        }
        this.mToolbar.setBackgroundColor(getResources().getColor(R.color.color_dk_background));
        this.mRelativeLayoutSettings.setBackgroundColor(getResources().getColor(R.color.color_dk_background_motiv));
        if (VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.color_dk_status_bar));
        }
    }
*/

/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
           finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/


}
