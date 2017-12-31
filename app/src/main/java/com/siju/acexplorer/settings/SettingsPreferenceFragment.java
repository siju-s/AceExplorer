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
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.siju.acexplorer.R;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.model.FileConstants;
import com.siju.acexplorer.model.helper.FileUtils;
import com.siju.acexplorer.theme.ThemeUtils;
import com.siju.acexplorer.utils.LocaleHelper;
import com.siju.acexplorer.view.AceActivity;

import static com.siju.acexplorer.theme.ThemeUtils.CURRENT_THEME;
import static com.siju.acexplorer.theme.ThemeUtils.PREFS_THEME;


public class SettingsPreferenceFragment extends PreferenceFragment {

    private final       String TAG             = this.getClass().getSimpleName();
    public static final String PREFS_LANGUAGE  = "prefLanguage";
    public static final String PREFS_ANALYTICS = "prefsAnalytics";

    private String            currentLanguage;
    private SharedPreferences preferences;
    private Preference        updatePreference;
    private int               theme;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        setHasOptionsMenu(true);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ListPreference langPreference = (ListPreference) findPreference(PREFS_LANGUAGE);
        ListPreference themePreference = (ListPreference) findPreference(PREFS_THEME);
        theme = ThemeUtils.getTheme(getActivity());
        String PREFS_UPDATE = "prefsUpdate";
        updatePreference = findPreference(PREFS_UPDATE);

        String PREFS_VERSION = "prefsVersion";
        Preference version = findPreference(PREFS_VERSION);
        try {
            version.setSummary(getActivity().getPackageManager()
                                       .getPackageInfo(getActivity().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        CheckBoxPreference hiddenPreference = (CheckBoxPreference) findPreference("prefHidden");
        hiddenPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getActivity().setResult(Activity.RESULT_OK, null);
                return true;
            }
        });


