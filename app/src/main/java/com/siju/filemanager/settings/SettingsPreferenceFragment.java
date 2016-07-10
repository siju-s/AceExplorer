package com.siju.filemanager.settings;

/**
 * Created by SIJU on 06-07-2016.
 */

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;

import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.MenuItem;

import com.siju.filemanager.R;
import com.siju.filemanager.common.Logger;
import com.siju.filemanager.utils.LocaleHelper;

import java.util.Locale;

import static com.siju.filemanager.filesystem.FileConstants.PREFS_THEME;
import static com.siju.filemanager.utils.LocaleHelper.getLanguage;


public class SettingsPreferenceFragment extends PreferenceFragment {


    private final String PREFS_PRO = "prefsPro";
    private final String PREFS_VERSION = "prefsVersion";
    private final String PREFS_UPDATE = "prefsUpdate";
    public static final String PREFS_LANGUAGE = "prefLanguage";

    private Locale myLocale;
    private String currentLanguage;
    //    ListPreference langPreference;
    ListPreference themePreference;

    Preference updatePreference;
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_settings, false);
        setHasOptionsMenu(true);

//        langPreference = (ListPreference) findPreference(PREFS_LANGUAGE);
        themePreference = (ListPreference) findPreference(PREFS_THEME);

        updatePreference = findPreference(PREFS_UPDATE);

        Preference version = findPreference(PREFS_VERSION);

        CheckBoxPreference preference = (CheckBoxPreference)findPreference("prefDualPane");
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String value = o.toString();
                Logger.log("Settings","Dualpane="+value);
                return true;
            }
        });

        try {
            version.setSummary(getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
//        String value = LocaleHelper.getLanguage(getActivity());
//        langPreference.setValue(value);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
//        bindPreferenceSummaryToValue(langPreference);
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
                                .parse("market://details?id=com.siju.musicalmojo.free"));
    /*                        if (!MyStartActivity(intent)) {
                                // Market (Google play) app seems not installed,
                                // let's try to open a webbrowser
                                intent.setData(Uri
                                        .parse("https://play.google.com/store/apps/details?id=com.siju.musicalmojo.free"));
                                if (!MyStartActivity(intent)) {
                                    // Well if this also fails, we have run out of
                                    // options, inform the user.
                                    Toast.makeText(getActivity(),
                                            getString(R.string.errorMarket),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }*/
                        return true;
                    }
                });
    }


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
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

         /*       if (listPreference.getKey().equals(PREFS_LANGUAGE)) {

                    LocaleHelper.setLocale(getActivity(), stringValue);
                    getActivity().recreate();
                }*/
                if (listPreference.getKey().equals(PREFS_THEME)) {
                    int theme = Integer.valueOf(stringValue);

                    ((SettingsActivity) getActivity()).setApplicationTheme(theme);
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

    /**
     * Set language
     *
     * @param lang
     */
    public void setLocale(String lang) {

        myLocale = new Locale(lang);

        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

 /*       SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PRE, lang);
        editor.apply();*/
    }

    private boolean MyStartActivity(Intent aIntent) {
        try {
            startActivity(aIntent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}