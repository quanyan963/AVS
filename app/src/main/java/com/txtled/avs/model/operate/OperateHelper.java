package com.txtled.avs.model.operate;

import android.app.Activity;

/**
 * Created by Mr.Quan on 2019/3/6.
 */

public interface OperateHelper {
    void requestPermissions(Activity activity, String[] permissions, OnPermissionsListener
            permissionsListener);

    interface OnPermissionsListener {
        void onSuccess(String name);

        void onFailure();

        void onAskAgain();
    }
}
