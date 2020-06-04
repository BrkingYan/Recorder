package com.aiot.yy.recorder.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

public class FileUtil {
    public static void pcmToWav(File pcmFile, File wavFile,int mSampleRate,int channelNum) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(pcmFile);
            fos = new FileOutputStream(wavFile);

            writeWavHeader(fos, fis.getChannel().size(), 16, mSampleRate, channelNum);

            int channelFormatNum = channelNum == 2 ? AudioFormat.CHANNEL_IN_STEREO :
                    AudioFormat.CHANNEL_IN_MONO;
            int bufferSize = AudioRecord.getMinBufferSize(mSampleRate, channelFormatNum,
                    AudioFormat.ENCODING_PCM_16BIT);

            byte[] data = new byte[bufferSize];
            while (fis.read(data) != -1) {
                fos.write(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }

                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeWavHeader(FileOutputStream fos, long pcmDataLength, int sampleFormat,
                                int sampleRate, int channels) throws IOException {
        long audioDataLength = pcmDataLength + 36;
        long bitRate = sampleRate * channels * sampleFormat / 8;
        byte[] header = new byte[44];
        // RIFF
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        // pcm data length
        header[4] = (byte) (pcmDataLength & 0xff);
        header[5] = (byte) ((pcmDataLength >> 8) & 0xff);
        header[6] = (byte) ((pcmDataLength >> 16) & 0xff);
        header[7] = (byte) ((pcmDataLength >> 24) & 0xff);
        // WAVE
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        // 'fmt '
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // 1(PCM)
        header[20] = 1;
        header[21] = 0;
        // channels
        header[22] = (byte) channels;
        header[23] = 0;
        // sample rate
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        // bit rate
        header[28] = (byte) (bitRate & 0xff);
        header[29] = (byte) ((bitRate >> 8) & 0xff);
        header[30] = (byte) ((bitRate >> 16) & 0xff);
        header[31] = (byte) ((bitRate >> 24) & 0xff);
        header[32] = 4;
        header[33] = 0;
        // 采样精度
        header[34] = (byte) sampleFormat;
        header[35] = 0;
        // data
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        // data length
        header[40] = (byte) (audioDataLength & 0xff);
        header[41] = (byte) ((audioDataLength >> 8) & 0xff);
        header[42] = (byte) ((audioDataLength >> 16) & 0xff);
        header[43] = (byte) ((audioDataLength >> 24) & 0xff);
        fos.write(header);
    }

    private static String error_file =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/ChirpRecord/error.txt";

    public static void writeError(String errorStr){
        Log.d("check","write error");
        PrintWriter writer = null;
        File file = new File(error_file);
        if (file.exists()){
            file.delete();
        }

        try {
            writer = new PrintWriter(error_file);
            writer.println(errorStr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            writer.close();
        }
    }

}
