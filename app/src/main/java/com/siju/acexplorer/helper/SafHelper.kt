package com.siju.acexplorer.helper

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.siju.acexplorer.R
import com.siju.acexplorer.main.model.FileConstants

object SafHelper {

    fun triggerStorageAccessFramework(activity : AppCompatActivity?, requestId : Int) {
        activity ?: return
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        if (activity.packageManager?.resolveActivity(intent, 0) != null) {
            activity.startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
                    requestId)
        }
        else {
            val context = activity.baseContext
            Toast.makeText(context, context.getString(R.string.msg_error_not_supported),
                    Toast.LENGTH_LONG).show()
        }
    }

    fun saveSafUri(sharedPreferences: SharedPreferences, uri: Uri) {
        sharedPreferences.edit().putString(FileConstants.SAF_URI, uri.toString()).apply()
    }

    fun persistUriPermission(context : Context, uri: Uri?) {
        uri ?: return
        val newFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent
                .FLAG_GRANT_WRITE_URI_PERMISSION
        // Persist URI - this is required for verification of writability.
        context.contentResolver.takePersistableUriPermission(uri, newFlags)
    }
}