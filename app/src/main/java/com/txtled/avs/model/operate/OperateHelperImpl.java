package com.txtled.avs.model.operate;

import android.app.Activity;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

/**
 * Created by Mr.Quan on 2019/3/6.
 */

public class OperateHelperImpl implements OperateHelper {

    @Inject
    public OperateHelperImpl() {
    }

    @Override
    public void requestPermissions(Activity activity, String[] permissions,
                                   final OnPermissionsListener permissionsListener) {
        RxPermissions rxPermission = new RxPermissions(activity);
        rxPermission.requestEach(permissions)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            permissionsListener.onSuccess(permission.name);
                            //Log.d(TAG, permission.name + " is granted.");
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            permissionsListener.onAskAgain();
                            //Log.d(TAG, permission.name + " is denied. More info should be provided.");
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            permissionsListener.onFailure();
                            //Log.d(TAG, permission.name + " is denied.");
                        }
                    }
                });
    }
}
