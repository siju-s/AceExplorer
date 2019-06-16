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

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.siju.acexplorer.AceApplication;
import com.siju.acexplorer.R;
import com.siju.acexplorer.analytics.Analytics;
import com.siju.acexplorer.logging.Logger;
import com.siju.acexplorer.main.model.FileConstants;
import com.siju.acexplorer.main.model.helper.FileUtils;
import com.siju.acexplorer.theme.ThemeUtils;
import com.siju.acexplorer.utils.LocaleHelper;

import static com.siju.acexplorer.theme.ThemeUtils.CURRENT_THEME;
import static com.siju.acexplorer.theme.ThemeUtils.PREFS_THEME;


public class SettingsPreferenceFragment extends PreferenceFragmentCompat  {

    private final String TAG = this.getClass().getSimpleName();
    public static final String PREFS_LANGUAGE = "prefLanguage";
    public static final String PREFS_ANALYTICS = "prefsAnalytics";

    private SharedPreferences preferences;
    private Preference updatePreference;
    private String currentLanguage;
    private int theme;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_settings, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ListPreference langPreference = findPreference(PREFS_LANGUAGE);
        ListPreference themePreference = findPreference(PREFS_THEME);
        theme = ThemeUtils.getTheme(getActivity());
        String PREFS_UPDATE = "prefsUpdate";
        updatePreference = findPreference(PREFS_UPDATE);


        CheckBoxPreference hiddenPreference = findPreference("prefHidden");
        hiddenPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return true;
            }
        });


        CheckBoxPreference analyticsPreference = findPreference(PREFS_ANALYTICS);
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
                Toast.makeText(AceApplication.Companion.getAppContext(), getString(R.string.msg_fav_reset), Toast
                        .LENGTH_LONG).show();
                getActivity().setResult(AppCompatActivity.RESULT_OK, data);
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
                            if (!stringValue.equals(currentLanguage)) {
                                LocaleHelper.persist(getActivity(), stringValue);
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
        AppCompatActivity activity = (AppCompatActivity) getActivity();
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


}