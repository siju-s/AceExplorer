package com.siju.acexplorer.ui.autoplay

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.siju.acexplorer.logging.Logger.log
import java.io.IOException

private const val TAG = "AutoPlayView"

class AutoPlayView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) : SurfaceView(context, attrs, defStyle), SurfaceHolder.Callback {
    private var mediaPlayer: MediaPlayer? = null
    var path: String? = null
        private set

    private var surfaceHolder: SurfaceHolder? = null
    private var callback: PeekPopVideoCallback? = null
    private val completionListener = MediaPlayer.OnCompletionListener { }
    private val preparedListener = MediaPlayer.OnPreparedListener { mp ->
        mp.start()
    }

    private val errorListener = MediaPlayer.OnErrorListener { _, _, _ -> true }

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying == true

    private var isInitialized = false
    private var isSurfaceReady = false

    private var isVideoMode = false

    var isMuted = true
    private set

    private var shouldMute = false
    var isLooping = false

    fun setDataSource(path : String?) {
        this.path = path
    }

    fun setVideoMode(isVideoMode : Boolean) {
        this.isVideoMode = isVideoMode
    }

    private fun startPlayer() {
        if (path.isNullOrEmpty() || isVideoMode && surfaceHolder == null) {
            return
        }
        if (mediaPlayer != null) {
            mediaPlayer?.start()
        } else {
            try {
                initPlayer()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: SecurityException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    private fun initPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.apply {
            setOnCompletionListener(completionListener)
            setOnPreparedListener(preparedListener)
            setOnErrorListener(errorListener)
            isLooping = isLooping
            setDataSource(path)
        }
        mutePlayer()
        if (isVideoMode) {
            mediaPlayer?.setDisplay(surfaceHolder)
        }
        mediaPlayer?.prepareAsync()
    }

    fun playNext() {
        if (path.isNullOrEmpty() || isVideoMode && (surfaceHolder == null || !isSurfaceReady)) {
            return
        }
        if (mediaPlayer == null) {
            try {
                initPlayer()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return
        }
        try {
            mediaPlayer?.isLooping = isLooping
            mediaPlayer?.setDataSource(path)
            mutePlayerOnCondition()
            if (isVideoMode) {
                mediaPlayer?.setDisplay(surfaceHolder)
            }
            mediaPlayer?.prepareAsync()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun init(): Boolean {
        return if (isVideoMode) {
            initSurface()
        } else {
            if (mediaPlayer != null) {
                startPlayer()
                return true
            }
            false
        }
    }

    private fun initSurface(): Boolean {
        if (surfaceHolder != null) {
            this.isInitialized = true
            return true
        }
        surfaceHolder = holder
        if (isSurfaceReady) {
            startPlayer()
            return true
        }
        surfaceHolder?.addCallback(this)
        return false
    }

    override fun onDetachedFromWindow() { // release resources on detach
        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
        super.onDetachedFromWindow()
    }

    fun clearAll() {
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
        isMuted = false
        if (surfaceHolder != null) {
            surfaceHolder?.removeCallback(this)
        }
    }

    fun stopPlayer() {
        Log.d(TAG, "stopPlayer: $mediaPlayer")
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        }
    }

    fun mutePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer?.setVolume(0f, 0f)
            Log.d(TAG, "mutePlayer: muted")
            isMuted = true
            shouldMute = true
        }
    }

    private fun mutePlayerOnCondition() {
        if (mediaPlayer != null && shouldMute) {
            mediaPlayer?.setVolume(0f, 0f)
            Log.d(TAG, "mutePlayer: muted")
            isMuted = true
        }
    }

    fun unmutePlayer() {
        if (mediaPlayer != null) {
            Log.d(TAG, "unmutePlayer: unmuted")
            mediaPlayer?.setVolume(1f, 1f)
            isMuted = false
            shouldMute = false
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        log(TAG, "surfaceCreated: ")
        surfaceHolder = holder
        isSurfaceReady = true
        this.isInitialized = true
        startPlayer()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        log(TAG, "surfaceDestroyed: ")
        isSurfaceReady = false
        surfaceHolder = null
        this.isInitialized = false
        if (callback != null) {
            callback?.onSurfaceDestroyed()
        }
    }

    fun setListener(callback: PeekPopVideoCallback?) {
        this.callback = callback
    }

    interface PeekPopVideoCallback {
        fun onSurfaceDestroyed()
    }
}