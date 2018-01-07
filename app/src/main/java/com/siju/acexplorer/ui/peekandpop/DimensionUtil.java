package com.siju.acexplorer.ui.peekandpop;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.TypedValue;

/**
 * Created by Vincent on 30/01/2016.
 */
public class DimensionUtil {

    public static int convertDpToPx(@NonNull Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }


}
