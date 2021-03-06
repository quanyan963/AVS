package com.txtled.avs.model;


import android.app.Activity;


import com.txtled.avs.bean.WWADeviceInfo;
import com.txtled.avs.model.ble.BleHelper;
import com.txtled.avs.model.db.DBHelper;
import com.txtled.avs.model.net.NetHelper;
import com.txtled.avs.model.operate.OperateHelper;
import com.txtled.avs.model.prefs.PreferencesHelper;

import java.util.List;

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

    @Override
    public void setUserId(String userId) {
        mPreferencesHelper.setUserId(userId);
    }

    @Override
    public String getUserId() {
        return mPreferencesHelper.getUserId();
    }

    @Override
    public void setUid(String uid) {
        mPreferencesHelper.setUid(uid);
    }

    @Override
    public String getUid() {
        return mPreferencesHelper.getUid();
    }

//    @Override
//    public void insertWWAInfo(List<WWADeviceInfo> infoList) {
//        mDBDbHelper.insertWWAInfo(infoList);
//    }
//
//    @Override
//    public List<WWADeviceInfo> getWWAInfo() {
//        return mDBDbHelper.getWWAInfo();
//    }
//
//    @Override
//    public void deleteWWAInfo() {
//        mDBDbHelper.deleteWWAInfo();
//    }
//
//    @Override
//    public void upDataWWAInfo(WWADeviceInfo info) {
//        mDBDbHelper.upDataWWAInfo(info);
//    }
}
