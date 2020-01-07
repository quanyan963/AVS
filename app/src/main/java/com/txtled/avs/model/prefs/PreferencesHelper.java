package com.txtled.avs.model.prefs;

/**
 * Created by Mr.Quan on 2018/4/17.
 */

public interface PreferencesHelper {

    boolean isConfigured();

    void setIsConfigured(boolean configured);

    void setUserId(String userId);

    String getUserId();

    void setUid(String uid);

    String getUid();
}
