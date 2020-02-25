package com.aiot.yy.recorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aiot.yy.recorder.play.AudioPlayer;
import com.aiot.yy.recorder.record.AudioRecorder;
import com.aiot.yy.recorder.util.SPUtil;

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

    private static final String FMIN_FILE = "fmin";
    private static final String FMAX_FILE = "fmax";
    private static final String B_FILE = "b";
    private static final String FS_FILE = "fs";
    private static final String T_FILE = "t";
    private static final String CHANNEL_FILE = "channel";

    private EditText fminInput;
    private EditText fmaxInput;
    private EditText BInput;
    private NiceSpinner fsSpinner;
    private NiceSpinner channelSpinner;
    private EditText TInput;
    private EditText fileNameInput;
    private Button startBtn;
    private Button saveBtn;

    List<String> fsSet = new LinkedList<>(Arrays.asList("11025", "22050", "24000", "44100", "48000"));
    List<String> channelSet = new LinkedList<>(Arrays.asList("1", "2"));
    private int selectedFsIdx = 0;
    private int selectedChannelIdx = 0;

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
        startBtn = findViewById(R.id.start_btn);
        saveBtn = findViewById(R.id.save_btn);

        startBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);


        fsSpinner.attachDataSource(fsSet);
        channelSpinner.attachDataSource(channelSet);

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_btn:{
                if (startBtn.getText().toString().equals("start")){
                    disableViews();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startBtn.setText("stop");
                        }
                    }, 300);
                    String fileName = "test";
                    if (!fileNameInput.getText().toString().equals("")){
                        fileName = fileNameInput.getText().toString();
                    }
                    //开始录音
                    audioRecorder = new AudioRecorder(fs,fileName,channels);
                    if (!audioRecorder.isRecording()) {
                        audioRecorder.startRecord();
                    }
                    //开始播放
                    //初始化音频处理对象
                    audioPlayer = new AudioPlayer(fs,this.T, this.fmin, this.fmax,channels);
                    audioPlayer.startPlay();
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
                    audioPlayer.finishPlay();
                    audioPlayer = null;
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
    }

    private void enableViews(){
        fminInput.setEnabled(true);
        fmaxInput.setEnabled(true);
        BInput.setEnabled(true);
        TInput.setEnabled(true);
        fsSpinner.setEnabled(true);
        fileNameInput.setEnabled(true);
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

        Log.d("test","param:" + "fmin:" + this.fmin + " fmax:" + this.fmax +
            " T:" + this.T + " B:" + this.B + " channel:"+this.channels + " fs:" + this.fs);
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
