package com.hopearena.autoreading;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hopearena.autoreading.service.ArticleService;
import com.hopearena.autoreading.service.impl.ArticleServiceImpl;
import com.hopearena.autoreading.util.ErrorCode;
import com.hopearena.autoreading.util.FucUtil;
import com.hopearena.autoreading.util.JsonParser;
import com.hopearena.autoreading.util.MediaRecordFunc;
import com.hopearena.autoreading.util.PermissionUtil;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class ArticleAddActivity extends AppCompatActivity {

    private Drawable RECORDING_DRAWABLE;
    private Drawable STOP_DRAWABLE;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private EditText txtSpeechInput;
    private Button playButton;
    private Button pauseButton;
    private FloatingActionButton fab;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ArticleService articleService = new ArticleServiceImpl();
    private SpeechRecognizer mIat;
    private boolean isRecording = false;
    private AudioTrack track;

    private AudioRecord mAudioRecord;
    private short[] mAudioRecordData;
    private short[] mAudioTrackData;
    private File mAudioFile;

    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_add);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtSpeechInput = (EditText) findViewById(R.id.add_content);

        init();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(PermissionUtil.requestPermission(ArticleAddActivity.this,
                            Manifest.permission.RECORD_AUDIO, PermissionUtil.PERMISSION_REQUEST_CODE_RECORD_AUDIO)) {
                        recordAudioFile();
                    }
                }
            });
        } else {
            fab.setEnabled(false);
            Snackbar.make(findViewById(R.id.add_main_clayout),
                    getString(R.string.speech_not_supported),
                    Snackbar.LENGTH_SHORT).show();
        }

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//                try {
//                    mediaPlayer = new MediaPlayer();
//                    File fpath = new File(getApplicationContext().getExternalCacheDir().getAbsolutePath() + "/data/audio/");
//                    if (!fpath.exists()) {
//                        fpath.mkdirs();
//                    }
//                    mediaPlayer.setDataSource(fpath + "/test.3gp");
//                    mediaPlayer.prepare();
//                    mediaPlayer.start();
//                } catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                } catch (IllegalStateException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//                        mp.release();
//                    }
//                });
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            track.play();
                            DataInputStream dis = new DataInputStream(
                                    new BufferedInputStream(
                                            new FileInputStream(mAudioFile)));
                            Log.d("TAG", "dis.available=" + dis.available());
                            while (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING
                                    && dis.available() > 0) {
                                int i = 0;
                                while (dis.available() > 0
                                        && i < mAudioTrackData.length) {
                                    mAudioTrackData[i] = dis.readShort();
                                    i++;
                                }
                                wipe(mAudioTrackData, 0, mAudioTrackData.length);
                                track.write(mAudioTrackData, 0,
                                        mAudioTrackData.length);
                            }
                            track.stop();
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
        });
        pauseButton = (Button) findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                track.stop();
