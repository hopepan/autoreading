package com.hopearena.autoreading.util;

import android.content.Context;
import android.os.Environment;
import java.io.File;
import android.media.MediaRecorder;

@Deprecated
public class AudioFileFunc {
    //音频输入-麦克风
    public final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;

    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    public final static int AUDIO_SAMPLE_RATE = 8000;  //44.1KHz,普遍使用的频率
    //录音输出文件
    private final static String AUDIO_RAW_FILENAME = "/data/audio/test.raw";
    private final static String AUDIO_WAV_FILENAME = "/data/audio/finalAudio.wav";
    public final static String AUDIO_AMR_FILENAME = "/data/audio/finalAudio.amr";

    /**
     * 判断是否有外部存储设备sdcard
     * @return true | false
     */
    public static boolean isSdcardExit(){
        return (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable());
    }

    /**
     * 获取麦克风输入的原始音频流文件路径
     * @return
     */
    public static String getRawFilePath(Context context){
        String mAudioRawPath = "";
        if(isSdcardExit()){
            mAudioRawPath = context.getExternalCacheDir().getAbsolutePath();
        } else {
            mAudioRawPath = context.getCacheDir().getAbsolutePath();
        }
        mAudioRawPath += AUDIO_RAW_FILENAME;
        return mAudioRawPath;
    }

    /**
     * 获取编码后的WAV格式音频文件路径
     * @return
     */
    public static String getWavFilePath(Context context){
        String mAudioWavPath = "";
        if(isSdcardExit()){
            mAudioWavPath = context.getExternalCacheDir().getAbsolutePath();
        } else {
            mAudioWavPath = context.getCacheDir().getAbsolutePath();
        }
        mAudioWavPath += AUDIO_WAV_FILENAME;
        return mAudioWavPath;
    }


    /**
     * 获取编码后的AMR格式音频文件路径
     * @return
     */
    public static String getAMRFilePath(Context context){
        String mAudioAMRPath = "";
        if(isSdcardExit()){
            mAudioAMRPath = context.getExternalCacheDir().getAbsolutePath();
        } else {
            mAudioAMRPath = context.getCacheDir().getAbsolutePath();
        }
        mAudioAMRPath += AUDIO_AMR_FILENAME;
        return mAudioAMRPath;
    }


    /**
     * 获取文件大小
     * @param path,文件的绝对路径
     * @return
     */
    public static long getFileSize(String path){
        File mFile = new File(path);
        if(!mFile.exists())
            return -1;
        return mFile.length();
    }

}
