package com.siju.acexplorer.main.model

import android.content.Context
import androidx.exifinterface.media.ExifInterface
import com.siju.acexplorer.R
import java.util.*

data class ExifData(val context : Context, val tag : String, var value : String) {

    init {
        if (tag == ExifInterface.TAG_ORIENTATION) {
            value = when(value.toInt()) {
                ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_UNDEFINED -> String.format(Locale.ENGLISH, context.getString(R.string.degrees), 0)
                ExifInterface.ORIENTATION_ROTATE_90                  -> String.format(Locale.ENGLISH, context.getString(R.string.degrees), 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> String.format(Locale.ENGLISH, context.getString(R.string.degrees), 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> String.format(Locale.ENGLISH, context.getString(R.string.degrees), 270)
                else -> value
            }
        }
        else if (tag == ExifInterface.TAG_FOCAL_LENGTH) {
            val result = value.split("/")
            if (result.size == 2) {
                value = String.format(Locale.ENGLISH, context.getString(R.string.mm), result[0].toInt()/1000f)
            }
        }
        else if (tag == ExifInterface.TAG_EXPOSURE_TIME) {
            value = String.format(Locale.ENGLISH, context.getString(R.string.exposure_sec), value)
        }
    }
}
