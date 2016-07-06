package com.siju.filemanager.settings;


import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.siju.filemanager.R;

import static com.siju.filemanager.settings.SettingsPreferenceFragment.THEME_DARK;
import static com.siju.filemanager.settings.SettingsPreferenceFragment.THEME_LIGHT;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private int mIsTheme = THEME_LIGHT; // Default is Light
    private RelativeLayout mRelativeLayoutSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_holder);
        mRelativeLayoutSettings = (RelativeLayout) findViewById(R.id.relativeLayoutSettings);
        setupActionBar();
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsPreferenceFragment())
                .commit();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root,
                false);
        root.addView(toolbar, 0); // insert at top
        setSupportActionBar(toolbar);


        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            // Show the Up button in the action bar.
    /*        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color
                    .colorPrimary)));*/
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    void setApplicationTheme(int theme) {
        if (mIsTheme != theme) {
            mIsTheme = theme;
            if (theme == THEME_LIGHT) {
                mRelativeLayoutSettings.setBackgroundColor(ContextCompat.getColor(this, R.color
                        .color_light_bg));
            } else if (theme == THEME_DARK) {
                mRelativeLayoutSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_bg));

            }
        }
    }


}
