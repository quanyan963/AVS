package com.txtled.avs.avs.mvp;

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
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.txtled.avs.R;
import com.txtled.avs.application.MyApplication;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
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
import static com.inuker.bluetooth.library.utils.BluetoothUtils.registerReceiver;
import static com.inuker.bluetooth.library.utils.BluetoothUtils.unregisterReceiver;
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
 * Created by Mr.Quan on 2019/12/10.
 */
public class AVSPresenter extends RxPresenter<AVSContract.View> implements AVSContract.Presenter {
    private DataManagerModel mDataManagerModel;

    private ProvisioningClient mProvisioningClient;
    private DeviceProvisioningInfo mDeviceProvisioningInfo;
    private Mdnser mdnser;
    private Activity activity;
    private RequestContext mRequestContext;
    private Socket socket;
    private OutputStream outStr = null;
    private InputStream inStr = null;
    private Disposable readDisposable;
    private Disposable writeDisposable;
    private Disposable timerDisposable;
    private Disposable loginDisposable;
    private String mCode;
    private String address;
    private CognitoCachingCredentialsProvider provider;
    private AuthorizeListenerImpl authorizeListener;
    private String readStr;
    private boolean avsSwitch;
    private LocationManager locationManager;

    @Inject
    public AVSPresenter(DataManagerModel mDataManagerModel) {
        this.mDataManagerModel = mDataManagerModel;
    }

