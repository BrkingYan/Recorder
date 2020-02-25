package com.aiot.yy.recorder.record;

/**
 * Created by cc on 2016/10/13.
 */

public interface IAudioRecorder {
    void startRecord();
    void finishRecord();
    boolean isRecording();
}
