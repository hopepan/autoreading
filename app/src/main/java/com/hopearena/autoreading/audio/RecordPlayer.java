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
    public void playRecordFile(File file) {
        if (file != null && file.exists()) {
            if (mediaPlayer == null) {
                Uri uri = Uri.fromFile(file);
                mediaPlayer = MediaPlayer.create(mContext, uri);
            }
            mediaPlayer.start();

            //监听MediaPlayer播放完成
            mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer paramMediaPlayer) {
                    // TODO Auto-generated method stub
                }
            });

        }
    }

    // 暂停播放录音
    public void pausePalyer() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.i(LOG_TAG, "暂停播放");
        }

    }

    // 停止播放录音
    public void stopPalyer() {
        // 这里不调用stop()，调用seekto(0),把播放进度还原到最开始
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            Log.i(LOG_TAG, "停止播放");
        }
    }
}
