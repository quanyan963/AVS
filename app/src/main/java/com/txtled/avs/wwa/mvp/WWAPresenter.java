package com.txtled.avs.wwa.mvp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.txtled.avs.R;
import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.model.DataManagerModel;
import com.txtled.avs.model.operate.OperateHelper;
import com.txtled.avs.utils.Constants;
import com.txtled.avs.wwa.WWAFragment;

import javax.inject.Inject;

import static android.content.Context.WIFI_SERVICE;
import static com.inuker.bluetooth.library.utils.BluetoothUtils.registerReceiver;
import static com.inuker.bluetooth.library.utils.BluetoothUtils.unregisterReceiver;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public class WWAPresenter extends RxPresenter<WWAContract.View> implements WWAContract.Presenter {
    private DataManagerModel mDataManagerModel;
    private static final String TAG = WWAFragment.class.getSimpleName();
    private boolean mReceiverRegistered = false;
    private Context context;
    private boolean mDestroyed = false;
    private WWAFragment.EsptouchAsyncTask4 mTask;

    @Inject
    public WWAPresenter(DataManagerModel mDataManagerModel) {
        this.mDataManagerModel = mDataManagerModel;
    }

    @Override
    public void checkPermission(Activity activity) {
        String[] permissions = {Constants.permissions[0],Constants.permissions[1]};
        mDataManagerModel.requestPermissions(activity, permissions, new OperateHelper.OnPermissionsListener() {
            @Override
            public void onSuccess(String name) {
                if (name.equals(Constants.permissions[1])){
                    //scanBle(false);
                    view.registerBroadcastReceiver();
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
    public void onViewClick(int id) {
        switch (id){
            case R.id.confirm_btn:
                view.confirm();
                break;
            case R.id.tv_register:
                view.register();
                break;
            default:
                view.hidSnackBar();
                break;
        }
    }

    private boolean isSDKAtLeastP() {
        return Build.VERSION.SDK_INT >= 28;
    }

    @Override
    public void registerBroadcast() {
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (isSDKAtLeastP()) {
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        }
        registerReceiver(mReceiver, filter);
        mReceiverRegistered = true;
    }

    @Override
    public void init(Context context) {
        this.context = context;
    }

    @Override
    public void destroy() {
        mDestroyed = true;
        if (mReceiverRegistered) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void executeTask(WWAFragment wwaFragment, byte[] ssid, byte[] bssid, byte[] password, byte[] deviceCount, byte[] broadcast) {
        if (mTask != null) {
            mTask.cancelEsptouch();
        }
        mTask = new WWAFragment.EsptouchAsyncTask4(wwaFragment);
        mTask.execute(ssid, bssid, password, deviceCount, broadcast);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }

            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(WIFI_SERVICE);
            assert wifiManager != null;

            switch (action) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                case LocationManager.PROVIDERS_CHANGED_ACTION:
                    onWifiChanged(wifiManager.getConnectionInfo());
                    break;
            }
        }
    };

    private void onWifiChanged(WifiInfo info) {
        boolean disconnected = info == null
                || info.getNetworkId() == -1
                || "<unknown ssid>".equals(info.getSSID());
        if (disconnected) {
            view.setNoWifiView();
            if (isSDKAtLeastP()) {
                view.checkLocation();
            }

            if (mTask != null) {
                mTask.cancelEsptouch();
                mTask = null;
                view.showAlertDialog(R.string.configure_wifi_change_message,android.R.string.cancel,null);
            }
        } else {
            String ssid = info.getSSID();
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            view.setInfo(ssid,info);
        }
    }
}