//                if(mediaPlayer != null){
//                    mediaPlayer.stop();
//                }
            }
        });

        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=583ba0a6," + SpeechConstant.FORCE_LOGIN +"=true");
        mIat = SpeechRecognizer.createRecognizer(this, null);

        RECORDING_DRAWABLE = new BitmapDrawable(getResources(),
                BitmapFactory.decodeResource(getResources(), android.R.drawable.presence_video_busy));
        STOP_DRAWABLE = new BitmapDrawable(getResources(),
                BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_btn_speak_now));

        mIatResults.clear();
        txtSpeechInput.setText(null);
    }

    private void wipe(short[] lin, int off, int len) {
        int i, j;
        for (i = 0; i < len; i++) {
            j = lin[i + off];
            lin[i + off] = (short) (j >> 2);
        }
    }

    private void init() {

        File path = new File(getApplicationContext().getExternalCacheDir().getAbsolutePath() + "/data/");
        if(!path.exists()) {
            path.mkdirs();
        }
        mAudioFile = new File(path.getAbsolutePath() + "/audiotest.pcm");
        if (mAudioFile.exists()) {
            mAudioFile.delete();
        } else {
            try {
                mAudioFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            int sampleRateInHz = 16000;//44100;
            int recordBufferSizeInBytes = AudioRecord.getMinBufferSize(
                    sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            mAudioRecordData = new short[recordBufferSizeInBytes];
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, recordBufferSizeInBytes);

            int trackBufferSizeInBytes = AudioRecord.getMinBufferSize(
                    sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            mAudioTrackData = new short[trackBufferSizeInBytes];
            track = new AudioTrack(AudioManager.STREAM_MUSIC,
                    sampleRateInHz, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, trackBufferSizeInBytes,
                    AudioTrack.MODE_STREAM);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    private void recordAudioFile() {
        File fpath = new File(getApplicationContext().getExternalCacheDir().getAbsolutePath() + "/data/audio/");
        if (!fpath.exists()) {
            fpath.mkdirs();
        }
        System.out.println("isrecord>>"+isRecording);
        if(isRecording) {
            if (mAudioRecord != null
                    && mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                mAudioRecord.stop();
                isRecording = false;
                fab.setImageDrawable(STOP_DRAWABLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("run");
                        translateAudio();
                        System.out.println("run1");
                    }
                }).start();
            }
        } else {
            fab.setImageDrawable(RECORDING_DRAWABLE);
            isRecording = true;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        mAudioRecord.startRecording();
                        DataOutputStream dos = new DataOutputStream(
                                new BufferedOutputStream(
                                        new FileOutputStream(mAudioFile)));
                        while (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                            int number = mAudioRecord.read(
                                    mAudioRecordData, 0,
                                    mAudioRecordData.length);
                            for (int i = 0; i < number; i++) {
                                dos.writeShort(mAudioRecordData[i]);
                            }
//                            if (AudioRecord.ERROR_BAD_VALUE != number
//                                    && AudioRecord.ERROR != number) {
//                                System.out.println("in");
//                            }
                        }
                        dos.flush();
                        dos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        isRecording = false;
                        fab.setImageDrawable(STOP_DRAWABLE);
                    } catch (IOException e) {
                        e.printStackTrace();
                        isRecording = false;
                        fab.setImageDrawable(STOP_DRAWABLE);
                    }
                }
            }).start();
        }
//        int ret =  AudioRecordFunc.getInstance().startRecordAndFile(fpath + "test");
//        if(ErrorCode.E_STATE_RECODING == ret) {
//            AudioRecordFunc.getInstance().stopRecordAndFile();
//            fab.setImageDrawable(STOP_DRAWABLE);
//        } else if(ErrorCode.SUCCESS == ret) {
//            fab.setImageDrawable(RECORDING_DRAWABLE);
//        }
    }


    //http://blog.csdn.net/imhxl/article/details/50854146
    private void translateAudio() {
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
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
        mIat.setParameter(SpeechConstant.SAMPLE_RATE, "16000");
        //只有设置这个属性为1时,VAD_BOS  VAD_EOS才会生效,且RecognizerListener.onVolumeChanged才有音量返回默认：1
        mIat.setParameter(SpeechConstant.VAD_ENABLE,"1");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "1000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");
        //保存音频文件的路径   仅支持pcm和wav
//        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, mAudioFile.getAbsolutePath());
        //在传文件路径方式（-2）下，SDK通过应用层设置的ASR_SOURCE_PATH值， 直接读取音频文件。目前仅在SpeechRecognizer中支持。
        mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
