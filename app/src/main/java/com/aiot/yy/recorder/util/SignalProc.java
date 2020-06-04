package com.aiot.yy.recorder.util;

public class SignalProc {
    private static final double PI = Math.PI;

    public static float[] upChirp(float fs,int fmin,int fmax,float T){
        int len = (int)(T*fs);
        float[] t = new float[len];
        float[] chirp = new float[len];
        for (int n = 0;n<len;n++){
            float k = (fmax-fmin)/T;
            t[n]=(float)n/fs;
            chirp[n] = (float) (Math.sin(2*PI*fmin*t[n]+ PI*k*t[n]*t[n]));
            //System.out.println(t[n]);
        }
        //Log.d("chirp","chirp len:" + len);
        //System.out.println(len);

        return chirp;
    }

    //48000  1000 21000 20000 1
    public static byte[] upChirpForPlayStereo(double fs,int fmin,int fmax,double T){
        int len = (int)(T*fs);
        double[] t = new double[len];
        int[] chirp = new int[len];
        byte[] res = new byte[4*len];
        for (int n = 0;n<len;n++){
            double k = (fmax-fmin)/T;
            t[n]=(double)n/fs;
            chirp[n] = (int)(Math.sin(2*PI*fmin*t[n]+ PI*k*t[n]*t[n])*32768);
            String intBinary = Integer.toBinaryString(chirp[n]);
            int l = intBinary.length();
            if (l <= 8){
                res[4*n+1] = 0;
                res[4*n+3] = 0;
            }else {
                boolean isNeg1 = l == 16;
                res[4*n+1] = isNeg1 ? (byte) (-(string2Byte(intBinary.substring(1,8))))
                        : string2Byte(intBinary.substring(0,l-8));
                res[4*n+3] = isNeg1 ? (byte) (-(string2Byte(intBinary.substring(1,8))))
                        : string2Byte(intBinary.substring(0,l-8));
            }

            if (l < 8){
                res[4*n] = string2Byte(intBinary.substring(0,l));
                res[4*n+2] = string2Byte(intBinary.substring(0,l));
            }else {
                boolean isNeg2 = intBinary.charAt(l-8) == '1';
                res[4*n] = isNeg2 ? (byte) (-string2Byte(intBinary.substring(l-7,l)))
                        : string2Byte(intBinary.substring(l-8,l));
                res[4*n+2] = isNeg2 ? (byte) (-string2Byte(intBinary.substring(l-7,l)))
                        : string2Byte(intBinary.substring(l-8,l));
            }
        }
        return res;
    }

    public static byte[] upChirpForPlayMono(double fs,int fmin,int fmax,double T){
        int len = (int)(T*fs);
        double[] t = new double[len];
        int[] chirp = new int[len];
        byte[] res = new byte[2*len];
        for (int n = 0;n<len;n++){
            double k = (fmax-fmin)/T;
            t[n]=(double)n/fs;
            chirp[n] = (int)(Math.sin(2*PI*fmin*t[n]+ PI*k*t[n]*t[n])*32768);
            String intBinary = Integer.toBinaryString(chirp[n]);
            int l = intBinary.length();
            if (l <= 8){
                res[2*n+1] = 0;
            }else {
                boolean isNeg1 = l == 16;
                res[2*n+1] = isNeg1 ? (byte) (-(string2Byte(intBinary.substring(1,8))))
                        : string2Byte(intBinary.substring(0,l-8));
            }

            if (l < 8){
                res[2*n] = string2Byte(intBinary.substring(0,l));
            }else {
                boolean isNeg2 = intBinary.charAt(l-8) == '1';
                res[2*n] = isNeg2 ? (byte) (-string2Byte(intBinary.substring(l-7,l)))
                        : string2Byte(intBinary.substring(l-8,l));

            }
        }
        return res;
    }

    private static byte string2Byte(String byteStr){
        byte sum = 0;
        int len = byteStr.length();
        for (int i = len-1;i>=0;i--){
            sum += (byteStr.charAt(i)-'0') * Math.pow(2,len-i-1);
        }
        return sum;
    }
}
