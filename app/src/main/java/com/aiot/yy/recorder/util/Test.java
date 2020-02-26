package com.aiot.yy.recorder.util;

import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Test {
    private static void testFs(){
        List<Integer> list = new ArrayList<>();
        list.add(8000);
        list.add(11025);
        list.add(22050);
        list.add(16000);
        list.add(37800);
        list.add(44100);
        list.add(48000);
        list.add(96000);
        list.add(192000);
        for (int e : list){
            int bufferSize = AudioRecord.getMinBufferSize(e,
                    AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0){
                //list.add(i);
                Log.d("test",e + "");
            }
        }
    }

    public static void testAudioSource(){

        int fs = 96000;
        int bufferSize =
                AudioRecord.getMinBufferSize(fs, AudioFormat.CHANNEL_IN_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_CALL, fs,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    }
}
