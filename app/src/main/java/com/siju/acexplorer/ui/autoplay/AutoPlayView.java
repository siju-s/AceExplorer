package com.siju.acexplorer.ui.autoplay;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.siju.acexplorer.logging.Logger;

import java.io.IOException;


@SuppressWarnings({"SameParameterValue", "unused"})
public class AutoPlayView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "AutoPlayView";
    private MediaPlayer   mediaPlayer;
    private String        path;
    private boolean       isLooping;
    private SurfaceHolder surfaceHolder;


    public AutoPlayView(Context context) {
        this(context, null, 0);
    }

    public AutoPlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoPlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setSource(String source) {
        this.path = source;
    }


    public void startVideo() {
        if (path != null && !path.isEmpty()) {
            if (surfaceHolder != null) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                } else {
                    try {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setOnCompletionListener(completionListener);
                        mediaPlayer.setOnPreparedListener(preparedListener);
                        mediaPlayer.setOnErrorListener(errorListener);

                        mediaPlayer.setLooping(isLooping);
                        mediaPlayer.setDataSource(path);
                        muteVideo();
                        mediaPlayer.setDisplay(surfaceHolder);
                        mediaPlayer.prepareAsync();
                    } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void init() {
        surfaceHolder = getHolder();
        if (isSurfaceReady) {
            startVideo();
            return;
        }
        surfaceHolder.addCallback(this);
    }

    private boolean isSurfaceReady;

    private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
        }
    };

    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();

        }
    };

    private MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return true;
        }
    };


    public void setLooping(boolean looping) {
        isLooping = looping;
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

    public void pauseVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    private boolean isMuted;

    public void muteVideo() {
        if (mediaPlayer != null && !isMuted()) {
            mediaPlayer.setVolume(0f, 0f);
            isMuted = true;
        }
    }

    public void unmuteVideo() {
        if (mediaPlayer != null && isMuted()) {
            mediaPlayer.setVolume(1f, 1f);
            isMuted = false;
        }
    }

    public boolean isMuted() {
        return isMuted;
    }


    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public String getSource() {
        return path;
    }

    public boolean isLooping() {
        return isLooping;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Logger.log(TAG, "surfaceCreated: ");
        surfaceHolder = holder;
        isSurfaceReady = true;
        startVideo();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.log(TAG, "surfaceDestroyed: ");
        isSurfaceReady = false;
        surfaceHolder = null;
        if (callback != null) {
            callback.onSurfaceDestroyed();
        }

    }

    private PeekPopVideoCallback callback;

    public void setListener(PeekPopVideoCallback callback) {
        this.callback = callback;
    }

    public interface PeekPopVideoCallback {
        void onSurfaceDestroyed();
    }
}