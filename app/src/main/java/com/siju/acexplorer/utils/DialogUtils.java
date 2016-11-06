package com.siju.acexplorer.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import com.afollestad.materialdialogs.MaterialDialog;
import com.siju.acexplorer.R;

import java.util.ArrayList;

public class DialogUtils {

    public MaterialDialog showEditDialog(final Context context, String[] texts) {
        int color = getCurrentThemePrimary(context);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.input(texts[0], texts[1], false, new
                MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog materialDialog, CharSequence charSequence) {

                    }
                });
        builder.widgetColor(ContextCompat.getColor(context, R.color.colorAccent));
  /*      if(m.theme1==1)
            a.theme(Theme.DARK);*/
        builder.title(texts[2]);
        builder.positiveText(texts[3]);
        builder.positiveColor(color);
        builder.neutralText(texts[4]);
        if (texts[5] != (null)) {
            builder.negativeText(texts[5]);
            builder.negativeColor(color);
        }
        return builder.build();
    }


    public MaterialDialog showCustomDialog(final Context context, int resourceId, String[] texts) {
        int color = getCurrentThemePrimary(context);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);

        builder.customView(resourceId, true);

        //        builder.widgetColor(ContextCompat.getColor(context,R.color.colorAccent));
  /*      if(m.theme1==1)
            a.theme(Theme.DARK);*/
        builder.title(texts[0]);
        builder.positiveText(texts[1]);
        builder.positiveColor(color);
        builder.neutralText(texts[2]);
        builder.neutralColor(color);
        if (texts[3] != (null)) {
            builder.negativeText(texts[3]);
            builder.negativeColor(color);
        }
        return builder.build();
    }






    public int getCurrentThemePrimary(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.titleTextColor, typedValue, true);
        return typedValue.data;
    }

    public MaterialDialog showListDialog(final Context context, String[] texts, ArrayList<String> items) {
        int color = getCurrentThemePrimary(context);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
  /*      if(m.theme1==1)
            a.theme(Theme.DARK);*/
        builder.title(texts[0]);
        builder.positiveText(texts[1]);
        builder.positiveColor(color);
//        builder.positiveColor(ContextCompat.getColor(context, R.color.colorPrimary));
        builder.neutralText(texts[2]);
        builder.items(items);
        if (texts[3] != (null)) {
            builder.negativeText(texts[3]);
            builder.negativeColor(color);
        }
        return builder.build();
    }

    public MaterialDialog showDialog(final Context context, String[] texts) {
        int color = getCurrentThemePrimary(context);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
  /*      if(m.theme1==1)
            a.theme(Theme.DARK);*/
        builder.title(texts[0]);
        builder.positiveText(texts[1]);
        builder.positiveColor(color);
        builder.neutralText(texts[2]);
        builder.neutralColor(color);
        if (texts[3] != (null)) {
            builder.negativeText(texts[3]);
            builder.negativeColor(color);
        }
        builder.content(texts[4]);

        return builder.build();
    }




}