        CheckBoxPreference analyticsPreference = (CheckBoxPreference) findPreference(PREFS_ANALYTICS);
        analyticsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Analytics.getLogger().sendAnalytics((Boolean) newValue);
                return true;
            }
        });


        Preference resetPreference = findPreference(FileConstants.PREFS_RESET);
        resetPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent data = new Intent();
                data.putExtra(FileConstants.PREFS_RESET, true);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.msg_fav_reset), Toast
                        .LENGTH_LONG).show();
                getActivity().setResult(Activity.RESULT_OK, data);
                return false;
            }
        });


        String value = LocaleHelper.getLanguage(getActivity());
        langPreference.setValue(value);
        currentLanguage = value;
        Logger.log("Settings", "lang=" + currentLanguage);


        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        bindPreferenceSummaryToValue(langPreference);
        bindPreferenceSummaryToValue(themePreference);
        initializeListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        preferences.registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(mListener);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        // If the user has clicked on a preference screen, set up the screen
        if (preference instanceof PreferenceScreen) {
            Analytics.getLogger().aboutDisplayed();
            setupActionBar((PreferenceScreen) preference);
        }

        return false;
    }

    private void setupActionBar(PreferenceScreen preferenceScreen) {
        final Dialog dialog = preferenceScreen.getDialog();
        AppBarLayout bar;
        ViewParent view1 = dialog.findViewById(android.R.id.list).getParent();
        ViewParent view2 = view1.getParent();
        LinearLayout root;
        Log.d(this.getClass().getSimpleName(), "On prefernce tree-" + view1 + " view2=" + view2 + " view3=" +
                view2.getParent());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            root = (LinearLayout) view2;
        } else {
            root = (LinearLayout) view1;
        }
        bar = (AppBarLayout) LayoutInflater.from(getActivity()).inflate(R.layout.toolbar_settings, root,

                                                                        false);
        root.addView(bar, 0);
        Toolbar toolbar = (Toolbar) bar.getChildAt(0);
        toolbar.setTitle(preferenceScreen.getTitle());
        toolbar.setPadding(0, ((SettingsActivity) getActivity()).getStatusBarHeight(), 0, 0);


        toolbar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));

        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(),
                                                                               R.color
                                                                                       .colorPrimaryDark));

        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    private void initializeListeners() {
        updatePreference
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        // Try Google play
                        intent.setData(Uri
                                               .parse("market://details?id=" + getActivity().getPackageName()));

                        if (FileUtils.isPackageIntentUnavailable(getActivity(), intent)) {
                            // Market (Google play) app seems not installed,
                            // let's try to open a webbrowser
                            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" +
                                                             getActivity().getPackageName()));
                            if (FileUtils.isPackageIntentUnavailable(getActivity(), intent)) {
                                Toast.makeText(getActivity(),
                                               getString(R.string.msg_error_not_supported),
                                               Toast.LENGTH_SHORT).show();
                            } else {
                                startActivity(intent);
                            }
                        } else {
                            startActivity(intent);
                        }

                        return true;
                    }
                });
    }


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new
            Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    String stringValue = value.toString();
                    Log.d("Settings", "On prefs chnage:"+preference);


                    if (preference instanceof ListPreference) {
                        // For list preferences, look up the correct display value in
                        // the preference's 'entries' list.
                        ListPreference listPreference = (ListPreference) preference;
                        int index = listPreference.findIndexOfValue(stringValue);

                        // Set the summary to reflect the new value.
                        preference.setSummary(
                                index >= 0
                                        ? listPreference.getEntries()[index]
                                        : null);

                        if (listPreference.getKey().equals(PREFS_LANGUAGE)) {
                            Logger.log("Settings", "sBindPreferenceSummaryToValueListener -lang=" + stringValue);

                            if (!stringValue.equals(currentLanguage)) {
                                LocaleHelper.setLocale(getActivity(), stringValue);
                                restartApp();
                            }
                        }

                        if (listPreference.getKey().equals(PREFS_THEME)) {
                            int theme = Integer.parseInt(stringValue);
                            preferences.edit().putInt(CURRENT_THEME, theme).apply();
                            Logger.log("TAG", "Current theme=" + SettingsPreferenceFragment.this
                                    .theme + " new theme=" + theme);
                            if (SettingsPreferenceFragment.this.theme != theme) {
                                restartApp();
                            }

//                            ((SettingsActivity) getActivity()).setApplicationTheme(theme);
                        }

                    } else {
                        // For all other preferences, set the summary to the value's
                        // simple string representation.
                        preference.setSummary(stringValue);
                    }
                    return true;
                }
            };


    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                                                                 PreferenceManager
                                                                         .getDefaultSharedPreferences(preference.getContext())
                                                                         .getString(preference.getKey(), ""));
    }


    private void restartApp() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.finish();
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.startActivity(activity.getIntent());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent in = new Intent(getActivity(), AceActivity.class);
            final int enter_anim = android.R.anim.fade_in;
            final int exit_anim = android.R.anim.fade_out;
            Activity activity = getActivity();
            activity.overridePendingTransition(enter_anim, exit_anim);
            activity.finish();
            activity.overridePendingTransition(enter_anim, exit_anim);
            activity.startActivity(in);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private final SharedPreferences.OnSharedPreferenceChangeListener mListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    switch (key) {
                        case FileConstants.PREFS_HOMESCREEN:
                            boolean isHomeScreenEnabled = prefs.getBoolean(FileConstants
                                                                                   .PREFS_HOMESCREEN, true);
                            Logger.log(TAG, "Homescreen=" + isHomeScreenEnabled);
//                            mSendIntent.putExtra(FileConstants.PREFS_HOMESCREEN, isHomeScreenEnabled);
                            break;

                        case FileConstants.PREFS_DUAL_PANE:
                            boolean isDualPaneEnabledSettings = preferences.getBoolean(FileConstants
                                                                                          .PREFS_DUAL_PANE, true);
                            Logger.log(TAG, "Dualpane=" + isDualPaneEnabledSettings);
//                            mSendIntent.putExtra(FileConstants.PREFS_DUAL_PANE, isDualPaneEnabledSettings);
                            break;
                    }
                }
            };

}