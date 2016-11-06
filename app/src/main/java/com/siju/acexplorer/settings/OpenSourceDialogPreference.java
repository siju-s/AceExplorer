package com.siju.acexplorer.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

import com.siju.acexplorer.R;

public class OpenSourceDialogPreference extends DialogPreference {

    public OpenSourceDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.open_source_licenses);
    }


    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setPositiveButton(getContext().getString(R.string.msg_ok), null);
        builder.setNegativeButton(null, null);
        super.onPrepareDialogBuilder(builder);
    }

    @Override
    protected void onBindDialogView(View view) {
        WebView webView = (WebView) view.findViewById(R.id.webViewLicense);
        webView.loadUrl("file:///android_asset/open_source_licenses.html");
        super.onBindDialogView(view);
    }
}
