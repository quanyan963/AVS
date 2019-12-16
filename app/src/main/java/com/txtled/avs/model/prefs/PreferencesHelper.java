package com.txtled.avs.model.prefs;

/**
 * Created by Mr.Quan on 2018/4/17.
 */

public interface PreferencesHelper {
    int getPlayPosition();

    void setPlayPosition(int position);

    boolean isFirstIn();

    void setFirstIn(boolean first);
}
