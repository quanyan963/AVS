package com.txtled.avs.bind.mvp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.nsd.NsdManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Base64;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ProfileScope;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.amazon.identity.auth.device.api.authorization.ScopeFactory;
import com.amazon.identity.auth.device.api.workflow.RequestContext;
import com.txtled.avs.R;
import com.txtled.avs.avs.amazonlogin.CompanionProvisioningInfo;
import com.txtled.avs.avs.amazonlogin.DeviceProvisioningInfo;
import com.txtled.avs.avs.amazonlogin.ProvisioningClient;
import com.txtled.avs.base.CommonSubscriber;
import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.mDNS.Mdnser;
import com.txtled.avs.main.MainActivity;
import com.txtled.avs.model.DataManagerModel;
import com.txtled.avs.model.operate.OperateHelper;
import com.txtled.avs.utils.Constants;
import com.txtled.avs.utils.RxUtil;
import com.txtled.avs.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.WIFI_SERVICE;
import static com.txtled.avs.base.BaseFragment.TAG;
import static com.txtled.avs.utils.Constants.ALEXA_ALL_SCOPE;
import static com.txtled.avs.utils.Constants.DEVICE_SERIAL_NUMBER;
import static com.txtled.avs.utils.Constants.GET_CODE;
import static com.txtled.avs.utils.Constants.PRODUCT_ID;
import static com.txtled.avs.utils.Constants.PRODUCT_INSTANCE_ATTRIBUTES;
import static com.txtled.avs.utils.Constants.RESET_DEVICE;
import static com.txtled.avs.utils.Constants.SERVICE_TYPE;
import static com.txtled.avs.utils.Constants.WIFI_NAME;
import static com.txtled.avs.utils.Constants.WIFI_NAME_OLD;

/**
 * Created by Mr.Quan on 2020/4/16.
 */
public class BindPresenter extends RxPresenter<BindContract.View> implements BindContract.Presenter {
    private DataManagerModel dataManagerModel;
    private Activity activity;
    private LocationManager locationManager;
    private Mdnser mdnser;
    private String address, readStr;
    private ProvisioningClient mProvisioningClient;
    private Socket socket;
    private String mCode,ip;
    private OutputStream outStr = null;
    private InputStream inStr = null;
    private Disposable readDisposable;
    private Disposable writeDisposable;
    private Disposable timerDisposable;
    private Disposable loginDisposable;
    private RequestContext mRequestContext;
    private AuthorizeListenerImpl authorizeListener;
    private DeviceProvisioningInfo mDeviceProvisioningInfo;
    private int count;
    private boolean hasResult;
    boolean hasCanResult,hasConn;

    @Inject
    public BindPresenter(DataManagerModel dataManagerModel) {
        this.dataManagerModel = dataManagerModel;
    }

