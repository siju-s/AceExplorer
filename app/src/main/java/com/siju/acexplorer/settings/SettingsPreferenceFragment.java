package com.siju.acexplorer.settings;

/**
 * Created by SIJU on 06-07-2016.
 */

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;

import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.Toast;

import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.FileConstants;

import java.util.Locale;


public class SettingsPreferenceFragment extends PreferenceFragment {

    private final String TAG = this.getClass().getSimpleName();
    private final String PREFS_PRO = "prefsPro";
    private final String PREFS_VERSION = "prefsVersion";
    private final String PREFS_UPDATE = "prefsUpdate";
    public static final String PREFS_LANGUAGE = "prefLanguage";

    private Locale myLocale;
    private String currentLanguage;
    //    ListPreference langPreference;
    ListPreference themePreference;
    private SharedPreferences mPrefs;
    Preference updatePreference;
    private Intent mSendIntent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_settings, false);
        setHasOptionsMenu(true);
        mSendIntent = new Intent();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

//        langPreference = (ListPreference) findPreference(PREFS_LANGUAGE);
        themePreference = (ListPreference) findPreference(FileConstants.PREFS_THEME);

        updatePreference = findPreference(PREFS_UPDATE);

        Preference version = findPreference(PREFS_VERSION);

        CheckBoxPreference preference = (CheckBoxPreference) findPreference("prefDualPane");
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String value = o.toString();
                Logger.log("Settings", "Dualpane=" + value);
                return true;
            }
        });

        Preference resetPreference = findPreference(FileConstants.PREFS_RESET);
        resetPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent data = new Intent();
                data.putExtra(FileConstants.PREFS_RESET, true);
                Toast.makeText(getActivity(), getString(R.string.msg_fav_reset), Toast
                        .LENGTH_LONG).show();
                getActivity().setResult(Activity.RESULT_OK, data);
                return false;
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

    @Override
    public void onResume() {
        super.onResume();
        mPrefs.registerOnSharedPreferenceChangeListener(mListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPrefs.unregisterOnSharedPreferenceChangeListener(mListener);

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
                        return true;
                    }
                });
    }


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new
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

         /*       if (listPreference.getKey().equals(PREFS_LANGUAGE)) {

                    LocaleHelper.setLocale(getActivity(), stringValue);
                    getActivity().recreate();
                }*/
                        if (listPreference.getKey().equals(FileConstants.PREFS_THEME)) {
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



    SharedPreferences.OnSharedPreferenceChangeListener mListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    switch (key) {
                        case FileConstants.PREFS_HOMESCREEN:
                            boolean isHomeScreenEnabled = prefs.getBoolean(FileConstants
                                    .PREFS_HOMESCREEN, true);
                            Logger.log(TAG,"Homescreen="+isHomeScreenEnabled);
//                            mSendIntent.putExtra(FileConstants.PREFS_HOMESCREEN, isHomeScreenEnabled);
                            getActivity().setResult(Activity.RESULT_OK, mSendIntent);
                            break;

                        case FileConstants.PREFS_DUAL_PANE:
                            boolean isDualPaneEnabledSettings = mPrefs.getBoolean(FileConstants
                                    .PREFS_DUAL_PANE, true);
                            Logger.log(TAG,"Dualpane="+isDualPaneEnabledSettings);
//                            mSendIntent.putExtra(FileConstants.PREFS_DUAL_PANE, isDualPaneEnabledSettings);
                            getActivity().setResult(Activity.RESULT_OK, mSendIntent);
                            break;
                    }
                }
            };
}