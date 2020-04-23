package com.txtled.avs.config.mvp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.txtled.avs.R;
import com.txtled.avs.avs.amazonlogin.ProvisioningClient;
import com.txtled.avs.base.CommonSubscriber;
import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.mDNS.Mdnser;
import com.txtled.avs.model.DataManagerModel;
import com.txtled.avs.model.operate.OperateHelper;
import com.txtled.avs.utils.Constants;
import com.txtled.avs.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.WIFI_SERVICE;
import static com.txtled.avs.base.BaseFragment.TAG;
import static com.txtled.avs.utils.Constants.GET_AUTH;
import static com.txtled.avs.utils.Constants.GET_PSK;
import static com.txtled.avs.utils.Constants.GET_SSID;
import static com.txtled.avs.utils.Constants.RESET_IP;
import static com.txtled.avs.utils.Constants.SERVICE_TYPE;
import static com.txtled.avs.utils.Constants.WIFI_NAME;
import static com.txtled.avs.utils.Constants.WIFI_NAME_OLD;

/**
 * Created by Mr.Quan on 2020/4/16.
 */
public class WifiPresenter extends RxPresenter<WifiContract.View> implements WifiContract.Presenter {

    private DataManagerModel dataManagerModel;
    private Activity activity;
    private LocationManager locationManager;
    private Mdnser mdnser;
    private String address, readStr;
    private ProvisioningClient mProvisioningClient;
    private Socket socket;
    private OutputStream outStr = null;
    private InputStream inStr = null;
    private Disposable readDisposable;
    private Disposable writeDisposable;
    private Disposable timerDisposable;
    private Disposable loginDisposable;
    private int count;
    private boolean isReset;

    @Inject
    public WifiPresenter(DataManagerModel dataManagerModel) {
        this.dataManagerModel = dataManagerModel;
    }

    @Override
    public void init(Activity activity) {
        this.activity = activity;

        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (Build.VERSION.SDK_INT == 28) {
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        }
        //registerReceiver(mReceiver, filter);

        locationManager = (LocationManager) activity
                .getSystemService(Context.LOCATION_SERVICE);

        mdnser = new Mdnser(activity);
        mdnser.initializeDiscoveryListener();

        try {
            mProvisioningClient = new ProvisioningClient(activity);
        } catch (Exception e) {
            //view.showAlertDialog(e);
            //Log.e(TAG, "Unable to use Provisioning Client. CA Certificate is incorrect or does not exist.", e);
        }
    }

