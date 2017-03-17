package com.hopearena.autoreading;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hopearena.autoreading.service.ArticleService;
import com.hopearena.autoreading.service.impl.ArticleServiceImpl;
import com.hopearena.autoreading.audio.AudioRecordFunc;
import com.hopearena.autoreading.util.ErrorCode;
import com.hopearena.autoreading.util.PermissionUtil;
import com.hopearena.autoreading.audio.RecogniseFunc;
import com.hopearena.autoreading.audio.RecordPlayer;
import com.hopearena.autoreading.util.RippleBackground;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ArticleAddActivity extends AppCompatActivity {

    private Drawable RECORDING_DRAWABLE;
    private Drawable STOP_DRAWABLE;
    private EditText txtSpeechInput;
    private Button playButton;
    private Button pauseButton;
    private FloatingActionButton fab;
    RippleBackground rippleBackground;
    private ArticleService articleService = new ArticleServiceImpl();

    private File audioFile;

    private AudioRecordFunc audioRecordFunc;
    private RecogniseFunc recogniseFunc;
    private RecordPlayer recordPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_add);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtSpeechInput = (EditText) findViewById(R.id.add_content);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        playButton = (Button) findViewById(R.id.play_button);
        pauseButton = (Button) findViewById(R.id.pause_button);
        rippleBackground = (RippleBackground)findViewById(R.id.content);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        prepareAudioFile();

        audioRecordFunc = new AudioRecordFunc();
        recogniseFunc = new RecogniseFunc(getApplicationContext());
        recordPlayer = new RecordPlayer(getApplicationContext());

        txtSpeechInput.setText(null);

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
//            fab.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    switch (event.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            int x = (int) event.getRawX();
//                            int y = (int) event.getRawY();
//                            rippleBackground.startRippleAnimation(x, y);
//                            if(PermissionUtil.requestPermission(ArticleAddActivity.this,
//                                    Manifest.permission.RECORD_AUDIO, PermissionUtil.PERMISSION_REQUEST_CODE_RECORD_AUDIO)) {
//                                recordAudioFile();
//                            }
//                            break;
//                        case MotionEvent.ACTION_UP:
//                            if(PermissionUtil.requestPermission(ArticleAddActivity.this,
//                                    Manifest.permission.RECORD_AUDIO, PermissionUtil.PERMISSION_REQUEST_CODE_RECORD_AUDIO)) {
//                                recordAudioFile();
//                            }
//                            rippleBackground.stopRippleAnimation();
//                            break;
//                    }
//                    return true;
//                }
//            });
        } else {
            fab.setEnabled(false);
            Snackbar.make(findViewById(R.id.add_main_clayout),
                    getString(R.string.speech_not_supported),
                    Snackbar.LENGTH_SHORT).show();
        }

        playButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(recordPlayer.playRecordFile(audioFile, new OnCompletionListenerImpl())) {
                    playButton.setVisibility(View.INVISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);
                } else {
                    Snackbar.make(findViewById(R.id.add_main_clayout),
                            getString(R.string.play_error),
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(recordPlayer.stopPlayer()) {
                    pauseButton.setVisibility(View.INVISIBLE);
                    playButton.setVisibility(View.VISIBLE);
                } else {
                    Snackbar.make(findViewById(R.id.add_main_clayout),
                            getString(R.string.play_error),
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        pauseButton.setVisibility(View.INVISIBLE);

        RECORDING_DRAWABLE = new BitmapDrawable(getResources(),
                BitmapFactory.decodeResource(getResources(), android.R.drawable.presence_video_busy));
        STOP_DRAWABLE = new BitmapDrawable(getResources(),
                BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_btn_speak_now));
    }

    private void prepareAudioFile() {
        File fpath = new File(getApplicationContext().getExternalCacheDir().getAbsolutePath() + "/data/audio/");
        if (!fpath.exists()) {
            fpath.mkdirs();
        }
        audioFile = new File(fpath + "/audioRecord.wav");
        try {
            if (audioFile.exists()) {
                audioFile.delete();
            } else {
                audioFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recordAudioFile() {
        int ret =  audioRecordFunc.startRecordAndFile(audioFile);
        if(ErrorCode.E_STATE_RECODING == ret) {
            audioRecordFunc.stopRecordAndFile();
            fab.setImageDrawable(STOP_DRAWABLE);
            new Thread(new Runnable() {
                    @Override
                    public void run() {
                        recogniseFunc.recogniseAudio(audioRecordFunc.getAudioRawBytes(), txtSpeechInput);
                    }
                }).start();
        } else if(ErrorCode.SUCCESS == ret) {
            fab.setImageDrawable(RECORDING_DRAWABLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CODE_RECORD_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

    private class OnCompletionListenerImpl implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            pauseButton.setVisibility(View.INVISIBLE);
            playButton.setVisibility(View.VISIBLE);
        }
    }
}
