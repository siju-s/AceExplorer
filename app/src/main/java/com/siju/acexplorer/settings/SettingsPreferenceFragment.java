package com.siju.acexplorer.settings;

/**
 * Created by SIJU on 06-07-2016.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.preference.PreferenceScreen;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.siju.acexplorer.R;
import com.siju.acexplorer.common.Logger;
import com.siju.acexplorer.filesystem.FileConstants;

import java.util.Locale;


public class SettingsPreferenceFragment extends PreferenceFragment {

    private final String TAG = this.getClass().getSimpleName();
    private final String PREFS_PRO = "prefsPro";
    private final String PREFS_UPDATE = "prefsUpdate";
    public static final String PREFS_LANGUAGE = "prefLanguage";

    private Locale myLocale;
    private String currentLanguage;
    //    ListPreference langPreference;
    ListPreference themePreference;
    private SharedPreferences mPrefs;
    Preference updatePreference;
    private Intent mSendIntent;
    private final String PREFS_VERSION = "prefsVersion";
    private int mIsTheme; // Default is Light


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        setHasOptionsMenu(true);
        mSendIntent = new Intent();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

//        langPreference = (ListPreference) findPreference(PREFS_LANGUAGE);
        themePreference = (ListPreference) findPreference(FileConstants.PREFS_THEME);
        mIsTheme = mPrefs.getInt(FileConstants.CURRENT_THEME, FileConstants.THEME_LIGHT);

        updatePreference = findPreference(PREFS_UPDATE);


        Preference version = findPreference(PREFS_VERSION);
        try {
            version.setSummary(getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


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

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);
        Log.d(this.getClass().getSimpleName(), "On prefernce tree-" + preference);

        // If the user has clicked on a preference screen, set up the screen
        if (preference instanceof PreferenceScreen) {
            setupActionBar((PreferenceScreen) preference);

        }

        return false;
    }

    public void setupActionBar(PreferenceScreen preferenceScreen) {
        final Dialog dialog = preferenceScreen.getDialog();
        AppBarLayout bar;
        ViewParent view1 = dialog.findViewById(android.R.id.list).getParent();
        ViewParent view2 = view1.getParent();
        Log.d(this.getClass().getSimpleName(), "On prefernce tree-" + view1 + " view2=" + view2+ " view3="+view2.getParent());
        LinearLayout root;
        if (Build.VERSION.SDK_INT >= 24) {
            root = (LinearLayout) dialog.findViewById(android.R.id.list).getParent().getParent();
        }
        else {
            root = (LinearLayout) dialog.findViewById(android.R.id.list).getParent();
        }
        bar = (AppBarLayout) LayoutInflater.from(getActivity()).inflate(R.layout.toolbar, root,
                false);
        root.addView(bar, 0);
        Toolbar toolbar = (Toolbar) bar.getChildAt(0);
        toolbar.setTitle(preferenceScreen.getTitle());


        if (mIsTheme == FileConstants.THEME_DARK) {
            toolbar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_colorPrimary));

            if (Build.VERSION.SDK_INT >= 21) {
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color
                        .dark_colorPrimaryDark));

            }
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
                    Log.d("TAG", "On prefs chnage");


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
                            mPrefs.edit().putInt(FileConstants.CURRENT_THEME, theme).apply();
                            Logger.log("TAG", "Current theme=" + mIsTheme + " new theme=" + theme);
                            if (mIsTheme != theme) {
                                getActivity().setResult(Activity.RESULT_OK, mSendIntent);
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
        if (activity == null)
            return;
        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.finish();
        activity.overridePendingTransition(enter_anim, exit_anim);
        /*final Intent intent = getActivity().getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);*/
        activity.startActivity(activity.getIntent());

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
                            Logger.log(TAG, "Homescreen=" + isHomeScreenEnabled);
//                            mSendIntent.putExtra(FileConstants.PREFS_HOMESCREEN, isHomeScreenEnabled);
                            getActivity().setResult(Activity.RESULT_OK, mSendIntent);
                            break;

                        case FileConstants.PREFS_DUAL_PANE:
                            boolean isDualPaneEnabledSettings = mPrefs.getBoolean(FileConstants
                                    .PREFS_DUAL_PANE, true);
                            Logger.log(TAG, "Dualpane=" + isDualPaneEnabledSettings);
//                            mSendIntent.putExtra(FileConstants.PREFS_DUAL_PANE, isDualPaneEnabledSettings);
                            getActivity().setResult(Activity.RESULT_OK, mSendIntent);
                            break;
                    }
                }
            };

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */


}