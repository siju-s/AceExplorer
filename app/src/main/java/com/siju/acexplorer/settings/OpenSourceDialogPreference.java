package com.siju.acexplorer.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.siju.acexplorer.R;

/**
 * Created by Siju on 04-09-2016.
 */
public class OpenSourceDialogPreference extends DialogPreference {

    public OpenSourceDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("SIJU", "DialogFrag");
        setDialogLayoutResource(R.layout.open_source_licenses);
    }

   /* @Override
    protected View onCreateDialogView() {
        Log.d("SIJU", "DialogFrag");
        return super.onCreateDialogView();
    }*/

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
//        builder.setTitle(getContext().getString(R.string.open_source_license));
        builder.setPositiveButton(getContext().getString(R.string.msg_ok), null);
        builder.setNegativeButton(null, null);
        super.onPrepareDialogBuilder(builder);
    }

    @Override
    protected void onBindDialogView(View view) {
        Log.d("SIJU", "onBindDialogView");
        WebView webView = (WebView) view.findViewById(R.id.webViewLicense);
        webView.loadUrl("file:///android_asset/open_source_licenses.html");
        super.onBindDialogView(view);
    }
}
