package com.txtled.avs.wwa.mvp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;

import androidx.annotation.Nullable;

import com.amazonaws.services.iot.AWSIot;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.txtled.avs.base.BasePresenter;
import com.txtled.avs.base.BaseView;
import com.txtled.avs.bean.WWADeviceInfo;
import com.txtled.avs.wwa.WWAFragment;
import com.txtled.avs.wwa.listener.OnCreateThingListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public interface WWAContract {
    interface View extends BaseView{
        void registerBroadcastReceiver();

        void confirm();

        void register();

        void setNoWifiView();

        void showAlertDialog(int configure_wifi_change_message, int cancel, @Nullable DialogInterface.OnClickListener listener);

        void setInfo(String ssid, WifiInfo info);

        void checkLocation();

        void hidSnackBar();

        void setData(ArrayList<WWADeviceInfo> strReceive);

        void closeRefresh();

        void createSuccess();

        void setUserId(String value);

        void updateAdapter(int position, ArrayList<WWADeviceInfo> newNames);

        void showLoadingView();

        void hidLoadingView();
    }
    interface Presenter extends BasePresenter<View>{

        void checkPermission(Activity activity);

        void onViewClick(int id);

        void registerBroadcast();

        void init(Activity context);

        void destroy();

        void executeTask(WWAFragment wwaFragment, byte[] ssid, byte[] bssid, byte[] password, byte[] deviceCount, byte[] broadcast);

        void onRefresh();

        void hasData();

        AWSIot getAmazonIotService();

        void createThing(int position, String friendlyName, OnCreateThingListener listener);

        boolean checkUserHasLogin();

        void onResume(boolean isShowing);

        String geUserId();
    }
}