    @Override
    public void init(Activity activity, String ip) {
        this.activity = activity;
        this.ip = ip;
        /**********AMZON LOGIN ********************************/

        mRequestContext = RequestContext.create(activity);
        authorizeListener = new AuthorizeListenerImpl();
        mRequestContext.registerListener(authorizeListener);

        try {
            mProvisioningClient = new ProvisioningClient(activity);
        } catch (Exception e) {
            //view.showAlertDialog(e);
            //Log.e(TAG, "Unable to use Provisioning Client. CA Certificate is incorrect or does not exist.", e);
        }

        /******************************************************/
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        if (Build.VERSION.SDK_INT == 28) {
//            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
//        }
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
     * 搜索当前网路下的设备
     */
    @Override
    public void findDevice() {
        count  = count + 1;
        mdnser = new Mdnser(activity);
        mdnser.initializeDiscoveryListener();
        addSubscribe(Flowable.create((FlowableOnSubscribe<Mdnser>) e -> {
            mdnser.initializeDiscoveryListener();
            mdnser.mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD,
                    mdnser.mDiscoveryListener);
            Thread.sleep(2000);
            e.onNext(mdnser);
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .subscribeWith(new CommonSubscriber<Mdnser>(view) {

                    @Override
                    public void onNext(Mdnser mdnser) {
                        try {
                            mdnser.mNsdManager.stopServiceDiscovery(mdnser.mDiscoveryListener);
                            if (mdnser.ipInfos != null && mdnser.ipInfos.size() != 0) {
                                for (int i = 0; i < mdnser.ipInfos.size(); i++) {
                                    if (mdnser.ipInfos.get(i).getHostip().equals(ip)){
                                        count = 0;
                                        //view.find();
                                        initSocket();
                                        return;
                                    }
                                }
                            }
                            if (count < 3){
                                findDevice();
                            }else {
                                count = 0;
                                view.timeOut();
                            }
                        }catch (Exception e){
                            if (count < 3){
                                findDevice();
                            }else {
                                count = 0;
                                view.timeOut();
                            }
                        }

                    }
                }));
    }

    @Override
    public void onDestroy() {
        if (activity != null) {
            activity = null;
        }
        if (socket != null) {
            try {
                outStr.close();
                inStr.close();
                outStr = null;
                inStr = null;
                socket.close();
            } catch (IOException e) {
                outStr = null;
                inStr = null;
            }
            socket = null;
        }
        if (readDisposable != null){
            readDisposable.dispose();
            readDisposable = null;
        }
        if (writeDisposable != null){
            writeDisposable.dispose();
            writeDisposable = null;
        }
        if (timerDisposable != null){
            timerDisposable.dispose();
            timerDisposable = null;
        }
        if (loginDisposable != null){
            loginDisposable.dispose();
            loginDisposable = null;
        }
        //unregisterReceiver(mReceiver);
        if (mdnser != null){
            mdnser = null;
        }
        if (locationManager != null){
            locationManager = null;
        }
    }

    private void initSocket() {
        address = ip;
        final String url = "http://" + address;

        mProvisioningClient.setEndpoint(url);
        readStr = "";

        if (socket != null) {
            try {
                readDisposable.dispose();
                writeDisposable.dispose();
                inStr.close();
                outStr.close();
//                timerDisposable.dispose();
//                loginDisposable.dispose();
                socket.close();
                socket = null;
            } catch (IOException e) {
                socket = null;
            }
        }
        socket = new Socket();
        connSocket(RESET_DEVICE);
    }

    @Override
    public void connSocket(String sendMsg) {
        loginDisposable = Flowable.create((FlowableOnSubscribe<Socket>) e -> {
            try {
                if (!socket.isConnected()){
                    socket.connect(new InetSocketAddress(address, 9000), 2000);
                    inStr = socket.getInputStream();
                    outStr = socket.getOutputStream();
                    readSocket();
                }
                e.onNext(socket);
            } catch (IOException e1) {
                socket.close();
                socket = null;
                socket = new Socket();
                connSocket(sendMsg);
            }
        }, BackpressureStrategy.LATEST).compose(RxUtil.rxSchedulerHelper())
                .subscribeWith(new CommonSubscriber<Socket>(view) {
                    @Override
                    public void onNext(Socket socket) {
                        startTime();
                        sendSocket(sendMsg);
                    }
                });
    }

    @Override
    public void checkState() {
        WifiManager wifiManager = (WifiManager) activity.getApplicationContext()
                .getSystemService(WIFI_SERVICE);
        onWifiChanged(wifiManager.getConnectionInfo());
    }

    @Override
    public void resume() {
        mRequestContext.onResume();
    }

    /**
     * 从亚马逊登录返回后计时
     */
    @Override
    public void startCount() {
        addSubscribe(Flowable.timer(8,TimeUnit.SECONDS)
                .compose(RxUtil.rxSchedulerHelper())
                .subscribeWith(new CommonSubscriber<Long>(view){

                    @Override
                    public void onNext(Long aLong) {
                        if (!hasResult){
                            view.noResult();
                            mRequestContext.unregisterListener(authorizeListener);
                            mRequestContext.registerListener(authorizeListener);
                        }else {
                            hasResult = false;
                        }
                    }
                }));
    }

    /**
     * 自动更改wifi
     * @param ssId
     * @param psk
     */
    @Override
    public void changeWifi(String ssId, String psk) {

        WifiManager manager = (WifiManager) activity.getApplicationContext().getSystemService(WIFI_SERVICE);

        addSubscribe(Flowable.create((FlowableOnSubscribe<String>) e -> {
            List<ScanResult> results = manager.getScanResults();
            int id = -1;
            List<WifiConfiguration> mWificonfiguration = manager.getConfiguredNetworks();
            ScanResult result = null;
            for (int i = 0; i < results.size(); i++) {
                if (results.get(i).SSID.equals(ssId)){
                    hasCanResult = true;
                    result = results.get(i);
                    break;
                }else {
                    hasCanResult = false;
                }
            }
            for (int i = 0; i < mWificonfiguration.size(); i++) {
                if (mWificonfiguration.get(i).SSID.replace("\"","").equals(ssId)){
                    hasConn = true;
                    id = mWificonfiguration.get(i).networkId;
                    break;
                }else {
                    hasConn = false;
                }
            }
            if (hasCanResult && hasConn){
                manager.enableNetwork(id,true);
                e.onNext("success");
            }else if (!hasCanResult){
                e.onNext("scanFail");
            }else if (hasCanResult && !hasConn){
                manager.addNetwork(createWifiInfo(result.SSID,deCode(psk),
                        getCipherType(result.capabilities)));
                //manager.enableNetwork(netId, true);
                e.onNext("success");
            }
        },BackpressureStrategy.BUFFER)
                .compose(RxUtil.rxSchedulerHelper())
                .doOnSubscribe(subscription -> manager.startScan())
                .subscribeWith(new CommonSubscriber<String>(view){

                    @Override
                    public void onNext(String s) {
                        if (s.equals("success")){
                            view.showToast(activity.getString(R.string.auto_change)+ssId);
                        }else {
                            //view.showToast("");
                        }
                    }
                }));
    }

    /**
     * 判断wifi的加密形式
     * @param capabilities
     * @return
     */
    private int getCipherType(String capabilities){
        if (capabilities.contains("WEB")) {
            return 2;
        } else if (capabilities.contains("PSK")) {
            return 3;
        } else if (capabilities.contains("WPS")) {
            return 1;
        } else {
            return 1;
        }
    }

    /**
     * 配置wifi参数
     * @param SSID wifi名称
     * @param Password wifi密码
     * @param Type 加密类型
     * @return WifiConfiguration
     */
    private WifiConfiguration createWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.allowedAuthAlgorithms.clear();
        configuration.allowedGroupCiphers.clear();
        configuration.allowedKeyManagement.clear();
        configuration.allowedPairwiseCiphers.clear();
        configuration.allowedProtocols.clear();
        configuration.SSID = "\"" + SSID + "\"";

        switch (Type) {
            case 1://不加密
                configuration.wepKeys[0] = "";
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                configuration.wepTxKeyIndex = 0;
                configuration.priority= 20000;
                break;
            case 2://wep加密
                configuration.hiddenSSID = true;
                configuration.wepKeys[0] = "\"" + Password +"\"";
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                break;
            case 3: //wpa加密

                configuration.preSharedKey = "\"" + Password + "\"";
                configuration.hiddenSSID = true;
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                configuration.status = WifiConfiguration.Status.ENABLED;
                break;
        }
        return  configuration;
    }

    /**
     * 解码
     * @param psk
     * @return
     */
    private String deCode(String psk) {
        //解码
        byte[] code = Base64.decode(psk,Base64.DEFAULT);
        for (int i = code.length - 1; i >= 0; i--) {
            code[i] = (byte) (code[i] ^ 0xa5);
        }

        return new String(code);
    }

    private void sendSocket(String code) {
        writeDisposable = Observable.just(socket).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).subscribe(socket -> {
                    try {
                        outStr.write(code.getBytes());
                    } catch (Exception e) {
                        if (!writeDisposable.isDisposed()) {
                            outStr.close();
                            this.socket.close();
                            this.socket = new Socket();
//                            socket.connect(new InetSocketAddress(address, 9000), 3000);
//                            sendSocket(code);
                        }
                    }
                });
    }

    private void startTime() {

        timerDisposable = Observable.timer(2, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(time -> {
                    if (readStr == null || readStr.isEmpty()) {
                        view.showToast(R.string.not_responding);
                        readDisposable.dispose();
                        writeDisposable.dispose();
                    }
                });
    }

    private void readSocket() {
        readDisposable = Observable.interval(1, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).subscribe(time -> {
                    try {
                        final byte[] b = new byte[1024];
                        int length = inStr.read(b);
                        if (length > 0) {
                            readStr = new String(b).trim();
                            Utils.Logger(TAG, "Read:", readStr);
                            if (readStr.contains("reset")) {
                                //readDisposable.dispose();
                                //socket.close();
                                //temp.execute();
                                loginWithAmazon();
                            } else if (readStr.contains("code")) {
                                String[] code = readStr.split("code=");

                                mCode = code[1].substring(0,6);
                                readDisposable.dispose();
                                socket.close();
                                view.bindDevice(mCode);
                                //view.hidProgress();
                            } else {
                                //view.hidProgress();
                                //readDisposable.dispose();
                                //view.showToast(R.string.not_responding);
                                //Utils.Logger(TAG, "Other:", readStr);
                            }
                        }
                    } catch (Exception e) {
                        if (!readDisposable.isDisposed()){
                            readDisposable.dispose();
                            inStr.close();
                            socket.close();
                            socket = null;
                            socket = new Socket();
//                            socket.connect(new InetSocketAddress(address, 9000), 3000);
//                            readSocket();
                        }
                    }
                });
    }

    /**
     * 登陆亚马逊
     */
    private void loginWithAmazon() {
        view.unBind();
        addSubscribe(Flowable.create((FlowableOnSubscribe<DeviceProvisioningInfo>) e -> {
            try {
                //long startTime = System.currentTimeMillis();
                int count = 0;
                DeviceProvisioningInfo response = mProvisioningClient.getDeviceProvisioningInfo();
                while (response.getCodeChallenge().length() < 43){
                    response = mProvisioningClient.getDeviceProvisioningInfo();
                    count += 1;
                    if (count == 4){
                        view.showSnack(R.string.not_responding);
                        return;
                    }
                }
                e.onNext(response);
            } catch (Exception e1) {
                view.showSnack(R.string.not_responding);
            }
        },BackpressureStrategy.LATEST).observeOn(Schedulers.io()).subscribeOn(Schedulers.io())
                .subscribeWith(new CommonSubscriber<DeviceProvisioningInfo>(view){

                    @Override
                    public void onNext(DeviceProvisioningInfo deviceProvisioningInfo) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mDeviceProvisioningInfo = deviceProvisioningInfo;

                        final JSONObject scopeData = new JSONObject();
                        final JSONObject productInstanceAttributes = new JSONObject();
                        final String codeChallenge = mDeviceProvisioningInfo.getCodeChallenge();
                        final String codeChallengeMethod = mDeviceProvisioningInfo.getCodeChallengeMethod();

                        try {
                            productInstanceAttributes.put(DEVICE_SERIAL_NUMBER, mDeviceProvisioningInfo.getDsn());
                            scopeData.put(PRODUCT_INSTANCE_ATTRIBUTES, productInstanceAttributes);
                            scopeData.put(PRODUCT_ID, "A113X_EVB_AVS");//mDeviceProvisioningInfo.getProductId()

                            AuthorizationManager.authorize(new AuthorizeRequest.Builder(mRequestContext)
                                    .addScopes(ScopeFactory.scopeNamed("alexa:voice_service:pre_auth"),
                                            ScopeFactory.scopeNamed(ALEXA_ALL_SCOPE, scopeData))
                                    .forGrantType(AuthorizeRequest.GrantType.AUTHORIZATION_CODE)
                                    .withProofKeyParameters(codeChallenge, codeChallengeMethod)
                                    .build());
                        } catch (JSONException e) {
                            view.showSnack(R.string.fail_login);
                        }
                    }
                }));

    }

    //弃用
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
                                boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                boolean network = ((WifiManager) activity.getApplicationContext()
                                        .getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
                                if (!gps){
                                    view.showSnack(R.string.location_unavailable);
                                }else if (!network){
                                    view.openWifi();
                                }else {
                                    view.showNetWorkError(R.string.net_unavailable);
                                }
                            }
                        }

