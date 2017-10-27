package com.example.iotmanager;

import android.content.Context;
import android.content.SharedPreferences;

import static android.R.attr.key;
import static android.R.attr.value;
import static android.content.Context.MODE_WORLD_WRITEABLE;
import static android.os.ParcelFileDescriptor.MODE_WORLD_READABLE;

/**
 * Created by eshia on 2017/10/26.
 */
/*  このクラスはプリファレンスを自分用に簡単にしたもの
    使い方は
    new MyPreference(this,key,data);
    でおｋ
* */
public class MyPreferences {
    static final String FCMTOKEN = "FCMTOKEN";
    static final String USERNAME = "USERNAME";
    static final String DOOR_REQUEST_RE = "DOOR_REQUEST_RE";

    MyPreferences(Context c,String key,String data){
        init(c).putString(key, data).commit();
    }
    MyPreferences(Context c,String key,int data){
        init(c).putInt(key, data).commit();
    }
    MyPreferences(Context c,String key,boolean data){
        init(c).putBoolean(key, data).commit();
    }
    MyPreferences(){
    }
    public int getInt(Context c,String key){
        return c.getSharedPreferences("pref",c.MODE_PRIVATE).getInt(key,-1);
    }
    public String getString(Context c,String key){
        return c.getSharedPreferences("pref",c.MODE_PRIVATE).getString(key,"-1");
    }
    public boolean getboolen(Context c,String key){
        return c.getSharedPreferences("pref",c.MODE_PRIVATE).getBoolean(key,false);
    }

    private SharedPreferences.Editor init(Context c){
        SharedPreferences pref = c.getSharedPreferences("pref",c.MODE_PRIVATE);
        SharedPreferences.Editor e = pref.edit();
        return e;
    }

}
