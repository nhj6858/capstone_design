package com.example.loginUI;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    public static void SaveString(Context context, String key, String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences("loginFile", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.commit();
    }
    public static String GetString(Context context, String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences("loginFile", Activity.MODE_PRIVATE);
        String value = sharedPreferences.getString(key,"");
        return value;
    }

}
