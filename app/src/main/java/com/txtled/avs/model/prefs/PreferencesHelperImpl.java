package com.txtled.avs.model.prefs;

import android.content.Context;
import android.content.SharedPreferences;


import com.txtled.avs.application.MyApplication;

import javax.inject.Inject;

/**
 * Created by Mr.Quan on 2018/4/17.
 */

public class PreferencesHelperImpl implements PreferencesHelper {
    private static final String SP_NAME = "my_sp";
    private SharedPreferences mSharedPreferences;
    public static final String PLAY_POSITION = "play_position";
    public static final String IS_FIRST_APP = "is_first_app";

    @Inject
    public PreferencesHelperImpl() {
        mSharedPreferences = MyApplication.getInstance().getSharedPreferences(SP_NAME, Context.
                MODE_PRIVATE);
    }

    @Override
    public int getPlayPosition() {
        return mSharedPreferences.getInt(PLAY_POSITION, 0);
    }

    @Override
    public void setPlayPosition(int position) {
        mSharedPreferences.edit().putInt(PLAY_POSITION, position).apply();
    }

    @Override
    public boolean isFirstIn() {
        return mSharedPreferences.getBoolean(IS_FIRST_APP,true);
    }

    @Override
    public void setFirstIn(boolean first) {
        mSharedPreferences.edit().putBoolean(IS_FIRST_APP,first).apply();
    }

//    @Override
//    public void setIsFirstApp(boolean b) {
//        mSharedPreferences.edit().putBoolean(IS_FIRST_APP,b).apply();
//    }
//
//    @Override
//    public boolean getIsFirstApp() {
//        return mSharedPreferences.getBoolean(IS_FIRST_APP,true);
//    }
//
//    @Override
//    public void setMainVolume(int progress) {
//        mSharedPreferences.edit().putInt(MAIN_VOLUME,progress).apply();
//    }
//
//    @Override
//    public int getMainVolume() {
//        return mSharedPreferences.getInt(MAIN_VOLUME,0);
//    }
//
//    @Override
//    public void setInitDialog(boolean b) {
//
//    }
//
//    @Override
//    public boolean getInitDialog() {
//        return false;
//    }
//
//    @Override
//    public boolean getIsCycle() {
//        return mSharedPreferences.getBoolean(IS_CYCLE,true);
//    }
//
//    @Override
//    public void setIsCycle(boolean b) {
//        mSharedPreferences.edit().putBoolean(IS_CYCLE,b).apply();
//    }
//
//    @Override
//    public boolean getIsRandom() {
//        return mSharedPreferences.getBoolean(IS_RANDOM,false);
//    }
//
//    @Override
//    public void setIsRandom(boolean b) {
//        mSharedPreferences.edit().putBoolean(IS_RANDOM,b).apply();
//    }
}
