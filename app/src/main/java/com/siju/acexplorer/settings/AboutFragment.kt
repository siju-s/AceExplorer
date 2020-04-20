package com.siju.acexplorer.settings

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.siju.acexplorer.R


private const val PREFS_VERSION = "prefsVersion"

class AboutFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_about, rootKey)
        setupVersion()
    }

    private fun setupVersion() {
        val version: Preference? = findPreference(PREFS_VERSION)
        try {
            activity?.let {
                version?.summary = it.packageManager
                        ?.getPackageInfo(it.packageName, 0)?.versionName
            }
        }
        catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        var dialogFragment: DialogFragment? = null
        if (preference is OpenSourceDialogPreference) {
            dialogFragment = OpenSourceDialogFragmentCompat()
            val bundle = Bundle(1)
            bundle.putString("key", preference.getKey())
            dialogFragment.setArguments(bundle)
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            fragmentManager?.let { dialogFragment.show(it, "android.support.v7.preference.PreferenceFragment.DIALOG") }
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}