    /**
     * 搜索设备
     */
    @Override
    public void findDevice() {
        count = count + 1;
        mdnser = new Mdnser(activity);
        mdnser.initializeDiscoveryListener();
        addSubscribe(Flowable.create((FlowableOnSubscribe<Mdnser>) e -> {

            try {
                mdnser.initializeDiscoveryListener();
                mdnser.mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD,
                        mdnser.mDiscoveryListener);
                Thread.sleep(1500);
                e.onNext(mdnser);
            } catch (Exception e1) {
                findDevice();
            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .subscribeWith(new CommonSubscriber<Mdnser>(view) {

                    @Override
                    public void onNext(Mdnser mdnser) {
                        try {
                            mdnser.mNsdManager.stopServiceDiscovery(mdnser.mDiscoveryListener);
                            if (mdnser.ipInfos != null && mdnser.ipInfos.size() != 0) {
                                //view.showWeb();
                                initSocket();
                                count = 0;
                            } else {
                                if (count < 3) {
                                    findDevice();
                                } else {
                                    count = 0;
                                    view.notFound();
                                }
                            }
                        } catch (Exception e) {
                            if (count < 3) {
                                findDevice();
                            } else {
                                count = 0;
                                view.notFound();
                            }
                        }

                    }
                }));
    }

    /**
     * 检查wifi状态
     */
    @Override
    public void checkState() {
        WifiManager wifiManager = (WifiManager) activity.getApplicationContext()
                .getSystemService(WIFI_SERVICE);
        onWifiChanged(wifiManager.getConnectionInfo());
    }

    /**
     * 防oom
     */
    @Override
    public void destroy() {
        if (activity != null) {
            activity = null;
        }
        if (socket != null) {
            try {
                inStr.close();
                outStr.close();
                inStr = null;
                outStr = null;
                socket.close();
            } catch (IOException e) {
                inStr = null;
                outStr = null;
                //e.printStackTrace();
            }
            socket = null;
        }
        if (readDisposable != null) {
            readDisposable.dispose();
            readDisposable = null;
        }
        if (writeDisposable != null) {
            writeDisposable.dispose();
            writeDisposable = null;
        }
        if (timerDisposable != null) {
            timerDisposable.dispose();
            timerDisposable = null;
        }
        if (loginDisposable != null) {
            loginDisposable.dispose();
            loginDisposable = null;
        }
        //unregisterReceiver(mReceiver);
        if (mdnser != null) {
            mdnser = null;
        }
        if (locationManager != null) {
            locationManager = null;
        }
    }

    /**
     * 发送重置ip指令
     */
    @Override
    public void senResetIp() {
        connSocket(RESET_IP);
    }

    /**
     * 初始化socket
     */
    private void initSocket() {
        address = mdnser.ipInfos.get(0).getHostip();
        final String url = "http://" + address;

        mProvisioningClient.setEndpoint(url);
        readStr = "";

        if (socket != null) {
            try {
//                readDisposable.dispose();
//                writeDisposable.dispose();
//                timerDisposable.dispose();
//                loginDisposable.dispose();
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket = new Socket();
        connSocket(GET_AUTH);
    }

    /**
     * 连接socket并发送数据
     *
     * @param sendMsg 要发送的数据
     */
    private void connSocket(String sendMsg) {
        loginDisposable = Flowable.create((FlowableOnSubscribe<Socket>) e -> {
            try {
                if (!socket.isConnected()) {
                    socket.connect(new InetSocketAddress(address, 9000), 2000);
                    inStr = socket.getInputStream();
                    outStr = socket.getOutputStream();
                }
                e.onNext(socket);
            } catch (IOException e1) {
                socket.close();
                socket = null;
                socket = new Socket();
                connSocket(sendMsg);
            }
        }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .subscribeWith(new CommonSubscriber<Socket>(view) {
                    @Override
                    public void onNext(Socket socket) {
                        readSocket();
                        startTime();
                        sendSocket(sendMsg);
                    }
                });
    }

    /**
     * socket发送数据
     *
     * @param code 内容
     */
    private void sendSocket(String code) {
        writeDisposable = Observable.just(socket).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).subscribe(socket -> {
                    try {
                        outStr.write(code.getBytes());
                    } catch (Exception e) {
                        if (!writeDisposable.isDisposed()) {
                            writeDisposable.dispose();
                            outStr.close();
                            socket.close();
                            socket = new Socket();
                            //socket.connect(new InetSocketAddress(address, 9000), 3000);
                            sendSocket(code);
                        }
                    }
                });
    }

    /**
     * 发送数据后计时，没收到数据则提示
     */
    private void startTime() {

        timerDisposable = Observable.timer(3, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(time -> {
                    if (readStr == null || readStr.isEmpty()) {
                        view.showToast(R.string.not_responding);
                        //readDisposable.dispose();
                        //writeDisposable.dispose();
                    } else {
                        if (!readStr.contains(RESET_IP) && !readStr.contains(GET_SSID) &&
                                !readStr.contains(GET_PSK))
                            view.showWeb();
                        readStr = null;
                    }
                });
    }

    /**
     * 读客户端（AVS）发来的消息
     */
    private void readSocket() {
        readDisposable = Observable.interval(1, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).subscribe(time -> {
                    try {
                        final byte[] b = new byte[1024];
                        int length = inStr.read(b);
                        if (length > 0) {
                            readStr = new String(b).trim();
                            Utils.Logger(TAG, "Read:", readStr);
                            if (readStr.contains("auth") && readStr.contains("ip")) {
                                //有时一条数据里有两个信息
                                //readDisposable.dispose();
                                //socket.close();
                                //view.toBindView();
                                String[] data = readStr.split("auth=");
                                boolean isAuth;
                                String ip;
                                if (data[0].isEmpty()) {
                                    isAuth = data[1].substring(0, 1).equals("1") ? true : false;
                                    ip = data[1].split("ip=")[1];
                                } else {
                                    isAuth = data[1].equals("1") ? true : false;
                                    ip = data[0].split("ip=")[1];
                                }
                                view.getAuth(isAuth);
                                view.getIp(ip);
                                //view.toBindView();
                            } else if (readStr.contains("auth")) {
                                //readDisposable.dispose();
                                //socket.close();
                                view.getAuth(readStr.substring(readStr.length() - 1).equals("1") ? true : false);
                                //temp.execute();
                            } else if (readStr.contains("ip")) {
                                String ip = readStr.split("=")[1];
                                view.getIp(ip);
                                if (isReset)
                                    sendSocket(GET_SSID);
                                //temp.execute();
                            } else if (readStr.contains(RESET_IP)) {
                                isReset = true;
                                view.webVisible();
                                //readDisposable.dispose();
//                                view.showToast(R.string.not_responding);
//                                Utils.Logger(TAG, "Other:", readStr);
                            } else if (readStr.contains(GET_SSID)) {
                                String ssid = readStr.split("=")[1];
                                view.getSsId(ssid);
                                sendSocket(GET_PSK);
                            } else if (readStr.contains(GET_PSK)) {
                                String psk = readStr.split("=")[1];
                                readDisposable.dispose();
                                socket.close();
                                view.getPsk(psk);
                                if (isReset) {
                                    view.toBindView();
                                }
                            } else {

                            }
                        }
                    } catch (Exception e) {
                        if (readDisposable != null) {
                            if (!readDisposable.isDisposed()) {
                                readDisposable.dispose();
                                inStr.close();
                                socket.close();
                                socket = null;
                                socket = new Socket();
                                //socket.connect(new InetSocketAddress(address, 9000), 3000);
//                                if (socket.isConnected()){
//                                    readSocket();
//                                }
                            }
                        }

                    }
                });
    }

    //目前弃用
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

    /**
     * 检查wifi状态 是否是GP_A211_****的网络
     *
     * @param connectionInfo wifi信息
     */
    private void onWifiChanged(WifiInfo connectionInfo) {
        boolean disconnected = connectionInfo == null
                || connectionInfo.getNetworkId() == -1
                || "<unknown ssid>".equals(connectionInfo.getSSID());
        if (disconnected) {
            String[] permissions = {Constants.permissions[0], Constants.permissions[1]};
            dataManagerModel.requestPermissions(activity, permissions,
                    new OperateHelper.OnPermissionsListener() {
                        @Override
                        public void onSuccess(String name) {
                            if (name.equals(Constants.permissions[1])) {

                                assert locationManager != null;
                                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    view.showSnack(R.string.location_unavailable);
                                } else {
                                    view.showNetWorkError(R.string.avs_wifi_available);
//                                    if (avsSwitch){
//                                        view.showNetWorkError(R.string.net_unavailable);
//                                    }else {
//                                        view.showNetWorkError(R.string.avs_wifi_available);
//                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure() {
                            view.showPermissionHint();
                        }

                        @Override
                        public void onAskAgain() {

                        }
                    });

        } else {
            if (connectionInfo.getSSID().contains(WIFI_NAME) ||
                    connectionInfo.getSSID().contains(WIFI_NAME_OLD)) {
                view.rightWifi();
            } else {
                view.showNetWorkError(R.string.avs_wifi_available);
            }
        }
    }
}