    @Override
    public void refresh() {
        if ((!Utils.getWifiSSID(activity).contains(Constants.WIFI_NAME) ||
                !Utils.getWifiSSID(activity).contains(Constants.WIFI_NAME_OLD)) &&
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            mdnser.ipInfos.clear();
            addSubscribe(Flowable.create((FlowableOnSubscribe<Mdnser>) e -> {
                mdnser.initializeDiscoveryListener();
                mdnser.mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD,
                        mdnser.mDiscoveryListener);
                Thread.sleep(3 * 1000);
                e.onNext(mdnser);
            },BackpressureStrategy.BUFFER).compose(RxUtil.rxSchedulerHelper())
                    .subscribeWith(new CommonSubscriber<Mdnser>(view){

                @Override
                public void onNext(Mdnser mdnser) {
                    mdnser.mNsdManager.stopServiceDiscovery(mdnser.mDiscoveryListener);
                    view.setAdapter(mdnser.ipInfos.size());
                    view.closeRefresh();
                }
            }));
        }else {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                view.showNetWorkError(R.string.location_unavailable);
                view.closeRefresh();
            }else {
                view.showSnack(R.string.net_unavailable);
                view.closeRefresh();
            }
        }

    }

    @Override
    public void initAmazon(Activity activity, Context context) {
        this.activity = activity;

        /**********AMZON LOGIN ********************************/

        mRequestContext = RequestContext.create(activity);
        authorizeListener = new AuthorizeListenerImpl();
        mRequestContext.registerListener(authorizeListener);

        try {
            mProvisioningClient = new ProvisioningClient(context);
        } catch (Exception e) {
            //view.showAlertDialog(e);
            //Log.e(TAG, "Unable to use Provisioning Client. CA Certificate is incorrect or does not exist.", e);
        }

        /******************************************************/
        mdnser = new Mdnser(activity);
        mdnser.initializeDiscoveryListener();
        view.initAdapter(mdnser.ipInfos);

        //getCognito
        provider = MyApplication.getCredentialsProvider();

        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (Build.VERSION.SDK_INT == 28) {
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        }
        registerReceiver(mReceiver, filter);

        locationManager = (LocationManager) activity
                .getSystemService(Context.LOCATION_SERVICE);
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

    private void onWifiChanged(WifiInfo connectionInfo) {
        boolean disconnected = connectionInfo == null
                || connectionInfo.getNetworkId() == -1
                || "<unknown ssid>".equals(connectionInfo.getSSID());
        if (disconnected) {
            String[] permissions = {Constants.permissions[0], Constants.permissions[1]};
            mDataManagerModel.requestPermissions(activity, permissions,
                    new OperateHelper.OnPermissionsListener() {
                @Override
                public void onSuccess(String name) {
                    if (name.equals(Constants.permissions[1])) {

                        assert locationManager != null;
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                            view.showSnack(R.string.location_unavailable);
                        }else {

                            if (avsSwitch){
                                view.showNetWorkError(R.string.net_unavailable);
                            }else {
                                view.showNetWorkError(R.string.avs_wifi_available);
                            }
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
            if (avsSwitch){
                view.hidSnackBar();
            }else {
                if (connectionInfo.getSSID().contains(WIFI_NAME) ||
                        connectionInfo.getSSID().contains(WIFI_NAME_OLD)){
                    view.hidSnackBar();
                }else {
                    view.showNetWorkError(R.string.avs_wifi_available);
                }
            }
        }
    }

    @Override
    public void onItemClick(int position) {
//        AuthorizationManager.authorize(
//                new AuthorizeRequest.Builder(mRequestContext)
//                        .addScopes(ProfileScope.profile())
//                        .build()
//        );

        final String url = "http://" + mdnser.ipInfos.get(position).getHostip();
        address = mdnser.ipInfos.get(position).getHostip();
        mProvisioningClient.setEndpoint(url);
        readStr = "";

        if (socket != null) {
            try {
                readDisposable.dispose();
                writeDisposable.dispose();
                timerDisposable.dispose();
                loginDisposable.dispose();
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket = new Socket();
        connSocket(RESET_DEVICE);
    }

    private void connSocket(String sendMsg) {
        loginDisposable = Flowable.create((FlowableOnSubscribe<Socket>) e -> {
            try {
                socket.connect(new InetSocketAddress(address, 9000), 2000);
                inStr = socket.getInputStream();
                outStr = socket.getOutputStream();
                e.onNext(socket);
            } catch (IOException e1) {
                socket = null;
                socket = new Socket();
                connSocket(sendMsg);
            }
        }, BackpressureStrategy.LATEST).compose(RxUtil.rxSchedulerHelper())
                .subscribeWith(new CommonSubscriber<Socket>(view) {
                    @Override
                    public void onNext(Socket socket) {
                        readSocket();
                        startTime();
                        sendSocket(sendMsg);
                    }
                });
    }

    private void startTime() {

        timerDisposable = Observable.timer(4,TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(time -> {
                    if (readStr == null || readStr.isEmpty()){
                        view.hidProgress();
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
                                readDisposable.dispose();
                                socket.close();
                                //temp.execute();
                                loginWithAmazon();
                            } else if (readStr.contains("code")) {
                                String[] code = readStr.split("=");
                                mCode = code[1];
                                readDisposable.dispose();
                                socket.close();
                                view.bindDevice(mCode);
                                view.hidProgress();
                            } else {
                                view.hidProgress();
                                readDisposable.dispose();
                                view.showToast(R.string.not_responding);
                                Utils.Logger(TAG, "Other:", readStr);
                            }
                        }
                    } catch (Exception e) {
                        if (!readDisposable.isDisposed()){
                            readDisposable.dispose();
                            socket = null;
                            socket = new Socket();
                            socket.connect(new InetSocketAddress(address, 9000), 3000);
                            readSocket();
                        }
                    }
                });
    }

    /**
     * 登陆亚马逊
     */
    private void loginWithAmazon() {
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

    private void sendSocket(String code) {
        writeDisposable = Observable.just(socket).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).subscribe(socket -> {
                    try {
                        outStr.write(code.getBytes());
                    } catch (Exception e) {
                        if (!writeDisposable.isDisposed()){
                            socket = new Socket();
                            socket.connect(new InetSocketAddress(address, 9000), 3000);
                            sendSocket(code);
                        }
                    }
                });
    }

    @Override
    public void resume() {
        mRequestContext.onResume();
    }

    @Override
    public void destroy() {
        try {
            if (socket != null) {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
                socket = null;
            }
            if (outStr != null) {
                outStr.close();
                outStr = null;
            }
            if (inStr != null) {
                inStr.close();
                inStr = null;
            }
            if (readDisposable != null) {
                readDisposable.dispose();
                readDisposable = null;
            }
            if (writeDisposable != null) {
                writeDisposable.dispose();
                writeDisposable = null;
            }
            if (loginDisposable != null){
                loginDisposable.dispose();
                loginDisposable = null;
            }
            mdnser = null;
            mRequestContext.unregisterListener(authorizeListener);
            unregisterReceiver(mReceiver);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSwitch(boolean avsSwitch) {
        this.avsSwitch = avsSwitch;
        WifiManager wifiManager = (WifiManager) activity.getApplicationContext()
                .getSystemService(WIFI_SERVICE);
        onWifiChanged(wifiManager.getConnectionInfo());
    }

    @Override
    public boolean isAvsWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(WIFI_SERVICE);
        String name = wifiManager.getConnectionInfo().getSSID();
        if (name.contains(WIFI_NAME) || name.contains(WIFI_NAME_OLD)){
            return true;
        }else {
            return false;
        }
    }

    private class AuthorizeListenerImpl extends AuthorizeListener {
        @Override
        public void onSuccess(final AuthorizeResult authorizeResult) {
            view.showLoadingView();
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
            mDataManagerModel.setUserId("");

            AuthorizationManager.getToken(activity, scopes, new Listener<AuthorizeResult, AuthError>() {
                @Override
                public void onSuccess(AuthorizeResult authorizeResult) {
                    if (authorizeResult.getUser() != null){
                        String[] names = authorizeResult.getUser().getUserId().split("\\.");
                        mDataManagerModel.setUserId(names[names.length - 1]);
                    }
                }

                @Override
                public void onError(AuthError authError) {

                }
            });
//            try {
//
//                URL url = new URL("https://www.amazon.com/ap/oa?client_id="+clientId+"&scope=alexa::skills:account_linking&response_type=code");//redirect_uri=https://ww.txtled.avs&  &state=VFJVRQ
//
//                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//                connection.addRequestProperty("Content-Type", "application/json");//application/x-www-form-urlencoded;charset=UTF-8
//                connection.setRequestMethod("GET");
////                connection.setDoOutput(true);
////                String data = "grant_type=authorization_code&code="+authorizationCode+
////                        "&redirect_uri="+redirectUri+"&client_id="+clientId+"&code_verifier="+mDeviceProvisioningInfo.getCodeChallenge();
////                outputStream = new DataOutputStream(connection.getOutputStream());
////                outputStream.write(data.getBytes());
////                outputStream.flush();
////                outputStream.close();
//
//                inputStream = connection.getInputStream();
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            //authorizeResult.getUser().getUserId();
            if (null != token) {

                /* 用户已登录，联合登录Cognito*/
                Map<String, String> logins = new HashMap<String, String>();
                logins.put("www.amazon.com", token);
                provider.setLogins(logins);
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
