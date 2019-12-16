package com.txtled.avs.model;


import android.app.Activity;
import android.content.Context;


import com.txtled.avs.model.ble.BleHelper;
import com.txtled.avs.model.db.DBHelper;
import com.txtled.avs.model.net.NetHelper;
import com.txtled.avs.model.operate.OperateHelper;
import com.txtled.avs.model.prefs.PreferencesHelper;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by Mr.Quan on 2018/4/17.
 */

public class DataManagerModel implements DBHelper, PreferencesHelper, NetHelper, OperateHelper, BleHelper {
    private DBHelper mDBDbHelper;
    private PreferencesHelper mPreferencesHelper;
    private NetHelper mNetHelper;
    private OperateHelper mOperateHelper;
    private BleHelper mBleHelper;

    public DataManagerModel(DBHelper mDBDbHelper, PreferencesHelper
            mPreferencesHelper, NetHelper mNetHelper, OperateHelper mOperateHelper, BleHelper mBleHelper) {
        this.mDBDbHelper = mDBDbHelper;
        this.mPreferencesHelper = mPreferencesHelper;
        this.mNetHelper = mNetHelper;
        this.mOperateHelper = mOperateHelper;
        this.mBleHelper = mBleHelper;
    }

    @Override
    public int getPlayPosition() {
        return 0;
    }

    @Override
    public void setPlayPosition(int position) {

    }

    @Override
    public boolean isFirstIn() {
        return mPreferencesHelper.isFirstIn();
    }

    @Override
    public void setFirstIn(boolean first) {
        mPreferencesHelper.setFirstIn(first);
    }

    @Override
    public void requestPermissions(Activity activity, String[] permissions, OnPermissionsListener permissionsListener) {
        mOperateHelper.requestPermissions(activity, permissions, permissionsListener);
    }

    @Override
    public void scanBle(Activity activity, boolean isSpecified, OnScanBleListener onScanBleListener, OnConnBleListener onConnBleListener) {

    }

    @Override
    public void connBle(OnConnBleListener onConnBleListener) {

    }

    @Override
    public void writeCommand(String command) {

    }

    @Override
    public void notifyBle(OnReadListener readListener) {

    }

    @Override
    public void readCommand(OnReadListener readListener) {

    }

    @Override
    public void unRegisterConn() {

    }
}
