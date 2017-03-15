package com.hopearena.autoreading.audio;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.widget.EditText;

import com.hopearena.autoreading.R;
import com.hopearena.autoreading.util.FucUtil;
import com.hopearena.autoreading.util.JsonParser;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;

public class RecogniseFunc {

    private SpeechRecognizer mIat;


    public RecogniseFunc(Context context) {
        initIat(context);
    }

    private void initIat(Context context) {
        SpeechUtility.createUtility(context, SpeechConstant.APPID +"=583ba0a6," + SpeechConstant.FORCE_LOGIN +"=true");
        mIat = SpeechRecognizer.createRecognizer(context, null);

        mIat.setParameter(SpeechConstant.PARAMS, null);
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        //普通话：mandarin(默认)
        //粤 语：cantonese
        //四川话：lmz
        //河南话：henanese
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
//        mIat.setParameter(SpeechConstant.SAMPLE_RATE, ""+AudioRecordFunc.AUDIO_SAMPLE_RATE);
//        mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        //只有设置这个属性为1时,VAD_BOS  VAD_EOS才会生效,且RecognizerListener.onVolumeChanged才有音量返回默认：1
        mIat.setParameter(SpeechConstant.VAD_ENABLE,"1");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");
        //保存音频文件的路径   仅支持pcm和wav
//        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, mAudioFile.getAbsolutePath());
        //在传文件路径方式（-2）下，SDK通过应用层设置的ASR_SOURCE_PATH值， 直接读取音频文件。目前仅在SpeechRecognizer中支持。
        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
//        mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, mAudioFile.getAbsolutePath());
    }

    public void recogniseAudio(File file, final EditText txtSpeechInput) {
        int ret = mIat.startListening(new RecognizerListener() {
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {
            }

            @Override
            public void onBeginOfSpeech() {
                // trigger the UI to block the user input and wait
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String resultString = txtSpeechInput.getText().toString();
                resultString += getResult(recognizerResult);
                txtSpeechInput.setText(resultString);
            }

            @Override
            public void onError(SpeechError speechError) {
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });

        if (ret == com.iflytek.cloud.ErrorCode.SUCCESS) {
            byte[] audioData = FucUtil.readAudioFile(file);

            if (null != audioData) {
                // 一次（也可以分多次）写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），位长16bit，单声道的wav或者pcm
                // 写入8KHz采样的音频时，必须先调用setParameter(SpeechConstant.SAMPLE_RATE, "8000")设置正确的采样率
                // 注：当音频过长，静音部分时长超过VAD_EOS将导致静音后面部分不能识别。
                // 音频切分方法：FucUtil.splitBuffer(byte[] buffer,int length,int spsize);
                //voiceBuffer为音频数据流，splitBuffer为自定义分割接口，将其以4.8k字节分割成数组
//                ArrayList<byte[]> buffers = FucUtil.splitBuffer(audioData,audioData.length, 10000);
//                for (int i = 0; i < buffers.size(); i++) {
//                    mIat.writeAudio(buffers.get(i), 0, buffers.get(i).length);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
                mIat.writeAudio(audioData, 0, audioData.length);
                mIat.stopListening();
            } else {
                mIat.cancel();
            }
        }
    }

    public void recogniseAudio(Queue<byte[]> rawBytes, final EditText txtSpeechInput) {
        int ret = mIat.startListening(new RecognizerListener() {
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {
            }

            @Override
            public void onBeginOfSpeech() {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String resultString = txtSpeechInput.getText().toString();
                System.out.println("result>>"+resultString);
                resultString += getResult(recognizerResult);
//                String resultString = getResult(recognizerResult);
                txtSpeechInput.setText(resultString);
            }

            @Override
            public void onError(SpeechError speechError) {
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });

        if (ret == com.iflytek.cloud.ErrorCode.SUCCESS) {
            if (null != rawBytes && !rawBytes.isEmpty()) {
                // 一次（也可以分多次）写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），位长16bit，单声道的wav或者pcm
                // 写入8KHz采样的音频时，必须先调用setParameter(SpeechConstant.SAMPLE_RATE, "8000")设置正确的采样率
                // 注：当音频过长，静音部分时长超过VAD_EOS将导致静音后面部分不能识别。
                // 音频切分方法：FucUtil.splitBuffer(byte[] buffer,int length,int spsize);
                //voiceBuffer为音频数据流，splitBuffer为自定义分割接口，将其以4.8k字节分割成数组
//                ArrayList<byte[]> buffers = FucUtil.splitBuffer(audioData,audioData.length, 10000);
//                for (int i = 0; i < buffers.size(); i++) {
//                    mIat.writeAudio(buffers.get(i), 0, buffers.get(i).length);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
                while (rawBytes.peek() != null) {
                    byte[] audioData = rawBytes.poll();
                    mIat.writeAudio(audioData, 0, audioData.length);
                }
                mIat.stopListening();
            } else {
                mIat.cancel();
            }
        }
    }

    private String getResult(RecognizerResult results) {
        return JsonParser.parseIatResult(results.getResultString());
    }
}
