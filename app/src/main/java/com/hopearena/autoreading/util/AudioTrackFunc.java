package com.hopearena.autoreading.util;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Deprecated
public class AudioTrackFunc {

    private static AudioTrackFunc mInstance;

    private AudioTrack audioTrack = null;
    private short[] mAudioTrackData;

    private AudioTrackFunc() {

    }

    public synchronized static AudioTrackFunc getInstance() {
        if (mInstance == null)
            mInstance = new AudioTrackFunc();
        return mInstance;
    }

    private void initAudioTrack() {
        int trackBufferSizeInBytes = AudioRecord.getMinBufferSize(
                AudioFileFunc.AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrackData = new short[trackBufferSizeInBytes];
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                AudioFileFunc.AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, trackBufferSizeInBytes,
                AudioTrack.MODE_STREAM);
    }

    public void play(final File file) {
        if(audioTrack == null) {
            initAudioTrack();
        }
        System.out.println(audioTrack.getPlayState());
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED || audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
            audioTrack.play();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        DataInputStream dis = new DataInputStream(
                                new BufferedInputStream(
                                        new FileInputStream(file)));
                        Log.d("TAG", "dis.available=" + dis.available());
                        System.out.println(file.getAbsolutePath());
                        System.out.println(dis.available());
                        while (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING
                                && dis.available() > 0) {
                            int i = 0;
                            while (dis.available() > 0
                                    && i < mAudioTrackData.length) {
                                mAudioTrackData[i] = dis.readShort();
                                i++;
                            }
                            wipe(mAudioTrackData, 0, mAudioTrackData.length);
                            audioTrack.write(mAudioTrackData, 0,
                                    mAudioTrackData.length);
                        }
                        audioTrack.stop();
                        dis.close();
                        Log.d("TAG", "dis.close()");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void stop() {
        if(audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
        }
    }

    private void wipe(short[] lin, int off, int len) {
        int i, j;
        for (i = 0; i < len; i++) {
            j = lin[i + off];
            lin[i + off] = (short) (j >> 2);
        }
    }
}
