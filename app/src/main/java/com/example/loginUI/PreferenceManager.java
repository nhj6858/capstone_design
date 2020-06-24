package com.example.loginUI;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    public static void SaveString(Context context, String key, String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences("loginFile", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.commit();
    }
    public static String GetString(Context context, String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences("loginFile", 0);
        String value = sharedPreferences.getString(key,"");
        return value;
    }
    public static void SaveInteger(Context context, String key,int value){
        SharedPreferences sharedPreferences = context.getSharedPreferences("loginFile", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key,value);
        editor.commit();
    }
    public static int GetInteger(Context context,String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences("loginFile", 0);
        int value = sharedPreferences.getInt(key, 0);
        return value;
    }

}
