package com.hopearena.autoreading.audio;

import java.io.File;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.util.Log;

/**
 *
 *
 *  录音播放类
 *
 */

public class RecordPlayer {

    private static final String LOG_TAG = RecordPlayer.class.getName();

    private static MediaPlayer mediaPlayer;

    private Context mContext;

    public RecordPlayer(Context context) {
        this.mContext = context;
    }

    // 播放录音文件
    public boolean playRecordFile(File file, final OnCompletionListener listener) {
        boolean rtn = false;
        System.out.println("len>>"+file.length());
        if (file != null && file.length()>0) {
            if (mediaPlayer == null) {
                Uri uri = Uri.fromFile(file);
                mediaPlayer = MediaPlayer.create(mContext, uri);
                //监听MediaPlayer播放完成
                mediaPlayer.setOnCompletionListener(listener);
            }
            if(!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                rtn = true;
            }
        }
        return rtn;
    }

    // 暂停播放录音
    public boolean pausePlayer() {
        if(mediaPlayer == null) {
            return false;
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.i(LOG_TAG, "暂停播放");
        }
        return true;
    }

    // 停止播放录音
    public boolean stopPlayer() {
        if(mediaPlayer == null) {
            return false;
        }
        // 这里不调用stop()，调用seekto(0),把播放进度还原到最开始
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            Log.i(LOG_TAG, "停止播放");
        }
        return true;
    }
}
