package com.siju.acexplorer.storage.model

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.Log
import java.util.*

private const val REFRESH_DELAY_MS = 500L

class MediaObserver(private val handler: Handler) : ContentObserver(handler), Runnable {
    private val listeners = HashSet<MediaObserverListener>()
    private var uri: Uri? = null

    override fun onChange(selfChange: Boolean) {
        this.onChange(selfChange, null)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        Log.e("MediaObserver", "onChange:$uri")
        if (uri == null) {
            return
        }
        handler.removeCallbacks(this)
        this.uri = uri
        handler.postDelayed(this, REFRESH_DELAY_MS)
    }

    fun addMediaObserverListener(mediaObserverListener: MediaObserverListener) {
        listeners.add(mediaObserverListener)
    }

    fun removeMediaObserverListener(listener: MediaObserverListener) {
        listeners.remove(listener)
    }

    override fun run() {
        for (listener in listeners) {
            uri?.let { listener.onMediaChanged(it) }
        }
    }

    interface MediaObserverListener {

        fun onMediaChanged(uri: Uri)
    }
}