package com.txtled.avs.main.mvp;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.wifi.WifiManager;

import com.txtled.avs.R;
import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.model.DataManagerModel;
import com.txtled.avs.model.operate.OperateHelper;
import com.txtled.avs.utils.Constants;
import com.txtled.avs.utils.Utils;

import javax.inject.Inject;

/**
 * Created by Mr.Quan on 2019/12/9.
 */
public class MainPresenter extends RxPresenter<MainContract.View> implements MainContract.Presenter {

    private DataManagerModel mDataManagerModel;

    @Inject
    public MainPresenter(DataManagerModel mDataManagerModel) {
        this.mDataManagerModel = mDataManagerModel;
    }


    @Override
    public void getWifiSSid(final Activity activity) {
        final String[] str = {Constants.permissions[0]};
        mDataManagerModel.requestPermissions(activity, str, new OperateHelper.OnPermissionsListener() {
            @Override
            public void onSuccess(String name) {
                if (name.equals(str[0])) {
                    LocationManager locationManager = (LocationManager) activity
                            .getSystemService(Context.LOCATION_SERVICE);
                    boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                    boolean network = ((WifiManager) activity.getApplicationContext()
                            .getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();

                    if (!gps) {
                        view.openLocation();
                    } else if (!network) {
                        view.openWifi();
                    } else {
                        if (Utils.getWifiSSID(activity).contains(Constants.WIFI_NAME)) {
                            view.toWebView();
                        } else {
                            view.networkNoteMatch();
                        }

                    }

                }
            }

            @Override
            public void onFailure() {

            }

            @Override
            public void onAskAgain() {

            }
        });
    }

    @Override
    public void switchFragment(int i) {
        switch (i) {
            case R.id.rb_main_avs:
                view.toAVS();
                break;
            case R.id.rb_main_wwa:
                view.toWWA();
                break;
        }
    }
}
