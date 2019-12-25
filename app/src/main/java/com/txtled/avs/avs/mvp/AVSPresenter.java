package com.txtled.avs.avs.mvp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ScopeFactory;
import com.amazon.identity.auth.device.api.workflow.RequestContext;
import com.txtled.avs.avs.amazonlogin.CompanionProvisioningInfo;
import com.txtled.avs.avs.amazonlogin.DeviceProvisioningInfo;
import com.txtled.avs.avs.amazonlogin.ProvisioningClient;
import com.txtled.avs.base.CommonSubscriber;
import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.mDNS.Mdnser;
import com.txtled.avs.model.DataManagerModel;
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
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.txtled.avs.base.BaseFragment.TAG;
import static com.txtled.avs.utils.Constants.ALEXA_ALL_SCOPE;
import static com.txtled.avs.utils.Constants.DEVICE_SERIAL_NUMBER;
import static com.txtled.avs.utils.Constants.GET_CODE;
import static com.txtled.avs.utils.Constants.PRODUCT_ID;
import static com.txtled.avs.utils.Constants.PRODUCT_INSTANCE_ATTRIBUTES;
import static com.txtled.avs.utils.Constants.RESET_DEVICE;
import static com.txtled.avs.utils.Constants.SERVICE_TYPE;

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
    private String mCode;
    private String address;


    @Inject
    public AVSPresenter(DataManagerModel mDataManagerModel) {
        this.mDataManagerModel = mDataManagerModel;
    }

    @Override
    public void refresh(Activity activity) {
        if (!Utils.getWifiSSID(activity).contains(Constants.WIFI_NAME)){
            mdnser.ipInfos.clear();
            new getIpAsyncTask().execute();
            view.closeRefresh(3100);
        }else {
            view.showNetWorkError();
            view.closeRefresh(0);
        }

    }

    @Override
    public void initAmazon(Activity activity, Context context) {
        this.activity = activity;

        /**********AMZON LOGIN ********************************/

        mRequestContext = RequestContext.create(activity);
        mRequestContext.registerListener(new AuthorizeListenerImpl());

        try {
            mProvisioningClient = new ProvisioningClient(context);
        } catch (Exception e) {
            view.showAlertDialog(e);
            Log.e(TAG, "Unable to use Provisioning Client. CA Certificate is incorrect or does not exist.", e);
        }

        /******************************************************/
        mdnser = new Mdnser(activity);
        mdnser.initializeDiscoveryListener();
        view.initAdapter(mdnser.ipInfos);
    }

    @Override
    public void onItemClick(int position) {
        final String url = "http://" + mdnser.ipInfos.get(position).getHostip();
        address = mdnser.ipInfos.get(position).getHostip();
        mProvisioningClient.setEndpoint(url);

        if (socket != null) {
            try {
                readDisposable.dispose();
                writeDisposable.dispose();
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket = new Socket();
        connSocket(true);
    }

    private void connSocket(boolean sendMsg) {
        addSubscribe(Flowable.create((FlowableOnSubscribe<Socket>) e -> {
                    try {
                        socket.connect(new InetSocketAddress(address, 9000), 3000);
                        inStr = socket.getInputStream();
                        outStr = socket.getOutputStream();
                        e.onNext(socket);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }, BackpressureStrategy.BUFFER).compose(RxUtil.rxSchedulerHelper())
                        .subscribeWith(new CommonSubscriber<Socket>(view) {
                            @Override
                            public void onNext(Socket socket) {
                                readSocket();
                                sendSocket(sendMsg == true ? RESET_DEVICE : GET_CODE);
                            }
                        })
        );
    }

    private void readSocket() {
        AsyncTask<Void, Void, DeviceProvisioningInfo> temp = new AsyncTask<Void, Void, DeviceProvisioningInfo>() {
            private Exception errorInBackground;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.e("TAG", "connect is begin");
            }

            @Override
            protected DeviceProvisioningInfo doInBackground(Void... voids) {
                try {
                    long startTime = System.currentTimeMillis();
                    DeviceProvisioningInfo response = mProvisioningClient.getDeviceProvisioningInfo();
                    long duration = System.currentTimeMillis() - startTime;

//                    if (duration < MIN_CONNECT_PROGRESS_TIME_MS) {
//                        try {
//                            Thread.sleep(MIN_CONNECT_PROGRESS_TIME_MS - duration);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    return response;
                } catch (Exception e) {
                    errorInBackground = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(DeviceProvisioningInfo deviceProvisioningInfo) {
                super.onPostExecute(deviceProvisioningInfo);
                if (deviceProvisioningInfo != null) {
                    mDeviceProvisioningInfo = deviceProvisioningInfo;
//                            SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
//                            editor.putString(getString(R.string.saved_device_address), address);
//                            editor.commit();
                    Log.e("TAG", "CONNECT IS SUCCESS");

                    Log.e("TAG", "Startlogin");

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
                        e.printStackTrace();
                    }

                } else {
                    view.showAlertDialog(errorInBackground);
                }
            }
        };
        readDisposable = Observable.interval(1, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).subscribe(time -> {
                    try {
                        final byte[] b = new byte[1024];
                        int length = inStr.read(b);
                        if (length > 0) {
                            final String str = new String(b).trim();
                            Utils.Logger(TAG, "Read:", str);
                            if (str.contains("reset")) {
                                //readDisposable.dispose();
                                //socket.connect(new InetSocketAddress(address,9000),3000);
                                //readSocket();
                                readDisposable.dispose();
                                socket.close();
                                temp.execute();
                            } else if (str.contains("code")) {
                                String[] code = str.split("=");
                                mCode = code[1];
                                readDisposable.dispose();
                                socket.close();
                                view.bindDevice(mCode);
                            } else {
                                Utils.Logger(TAG, "Other:", str);
                            }
                        }
                    } catch (Exception e) {
                        readDisposable.dispose();
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(address, 9000), 3000);
                        readSocket();
                    }
                });
    }

    private void sendSocket(String code) {
        writeDisposable = Observable.just(socket).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()).subscribe(socket -> {
                    try {
                        outStr.write(code.getBytes());
                    } catch (Exception e) {
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(address, 9000), 3000);
                        sendSocket(code);
                    }
                });
    }

    @Override
    public void resume() {
        mRequestContext.onResume();
    }

    @Override
    public void startSocket() {

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class getIpAsyncTask extends AsyncTask {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected Object doInBackground(Object[] objects) {

            mdnser.initializeDiscoveryListener();
            try {
                mdnser.mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mdnser.mDiscoveryListener);
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mdnser.mNsdManager.stopServiceDiscovery(mdnser.mDiscoveryListener);
                getIpHandler.sendEmptyMessage(0);
            }

            return null;
        }
    }

    Handler getIpHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            mAdapter.notifyDataSetChanged();
            view.setAdapter(mdnser.ipInfos.size());

        }
    };

    private class AuthorizeListenerImpl extends AuthorizeListener {
        @Override
        public void onSuccess(final AuthorizeResult authorizeResult) {
            final String authorizationCode = authorizeResult.getAuthorizationCode();
            final String redirectUri = authorizeResult.getRedirectURI();
            final String clientId = authorizeResult.getClientId();
            final String sessionId = mDeviceProvisioningInfo.getSessionId();

            final CompanionProvisioningInfo companionProvisioningInfo = new CompanionProvisioningInfo(sessionId, clientId, redirectUri, authorizationCode);

            new AsyncTask<Void, Void, Void>() {
                private Exception errorInBackground;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    Log.e("TAG", "Login...");
                    //loginInProgressState();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        mProvisioningClient.postCompanionProvisioningInfo(companionProvisioningInfo);
                    } catch (Exception e) {
                        errorInBackground = e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    if (errorInBackground != null) {
                        //connectCleanState();
                        view.showAlertDialog(errorInBackground);
                    } else {
                        //loginSuccessState();
                        Log.e("TAG", "Login success");
                        socket = new Socket();
                        connSocket(false);
                    }
                }
            }.execute();
        }

        @Override
        public void onError(final AuthError authError) {
            Log.e(TAG, "AuthError during authorization", authError);
            activity.runOnUiThread(() -> view.showAlertDialog(authError));
        }

        @Override
        public void onCancel(final AuthCancellation authCancellation) {
            Log.e(TAG, "User cancelled authorization");
            view.hidProgress();
        }
    }
}
