package com.hopearena.autoreading.audio;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Queue;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.hopearena.autoreading.util.ErrorCode;


public class AudioRecordFunc {

    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    public final static int AUDIO_SAMPLE_RATE = 8000;  //44.1KHz,普遍使用的频率

    // 缓冲区字节大小
    private static int bufferSizeInBytes = 0;

    private RandomAccessFile wavFile;

    private static AudioRecord audioRecord;
    private boolean isRecord = false;// 设置正在录制的状态

    private Queue<byte[]> audioRawBytes;

    public AudioRecordFunc() {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        audioRawBytes = new LinkedList<>();
    }

    public int startRecordAndFile(File file) {
        //判断是否有外部存储设备sdcard
        if (isRecord) {
            return ErrorCode.E_STATE_RECODING;
        } else {
            // 创建AudioRecord对象
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);

            audioRecord.startRecording();

            // 让录制状态为true
            isRecord = true;

            try {
                fileOpen(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 开启音频文件写入线程
            new Thread(new AudioRecordThread()).start();

            return ErrorCode.SUCCESS;
        }
    }

    public void stopRecordAndFile() {
        close();
    }

    private void close() {
        if (audioRecord != null) {
            isRecord = false;//停止文件写入
            audioRecord.stop();
            audioRecord.release();//释放资源
            audioRecord = null;
        }
    }

    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            writeDataToQueue();

            try {
                wavFile.seek(wavFile.length());
                for (byte[] audioData : audioRawBytes) {
                    wavFile.write(audioData, 0, audioData.length);
                }
                fileClose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeDataToQueue() {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audioData = new byte[bufferSizeInBytes];
        int readSize;
        while (isRecord) {
            readSize = audioRecord.read(audioData, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                byte[] rawBytes = new byte[readSize];
                System.arraycopy(audioData, 0, rawBytes, 0, readSize);
                audioRawBytes.offer(rawBytes);
            }
        }
    }

    public Queue<byte[]> getAudioRawBytes() {
        return audioRawBytes;
    }

    private void fileOpen(File f) throws IOException {
        wavFile = new RandomAccessFile(f, "rw");

        if(wavFile.length() == 0) {
            // 16K、16bit、单声道
            /* RIFF header */
            wavFile.writeBytes("RIFF"); // riff id
            wavFile.writeInt(0); // riff chunk size *PLACEHOLDER*
            wavFile.writeBytes("WAVE"); // wave type

            /* fmt chunk */
            wavFile.writeBytes("fmt "); // fmt id
            wavFile.writeInt(Integer.reverseBytes(16)); // fmt chunk size
            wavFile.writeShort(Short.reverseBytes((short) 1)); // format: 1(PCM)
            wavFile.writeShort(Short.reverseBytes((short) 1)); // channels: 1
            wavFile.writeInt(Integer.reverseBytes(16000)); // samples per second
            wavFile.writeInt(Integer.reverseBytes((int) (1 * 16000 * 16 / 8))); // BPSecond
            wavFile.writeShort(Short.reverseBytes((short) (1 * 16 / 8))); // BPSample
            wavFile.writeShort(Short.reverseBytes((short) (1 * 16))); // bPSample

            /* data chunk */
            wavFile.writeBytes("data"); // data id
            wavFile.writeInt(0); // data chunk size *PLACEHOLDER*
        }
    }

    private void fileClose() throws IOException {
        try {
            long len = wavFile.length();
            wavFile.seek(4); // riff chunk size
            wavFile.writeInt(Integer.reverseBytes((int) (len - 8)));

            wavFile.seek(40); // data chunk size
            wavFile.writeInt(Integer.reverseBytes((int) (len - 44)));
         }finally {
            wavFile.close();
        }
    }
}