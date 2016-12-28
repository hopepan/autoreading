package com.hopearena.autoreading.util;

import android.content.Context;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;


public class MediaRecordFunc {

    private MediaRecorder mediaRecorder;

    private boolean isRecording;

    public boolean startRecording(File fpath){

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        if (!fpath.exists()) {
            fpath.mkdirs();
        }
        mediaRecorder.setOutputFile(fpath + "/test.3gp");
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
        } catch (IllegalStateException e) {
            isRecording = false;
            e.printStackTrace();
        } catch (IOException e) {
            isRecording = false;
            e.printStackTrace();
        }
        return isRecording;
    }

    public void stopRecording() {
        if(isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            isRecording = false;
        }
    }


}
