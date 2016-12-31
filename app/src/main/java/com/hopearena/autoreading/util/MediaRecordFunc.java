package com.hopearena.autoreading.util;

import android.media.MediaRecorder;

import java.io.IOException;


public class MediaRecordFunc {

    private static MediaRecordFunc mInstance;

    private MediaRecorder  mediaRecorder = new MediaRecorder();

    private boolean isRecording;

    public synchronized static MediaRecordFunc getInstance() {
        if (mInstance == null)
            mInstance = new MediaRecordFunc();
        return mInstance;
    }

    public void startRecording(String fileName) throws IOException {

        mediaRecorder.reset();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(fileName);
        mediaRecorder.setMaxDuration(20000);
        mediaRecorder.prepare();
        mediaRecorder.start();
        isRecording = true;
    }

    public void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }
}