//        mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, mAudioFile.getAbsolutePath());

        int ret = mIat.startListening(new RecognizerListener() {
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {

            }

            @Override
            public void onBeginOfSpeech() {
                Snackbar.make(findViewById(R.id.add_main_clayout),
                        "开始识别",
                        Snackbar.LENGTH_SHORT).show();
                System.out.println("开始识别");
            }

            @Override
            public void onEndOfSpeech() {
                Snackbar.make(findViewById(R.id.add_main_clayout),
                        "识别结束",
                        Snackbar.LENGTH_SHORT).show();
                System.out.println("识别结束");
            }

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String str = JsonParser.parseIatResult(recognizerResult.getResultString());
//                Snackbar.make(findViewById(R.id.add_main_clayout),
//                        str,
//                        Snackbar.LENGTH_SHORT).show();
                String resultString = txtSpeechInput.getText().toString();
                resultString += getResult(recognizerResult);
                txtSpeechInput.setText(resultString);
                System.out.println("识别结果"+str);
            }

            @Override
            public void onError(SpeechError speechError) {
                Snackbar.make(findViewById(R.id.add_main_clayout),
                        speechError.getErrorDescription() + speechError.toString(),
                        Snackbar.LENGTH_SHORT).show();
                System.out.println("识别出错");
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });

        if (ret != com.iflytek.cloud.ErrorCode.SUCCESS) {
            System.out.println("识别失败,错误码：" + ret);
        } else {
            byte[] audioData = FucUtil.readAudioFile(this, "/data/audiotest.pcm");
            System.out.println("len>>"+audioData.length);

            if (null != audioData) {
                // 一次（也可以分多次）写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），位长16bit，单声道的wav或者pcm
                // 写入8KHz采样的音频时，必须先调用setParameter(SpeechConstant.SAMPLE_RATE, "8000")设置正确的采样率
                // 注：当音频过长，静音部分时长超过VAD_EOS将导致静音后面部分不能识别。
                // 音频切分方法：FucUtil.splitBuffer(byte[] buffer,int length,int spsize);
                //voiceBuffer为音频数据流，splitBuffer为自定义分割接口，将其以4.8k字节分割成数组
                ArrayList<byte[]> buffers = FucUtil.splitBuffer(audioData,audioData.length, 4800);
                for (int i = 0; i < buffers.size(); i++) {
                    mIat.writeAudio(buffers.get(i), 0, buffers.get(i).length);
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mIat.stopListening();
            } else {
                mIat.cancel();
                System.out.println("读取音频流失败");
            }
        }
    }

    private String getResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        return resultBuffer.toString();
    }

    /**
     * Receiving speech input
     * */
    @Override
    @Deprecated
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String resultString = txtSpeechInput.getText().toString();
                    for(int i=0;i<result.size();i++){
                        resultString += result.get(i);
                    }
                    txtSpeechInput.setText(resultString);
                }
                break;
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CODE_RECORD_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startRecording();
                recordAudioFile();
            } else {
                // Permission Denied
                Snackbar.make(findViewById(R.id.add_main_clayout), "您没有授权该权限，请在设置中打开授权", Snackbar.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, ArticleListActivity.class));
            return true;
        } else if(id == R.id.add_menu_save) {
            // open saving popup
        } else if(id == R.id.add_menu_setting) {

        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void recordAudio() {
        if(MediaRecordFunc.getInstance().isRecording()) {
            MediaRecordFunc.getInstance().stopRecording();
            fab.setImageDrawable(STOP_DRAWABLE);
        } else {
            File fpath = new File(getApplicationContext().getExternalCacheDir().getAbsolutePath() + "/data/audio/");
            if (!fpath.exists()) {
                fpath.mkdirs();
            }
            try {
                 MediaRecordFunc.getInstance().startRecording(fpath + "/test.wav");
            } catch (IOException e) {
                e.printStackTrace();
                Snackbar.make(findViewById(R.id.add_main_clayout),
                        e.getMessage(),
                        Snackbar.LENGTH_SHORT).show();
            }
            fab.setImageDrawable(RECORDING_DRAWABLE);
        }
//        if(isRecording) {
//            stopRecording();
//        } else {
//            if(PermissionUtil.requestPermission(this, Manifest.permission.RECORD_AUDIO, PermissionUtil.PERMISSION_REQUEST_CODE_RECORD_AUDIO)) {
//                startRecording();
//            }
//        }
    }

    @Deprecated
    private void startRecording(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        File fpath = new File(this.getApplicationContext().getExternalCacheDir().getAbsolutePath() + "/data/audio/");
        if (!fpath.exists()) {
            fpath.mkdirs();
        }
        mediaRecorder.setOutputFile(fpath + "/test.3gp");
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
        isRecording = true;
        System.out.println("done");

    }

    @Deprecated
    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        isRecording = false;
    }

    /**
     * Showing google speech input dialog
     * */
    @Deprecated
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        if(PermissionUtil.requestPermission(this, Manifest.permission.RECORD_AUDIO, PermissionUtil.PERMISSION_REQUEST_CODE_RECORD_AUDIO)) {
            startRecording();
        }
    }
}
