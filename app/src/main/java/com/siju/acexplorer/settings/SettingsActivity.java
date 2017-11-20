/*
 * Copyright (C) 2017 Ace Explorer owned by Siju Sakaria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.siju.acexplorer.settings;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.siju.acexplorer.R;
import com.siju.acexplorer.theme.ThemeUtils;
import com.siju.acexplorer.utils.LocaleHelper;
import com.siju.acexplorer.view.AceActivity;

import static com.siju.acexplorer.theme.ThemeUtils.THEME_DARK;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLanguage(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_holder);
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
        setupActionBar();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsPreferenceFragment())
                .commit();

    }

    private void checkTheme() {
        int theme = ThemeUtils.getTheme(this);

        if (theme == THEME_DARK) {
            setTheme(R.style.BaseDarkTheme_Settings);
        }
        else {
            setTheme(R.style.BaseLightTheme_Settings);
        }
    }

    @Override
    public void onBackPressed() {
        Intent in = new Intent(this, AceActivity.class);
        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        Activity activity = this;
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.finish();
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.startActivity(in);
    }


    private void setupActionBar() {
        ViewGroup rootView = findViewById(R.id.action_bar_root);
        AppBarLayout bar;
        if (rootView != null) {
            bar = (AppBarLayout) LayoutInflater.from(this).inflate(R.layout.toolbar_settings,
                    rootView,
                    false);
            rootView.addView(bar, 0); // insert at top
            Toolbar toolbar = (Toolbar) bar.getChildAt(0);
            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color
                        .colorPrimaryDark));
            }

            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();

            if (actionBar != null) {
                // Show the Up button in the action bar.
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
