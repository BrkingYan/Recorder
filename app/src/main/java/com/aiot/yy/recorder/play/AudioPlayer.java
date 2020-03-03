package com.aiot.yy.recorder.play;

import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.aiot.yy.recorder.util.SignalProc;


public class AudioPlayer implements IAudioPlayer{

    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private AudioTrack audioTrack;
    private int bufferSizeInBytes;

    private volatile PlayStatus status = PlayStatus.PLAY_NO_READY;

    private byte[] chirp;
    private int fs;
    private int channelNum;


    public AudioPlayer(int fs,double T,int fmin,int fmax,int channelNum){
        this.fs = fs;
        Log.d("player","fs:" + fs);
        this.channelNum = channelNum == 2 ? AudioFormat.CHANNEL_OUT_STEREO :
                AudioFormat.CHANNEL_OUT_MONO;
        if (channelNum == 2){
            chirp = SignalProc.upChirpForPlayStereo(fs,fmin,fmax,T);
        }else {
            chirp = SignalProc.upChirpForPlayMono(fs,fmin,fmax,T);
        }
        init();
    }

    private void init(){
        bufferSizeInBytes = AudioTrack.getMinBufferSize(fs,channelNum,AUDIO_FORMAT);

        //bufferSizeInBytes = bufferSizeInBytes * 2;
        Log.d("player","buffer size:" + bufferSizeInBytes);
        if (bufferSizeInBytes <= 0){
            throw new IllegalStateException("AudioTrack is not available " + bufferSizeInBytes);
        }

        while (bufferSizeInBytes < chirp.length){
            bufferSizeInBytes *= 2;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            audioTrack = new AudioTrack.Builder()
                    .setBufferSizeInBytes(bufferSizeInBytes)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AUDIO_FORMAT)
                        .setSampleRate(fs)
                        .setChannelMask(channelNum)
                        .build())
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
        }else {
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,fs,channelNum,
                    AUDIO_FORMAT,bufferSizeInBytes,AudioTrack.MODE_STREAM);
        }
        status = PlayStatus.PLAY_READY;
    }

    @Override
    public void startPlay() {
        if (status == PlayStatus.PLAY_NO_READY || audioTrack == null){
            throw new IllegalStateException("播放器未初始化");
        }
        if (status == PlayStatus.PLAY_START){
            throw new IllegalStateException("播放早已开始");
        }
        Log.d("player","===start===");
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                playAudioData();
            }
        }).start();
        status = PlayStatus.PLAY_START;
    }

    //@TargetApi(Build.VERSION_CODES.M)
    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void playAudioData(){
        byte[] bytes = new byte[bufferSizeInBytes];
        int len;
        audioTrack.play();

        while (status == PlayStatus.PLAY_START){
            //audioTrack.write(chirp,0,chirp.length,AudioTrack.WRITE_BLOCKING);
            audioTrack.write(chirp,0,chirp.length);
        }
    }



    @Override
    public void finishPlay() {
        if (status != PlayStatus.PLAY_START){
            throw new IllegalStateException("播放尚未开始");
        }else {
            audioTrack.stop();
            status = PlayStatus.PLAY_STOP;
            if (audioTrack != null){
                audioTrack.release();
                audioTrack = null;
            }
            status = PlayStatus.PLAY_NO_READY;
        }
    }

    @Override
    public boolean isPlaying() {
        return status == PlayStatus.PLAY_START;
    }
}