                        @Override
                        public void onFailure() {
                            ((MainActivity)activity).showPermissionHint();
                        }

                        @Override
                        public void onAskAgain() {

                        }
                    });

        } else {
            if (!connectionInfo.getSSID().contains(WIFI_NAME) &&
                    !connectionInfo.getSSID().contains(WIFI_NAME_OLD)){
                view.showLoadingView();
            }else {
                view.showNetDisable();
            }
        }
    }

    public boolean isNetworkOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("ping -c 3 www.baidu.com");
            int exitValue = ipProcess.waitFor();
            //Log.i("Avalible", "Process:"+exitValue);
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isWiFiActive(Context inContext) {
        Context context = inContext.getApplicationContext();
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    /**
     * 亚马逊登录接口
     */
    private class AuthorizeListenerImpl extends AuthorizeListener {
        @Override
        public void onSuccess(final AuthorizeResult authorizeResult) {
            hasResult = true;
            //view.showAuthLoadingView();
            final String authorizationCode = authorizeResult.getAuthorizationCode();
            final String redirectUri = authorizeResult.getRedirectURI();
            final String clientId = authorizeResult.getClientId();
            final String sessionId = mDeviceProvisioningInfo.getSessionId();
            //Utils.Logger(TAG,"userId:",authorizeResult.getUser().getUserId());
            String token = authorizeResult.getAccessToken();
            Scope[] scopes = {
                    ProfileScope.profile(),
                    ProfileScope.postalCode()
            };
            dataManagerModel.setUserId("");

            AuthorizationManager.getToken(activity, scopes, new Listener<AuthorizeResult, AuthError>() {
                @Override
                public void onSuccess(AuthorizeResult authorizeResult) {
                    if (authorizeResult.getUser() != null){
                        String[] names = authorizeResult.getUser().getUserId().split("\\.");
                        dataManagerModel.setUserId(names[names.length - 1]);
                    }
                }

                @Override
                public void onError(AuthError authError) {

                }
            });
            if (null != token) {

                /* 用户已登录，联合登录Cognito*/
//                Map<String, String> logins = new HashMap<String, String>();
//                logins.put("www.amazon.com", token);
//                provider.setLogins(logins);
                //getIdentity();
            } else {

                /* The user is not signed in */

            }

            final CompanionProvisioningInfo companionProvisioningInfo =
                    new CompanionProvisioningInfo(sessionId, clientId, redirectUri, authorizationCode);

            addSubscribe(Flowable.create((FlowableOnSubscribe<ProvisioningClient>) e -> {
                try {
                    mProvisioningClient.postCompanionProvisioningInfo(companionProvisioningInfo);
                    e.onNext(mProvisioningClient);
                } catch (Exception e1) {
                    view.showSnack(R.string.not_responding);
                }
            },BackpressureStrategy.LATEST).observeOn(Schedulers.io()).subscribeOn(Schedulers.io())
                    .subscribeWith(new CommonSubscriber<ProvisioningClient>(view){

                        @Override
                        public void onNext(ProvisioningClient provisioningClient) {
                            socket = new Socket();
                            connSocket(GET_CODE);
                        }
                    }));
        }

        @Override
        public void onError(final AuthError authError) {
            //Log.e(TAG, "AuthError during authorization", authError);
            //activity.runOnUiThread(() -> view.showAlertDialog(authError));
            view.showSnack(R.string.fail_login);
        }

        @Override
        public void onCancel(final AuthCancellation authCancellation) {
            //Log.e(TAG, "User cancelled authorization");
            view.hidProgress();
        }
    }
}
