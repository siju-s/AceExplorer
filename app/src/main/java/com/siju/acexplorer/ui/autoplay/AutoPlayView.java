package com.siju.acexplorer.ui.autoplay;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.siju.acexplorer.logging.Logger;

import java.io.IOException;


@SuppressWarnings({"SameParameterValue", "unused"})
public class AutoPlayView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "AutoPlayView";
    private MediaPlayer mediaPlayer;
    private String path;
    private boolean isLooping;
    private SurfaceHolder surfaceHolder;
    private boolean isSurfaceReady;
    private boolean videoMode;
    private boolean isMuted = true;
    private boolean shouldMute;
    private PeekPopVideoCallback callback;

    private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
        }
    };
    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.d(TAG, "onPrepared: ");
            mp.start();
        }
    };
    private MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return true;
        }
    };

    public AutoPlayView(Context context) {
        this(context, null, 0);
    }

    public AutoPlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoPlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void startPlayer() {
        if (path == null || path.isEmpty() || (isVideoMode() && surfaceHolder == null)) {
            return;
        }
        if (mediaPlayer != null) {
            mediaPlayer.start();
        } else {
            try {
                initPlayer();
            } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initPlayer() throws IOException {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(completionListener);
        mediaPlayer.setOnPreparedListener(preparedListener);
        mediaPlayer.setOnErrorListener(errorListener);
        Log.d(TAG, "initPlayer: looping:"+isLooping);
        mediaPlayer.setLooping(isLooping);
        mediaPlayer.setDataSource(path);
        mutePlayer();
        if (isVideoMode()) {
            mediaPlayer.setDisplay(surfaceHolder);
        }
        mediaPlayer.prepareAsync();
    }

    public void playNext() {
        if (path == null || path.isEmpty() || (isVideoMode() && (surfaceHolder == null || !isSurfaceReady))) {
            return;
        }
        if (mediaPlayer == null) {
            try {
                initPlayer();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            mediaPlayer.setLooping(isLooping);
            mediaPlayer.setDataSource(path);
            mutePlayerOnCondition();
            if (isVideoMode()) {
                mediaPlayer.setDisplay(surfaceHolder);
            }
            mediaPlayer.prepareAsync();
        } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

      private boolean initialized;

    public boolean isInitialized() {
        return initialized;
    }

    public boolean init() {
        if (isVideoMode()) {
            return initSurface();
        }
        else {
            if (mediaPlayer != null) {
                startPlayer();
                return true;
            }
            return false;
        }
    }

    private boolean initSurface() {
        if (surfaceHolder != null) {
            initialized = true;
            return true;
        }
        surfaceHolder = getHolder();
        if (isSurfaceReady) {
            startPlayer();
            return true;
        }
        surfaceHolder.addCallback(this);
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        // release resources on detach
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDetachedFromWindow();
    }


    public void clearAll() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isMuted = false;
        if (surfaceHolder != null) {
            surfaceHolder.removeCallback(this);
        }
    }

    public void pausePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void stopPlayer() {
        Log.d(TAG, "stopPlayer: "+mediaPlayer);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void mutePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(0f, 0f);
            Log.d(TAG, "mutePlayer: muted");
            isMuted = true;
            shouldMute = true;
        }
    }


    private void mutePlayerOnCondition() {
        if (mediaPlayer != null && shouldMute) {
            mediaPlayer.setVolume(0f, 0f);
            Log.d(TAG, "mutePlayer: muted");
            isMuted = true;
        }
    }

    public void unmutePlayer() {
        if (mediaPlayer != null) {
            Log.d(TAG, "unmutePlayer: unmuted");
            mediaPlayer.setVolume(1f, 1f);
            isMuted = false;
            shouldMute = false;
        }
    }


    public void setVideoMode(boolean videoMode) {
        this.videoMode = videoMode;
    }

    public boolean isMuted() {
        Log.d(TAG, "isMuted: "+isMuted);
        return isMuted;
    }

    private boolean isVideoMode() {
        return videoMode;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public String getSource() {
        return path;
    }

    public void setSource(String source) {
        this.path = source;
        Log.d(TAG, "setSource() called with: source = [" + source + "]");
    }

    public boolean isLooping() {
        return isLooping;
    }

    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Logger.log(TAG, "surfaceCreated: ");
        surfaceHolder = holder;
        isSurfaceReady = true;
        initialized = true;
        startPlayer();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.log(TAG, "surfaceDestroyed: ");
        isSurfaceReady = false;
        surfaceHolder = null;
        initialized = false;
        if (callback != null) {
            callback.onSurfaceDestroyed();
        }

    }

    public void setListener(PeekPopVideoCallback callback) {
        this.callback = callback;
    }

    public interface PeekPopVideoCallback {
        void onSurfaceDestroyed();
    }
}