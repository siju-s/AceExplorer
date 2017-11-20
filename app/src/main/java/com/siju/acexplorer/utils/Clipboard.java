package com.siju.acexplorer.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.siju.acexplorer.R;

import static android.content.Context.CLIPBOARD_SERVICE;

public class Clipboard {

    public static void copyTextToClipBoard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), text);
        clipboard.setPrimaryClip(clip);
    }
}
