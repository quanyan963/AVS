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
    public static final String USER_ID = "user_id";

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

    @Override
    public void setUserId(String userId) {
        mSharedPreferences.edit().putString(USER_ID,userId).apply();
    }

    @Override
    public String getUserId() {
        return mSharedPreferences.getString(USER_ID,"");
    }
}
