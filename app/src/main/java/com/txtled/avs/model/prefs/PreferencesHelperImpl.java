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
    public static final String IS_CONFIGURED = "is_configured";

    @Inject
    public PreferencesHelperImpl() {
        mSharedPreferences = MyApplication.getInstance().getSharedPreferences(SP_NAME, Context.
                MODE_PRIVATE);
    }

    @Override
    public boolean isConfigured() {
        return mSharedPreferences.getBoolean(IS_CONFIGURED,false);
    }

    @Override
    public void setIsConfigured(boolean configured) {
        mSharedPreferences.edit().putBoolean(IS_CONFIGURED,configured).apply();
    }
}
