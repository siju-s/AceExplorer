package com.siju.acexplorer.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.siju.acexplorer.R


private const val PREFS_VERSION = "prefsVersion"

class AboutFragment : PreferenceFragmentCompat() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root: View? = super.onCreateView(inflater, container, savedInstanceState)
        val activity = activity as AppCompatActivity?
        activity?.setSupportActionBar(root?.findViewById(R.id.toolbar) as Toolbar)
        val actionBar: ActionBar? = activity?.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setTitle(R.string.pref_title_about)
        setHasOptionsMenu(true)
        return root
    }

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
            parentFragmentManager.let { dialogFragment.show(it, "android.support.v7.preference.PreferenceFragment.DIALOG") }
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            findNavController().popBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}