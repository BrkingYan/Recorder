package com.aiot.yy.recorder.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtil {

    public static void putData(Context context, String fileName, String key, Object value){
        SharedPreferences sp = context.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String type = value.getClass().getSimpleName();
        try {
            switch (type){
                case "Boolean":{
                    editor.putBoolean(key,(Boolean)value);
                    break;
                }
                case "Long":{
                    editor.putLong(key,(Long)value);
                    break;
                }
                case "Float":{
                    editor.putFloat(key,(Float)value);
                    break;
                }
                case "String":{
                    editor.putString(key,(String)value);
                    break;
                }
                case "Integer":{
                    editor.putInt(key,(Integer)value);
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void putString(Context context, String fileName, String key, String value){
        SharedPreferences sp = context.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key,value);
        editor.apply();
    }

    public static String getString(Context context, String fileName, String key, String defaultValue){
        SharedPreferences sp = context.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        String ret = sp.getString(key,defaultValue);
        return ret;
    }


    public static Object getData(Context context, String fileName, String key, Object defaultValue){
        Object result;
        SharedPreferences sp = context.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        String type = defaultValue.getClass().getSimpleName();
        try {
            switch (type){
                case "Boolean":{
                    result = sp.getBoolean(key,(Boolean) defaultValue);
                    break;
                }
                case "Long":{
                    result = sp.getLong(key,(Long)defaultValue);
                    break;
                }
                case "Float":{
                    result = sp.getLong(key,(Long)defaultValue);
                    break;
                }
                case "String":{
                    result = sp.getString(key,(String)defaultValue);
                    break;
                }
                case "Integer":{
                    result = sp.getInt(key,(Integer)defaultValue);
                    break;
                }
                default:{
                    result = null;
                }
            }
        }catch (Exception e){
            result = null;
            e.printStackTrace();
        }
        return result;
    }
}
