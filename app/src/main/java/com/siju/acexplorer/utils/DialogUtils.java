package com.siju.acexplorer.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.siju.acexplorer.R;

import java.util.ArrayList;

/**
 * Created by Siju on 21-08-2016.
 */
public class DialogUtils {

    public MaterialDialog showEditDialog(final Context context, String[] texts) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.input(texts[0], texts[1], false, new
                MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {

                    }
                });
        builder.widgetColor(ContextCompat.getColor(context, R.color.colorAccent));
  /*      if(m.theme1==1)
            a.theme(Theme.DARK);*/
        builder.title(texts[2]);
        builder.positiveText(texts[3]);
        builder.positiveColor(ContextCompat.getColor(context, R.color.colorPrimary));
        builder.neutralText(texts[4]);
        if (texts[5] != (null)) {
            builder.negativeText(texts[5]);
            builder.negativeColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
        return builder.build();
    }


    public MaterialDialog showCustomDialog(final Context context, int resourceId, String[] texts) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.customView(resourceId, true);
 //        builder.widgetColor(ContextCompat.getColor(context,R.color.colorAccent));
  /*      if(m.theme1==1)
            a.theme(Theme.DARK);*/
        builder.title(texts[0]);
        builder.positiveText(texts[1]);
        builder.positiveColor(ContextCompat.getColor(context, R.color.colorPrimary));
        builder.neutralText(texts[2]);
        builder.neutralColor(ContextCompat.getColor(context, R.color.colorPrimary));
        if (texts[3] != (null)) {
            builder.negativeText(texts[3]);
            builder.negativeColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
        return builder.build();
    }

    public MaterialDialog showListDialog(final Context context, String[] texts, ArrayList<String> items) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
  /*      if(m.theme1==1)
            a.theme(Theme.DARK);*/
        builder.title(texts[0]);
        builder.positiveText(texts[1]);
        builder.positiveColor(ContextCompat.getColor(context, R.color.colorPrimary));
        builder.neutralText(texts[2]);
        builder.items(items);
        if (texts[3] != (null)) {
            builder.negativeText(texts[3]);
            builder.negativeColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
        return builder.build();
    }

    public MaterialDialog showRadioListDialog(final Context context, String[] texts, ArrayList<String> items) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
  /*      if(m.theme1==1)
            a.theme(Theme.DARK);*/
        builder.title(texts[0]);
        builder.positiveText(texts[1]);
        builder.positiveColor(ContextCompat.getColor(context, R.color.colorPrimary));
        builder.neutralText(texts[2]);
        builder.items(items);
        if (texts[3] != (null)) {
            builder.negativeText(texts[3]);
            builder.negativeColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
        return builder.build();
    }




}
