package com.siju.acexplorer.settings

import android.view.View
import android.webkit.WebView
import androidx.preference.PreferenceDialogFragmentCompat
import com.siju.acexplorer.R

class OpenSourceDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    override fun onDialogClosed(positiveResult: Boolean) {

    }

    override fun onBindDialogView(view: View?) {
        val webView : WebView? = view?.findViewById(R.id.webViewLicense)
        webView?.loadUrl("file:///android_asset/open_source_licenses.html")
        super.onBindDialogView(view)
    }
}