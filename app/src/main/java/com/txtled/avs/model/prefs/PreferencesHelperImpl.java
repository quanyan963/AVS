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
    public static final String UID = "uid";

    @Inject
    public PreferencesHelperImpl() {
        mSharedPreferences = MyApplication.getInstance().getSharedPreferences(SP_NAME, Context.
                MODE_PRIVATE);
    }

    @Override
    public void setUserId(String userId) {
        mSharedPreferences.edit().putString(USER_ID,userId).apply();
    }

    @Override
    public String getUserId() {
        return mSharedPreferences.getString(USER_ID,"");
    }

    @Override
    public void setUid(String uid) {
        mSharedPreferences.edit().putString(UID,uid).apply();
    }

    @Override
    public String getUid() {
        return mSharedPreferences.getString(UID,"");
    }
}
