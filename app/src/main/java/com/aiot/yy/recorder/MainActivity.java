package com.aiot.yy.recorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.aiot.yy.recorder.play.AudioPlayer;
import com.aiot.yy.recorder.record.AudioRecorder;
import com.aiot.yy.recorder.util.SPUtil;
import com.aiot.yy.recorder.util.Test;
import com.kyleduo.switchbutton.SwitchButton;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String FMIN_KEY = "fmin";
    private static final String FMAX_KEY = "fmax";
    private static final String B_KEY = "b";
    private static final String FS_KEY = "fs";
    private static final String T_KEY = "t";
    private static final String CHANNEL_KEY = "channel";
    private static final String PLAY_FLAG_KEY = "play";
    private static final String AUDIO_SOURCE_KEY = "audio";

    private static final String FMIN_FILE = "fmin";
    private static final String FMAX_FILE = "fmax";
    private static final String B_FILE = "b";
    private static final String FS_FILE = "fs";
    private static final String T_FILE = "t";
    private static final String CHANNEL_FILE = "channel";
    private static final String PLAY_FLAG_FILE = "play";
    private static final String AUDIO_SOURCE_FILE = "audio";

    private EditText fminInput;
    private EditText fmaxInput;
    private EditText BInput;
    private NiceSpinner fsSpinner;
    private NiceSpinner channelSpinner;
    private NiceSpinner audioSourceSpinner;
    private EditText TInput;
    private EditText fileNameInput;
    private Button startBtn;
    private Button saveBtn;
    private SwitchButton switchButton;

    private static final List<String> fsSet = new LinkedList<>(Arrays.asList("8000","11025","16000", "22050", "24000",
           "37800", "44100", "48000","96000","192000"));
    private static final List<String> channelSet = new LinkedList<>(Arrays.asList("1", "2"));
    private static final List<String> audioSourceSet = new LinkedList<>(Arrays.asList("MIC","DEFAULT",
            "VOICE_CALL",
            "VOICE_COMMUNICATION","VOICE_DOWNLINK","VOICE_UPLINK",
            "VOICE_RECOGNITION","CAMCORDER",
            "UNPROCESSED","REMOTE_SUBMIX"));
    private int selectedFsIdx = 0;
    private int selectedChannelIdx = 0;
    private int selectedAudioSourceIdx = 0;

    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    List<String> permissionsNotPass;
    private static final int PERMISSION_RequestCode = 100;//权限请求码


    private AudioPlayer audioPlayer;
    private AudioRecorder audioRecorder;
    private int fmin;
    private int fmax;
    private int fs;
    private float T;
    private int B;
    private int channels;
    private String playFlag;
    private int audioFormatSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPermissions();
        initView();
        initData();

    }


    private void initView(){
        fminInput = findViewById(R.id.fmin_input);
        fmaxInput = findViewById(R.id.fmax_input);
        BInput = findViewById(R.id.B_input);
        TInput = findViewById(R.id.T_input);
        fileNameInput = findViewById(R.id.file_input);
        fsSpinner = findViewById(R.id.fs_spinner);
        channelSpinner = findViewById(R.id.channel_spinner);
        audioSourceSpinner = findViewById(R.id.source_spinner);
        startBtn = findViewById(R.id.start_btn);
        saveBtn = findViewById(R.id.save_btn);
        switchButton = findViewById(R.id.switch_btn);

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchButton.setChecked(isChecked);
                if (isChecked){
                    playFlag = "1";
                }else {
                    playFlag = "0";
                }
            }
        });

        startBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);


        fsSpinner.attachDataSource(fsSet);
        channelSpinner.attachDataSource(channelSet);
        audioSourceSpinner.attachDataSource(audioSourceSet);

        fsSpinner.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {
            // This example uses String, but your type can be any
            String item = (String) parent.getItemAtPosition(position);
            //fsSpinner.setText(item);
            fsSpinner.setSelectedIndex(position);
            selectedFsIdx = position;
            Log.d("test","pos:" + position);
        });
        channelSpinner.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {
            // This example uses String, but your type can be any
            String item = (String) parent.getItemAtPosition(position);
            //fsSpinner.setText(item);
            channelSpinner.setSelectedIndex(position);
            selectedChannelIdx = position;
        });
        audioSourceSpinner.setOnSpinnerItemSelectedListener((parent, view, position, id) -> {
            // This example uses String, but your type can be any
            String item = (String) parent.getItemAtPosition(position);
            //fsSpinner.setText(item);
            audioSourceSpinner.setSelectedIndex(position);
            selectedAudioSourceIdx = position;
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_btn:{
                if (startBtn.getText().toString().equals("start")){
                    Log.d("test","param:" + "fmin:" + this.fmin + " fmax:" + this.fmax +
                            " T:" + this.T + " B:" + this.B + " channel:"+this.channels + " fs:"
                            + this.fs + " souce:" + audioSourceSet.get(selectedAudioSourceIdx));
                    String fileName = "test";
                    if (!fileNameInput.getText().toString().equals("")){
                        fileName = fileNameInput.getText().toString();
                    }
                    //开始录音
                    audioRecorder = new AudioRecorder(fs,fileName,channels,audioFormatSource);
                    //测试音源
                    if (!audioRecorder.checkAudioSource()){
                        Toast.makeText(MainActivity.this,
                                "无法录音，请切换音源或修改参数后重试",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //变换按钮颜色
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startBtn.setText("stop");
                        }
                    }, 300);
                    disableViews();
                    if (!audioRecorder.isRecording()) {
                        audioRecorder.startRecord();
                    }
                    //开始播放
                    //初始化音频处理对象
                    if (switchButton.isChecked()){
                        try{
                            audioPlayer = new AudioPlayer(fs,this.T, this.fmin, this.fmax,channels);
                        }catch (Exception e){
                            Toast.makeText(MainActivity.this,"当前参数无法播放",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        audioPlayer.startPlay();
                    }
                }else {
                    enableViews();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startBtn.setText("start");
                        }
                    }, 300);
                    //停止录音
                    audioRecorder.finishRecord();
                    audioRecorder = null;
                    //停止播放
                    if (switchButton.isChecked()){
                        audioPlayer.finishPlay();
                        audioPlayer = null;
                    }
                }
                break;
            }
            case R.id.save_btn:{
                try {
                    saveData();
                }catch (Exception e){
                    Toast.makeText(this,"请输入正确格式的参数",Toast.LENGTH_SHORT).show();
                    break;
                }
                Toast.makeText(this,"参数保存成功",Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    private void disableViews(){
        fminInput.setEnabled(false);
        fmaxInput.setEnabled(false);
        BInput.setEnabled(false);
        TInput.setEnabled(false);
        fsSpinner.setEnabled(false);
        fileNameInput.setEnabled(false);
        switchButton.setEnabled(false);
        audioSourceSpinner.setEnabled(false);
        channelSpinner.setEnabled(false);
    }

    private void enableViews(){
        fminInput.setEnabled(true);
        fmaxInput.setEnabled(true);
        BInput.setEnabled(true);
        TInput.setEnabled(true);
        fsSpinner.setEnabled(true);
        fileNameInput.setEnabled(true);
        switchButton.setEnabled(true);
        audioSourceSpinner.setEnabled(true);
        channelSpinner.setEnabled(true);
    }

    private void initData(){
        String fmin = SPUtil.getString(this,FMIN_FILE,FMIN_KEY,"0");
        fminInput.setText(fmin);
        this.fmin = Integer.parseInt(fmin);

        String fmax = SPUtil.getString(this,FMAX_FILE,FMAX_KEY,"0");
        fmaxInput.setText(fmax);
        this.fmax = Integer.parseInt(fmax);


        String T = SPUtil.getString(this,T_FILE,T_KEY,"0.00");
        TInput.setText(T);
        this.T = Float.parseFloat(T);

        String fsIdx = SPUtil.getString(this,FS_FILE,FS_KEY,"0");
        Log.d("test","fsIdex:" + fsIdx);
        selectedFsIdx = Integer.parseInt(fsIdx);
        fsSpinner.setSelectedIndex(Integer.parseInt(fsIdx));
        this.fs = Integer.parseInt(fsSet.get(Integer.parseInt(fsIdx)));

        String B = SPUtil.getString(this,B_FILE,B_KEY,"0");
        BInput.setText(B);
        this.B = Integer.parseInt(B);

        String channelIdx = SPUtil.getString(this,CHANNEL_FILE,CHANNEL_KEY,"0");
        channelSpinner.setSelectedIndex(Integer.parseInt(channelIdx));
        selectedChannelIdx = Integer.parseInt(channelIdx);
        channels = Integer.parseInt(channelSet.get(Integer.parseInt(channelIdx)));

        playFlag = SPUtil.getString(this,PLAY_FLAG_FILE,PLAY_FLAG_KEY,"0");
        switchButton.setChecked(playFlag.equals("1"));

        String audioSourceIdx = SPUtil.getString(this,AUDIO_SOURCE_FILE,AUDIO_SOURCE_KEY,
                "0");
        selectedAudioSourceIdx = Integer.parseInt(audioSourceIdx);
        audioFormatSource = getAudioFormatSource(selectedAudioSourceIdx);
        audioSourceSpinner.setSelectedIndex(selectedAudioSourceIdx);

        Log.d("test","param:" + "fmin:" + this.fmin + " fmax:" + this.fmax +
            " T:" + this.T + " B:" + this.B + " channel:"+this.channels + " fs:" + this.fs);
    }

    private int getAudioFormatSource(int idx){
        int ret = -1;
        switch (idx){
            case 0:{
                ret = MediaRecorder.AudioSource.MIC;
                break;
            }
            case 1:{
                ret = MediaRecorder.AudioSource.DEFAULT;
                break;
            }
            case 2:{
                ret = MediaRecorder.AudioSource.VOICE_CALL;
                break;
            }
            case 3:{
                ret = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
                break;
            }
            case 4:{
                ret = MediaRecorder.AudioSource.VOICE_DOWNLINK;
                break;
            }
            case 5:{
                ret = MediaRecorder.AudioSource.VOICE_UPLINK;
                break;
            }
            case 6:{
                ret = MediaRecorder.AudioSource.VOICE_RECOGNITION;
                break;
            }
            case 7:{
                ret = MediaRecorder.AudioSource.CAMCORDER;
                break;
            }
            case 8:{
                ret = MediaRecorder.AudioSource.UNPROCESSED;
                break;
            }
            case 9:{
                ret = MediaRecorder.AudioSource.REMOTE_SUBMIX;
                break;
            }
        }
        return ret;
    }

    private void saveData(){
        String fmin = fminInput.getText().toString();
        SPUtil.putString(this,FMIN_FILE,FMIN_KEY,fmin);
        this.fmin = Integer.parseInt(fmin);

        String fmax = fmaxInput.getText().toString();
        SPUtil.putString(this,FMAX_FILE,FMAX_KEY,fmax);
        this.fmax = Integer.parseInt(fmax);

        String T = TInput.getText().toString();
        SPUtil.putString(this,T_FILE,T_KEY,T);
        this.T = Float.parseFloat(T);

        SPUtil.putString(this,FS_FILE,FS_KEY,selectedFsIdx+"");

        String B = BInput.getText().toString();
        SPUtil.putString(this,B_FILE,B_KEY,B);
        this.B = Integer.parseInt(B);

        fs = Integer.parseInt(fsSet.get(selectedFsIdx));

        SPUtil.putString(this,CHANNEL_FILE,CHANNEL_KEY,selectedChannelIdx+"");
        channels = Integer.parseInt(channelSet.get(selectedChannelIdx));

        SPUtil.putString(this,PLAY_FLAG_FILE,PLAY_FLAG_KEY,playFlag);

        SPUtil.putString(this,AUDIO_SOURCE_FILE,AUDIO_SOURCE_KEY,
                selectedAudioSourceIdx+"");
        audioFormatSource = getAudioFormatSource(selectedAudioSourceIdx);

    }


    //权限处理
    private void initPermissions(){
        permissionsNotPass = new ArrayList<>();
        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionsNotPass.add(permissions[i]);//添加还未授予的权限
            }
        }
        //申请权限
        if (permissionsNotPass.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_RequestCode);
        }else{
            //说明权限都已经通过，可以做你想做的事情去
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean hasPermissionDismiss = false;//有权限没有通过
        if (PERMISSION_RequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                finish();
            }
        }
    }
}
