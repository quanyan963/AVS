package com.txtled.avs.avs.mvp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ProfileScope;
import com.amazon.identity.auth.device.api.authorization.ScopeFactory;
import com.amazon.identity.auth.device.api.workflow.RequestContext;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;
import com.txtled.avs.avs.amazonlogin.CompanionProvisioningInfo;
import com.txtled.avs.avs.listener.OnSearchListener;
import com.txtled.avs.avs.amazonlogin.DeviceProvisioningInfo;
import com.txtled.avs.avs.amazonlogin.ProvisioningClient;
import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.mDNS.Mdnser;
import com.txtled.avs.model.DataManagerModel;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import static com.txtled.avs.base.BaseFragment.TAG;
import static com.txtled.avs.utils.Constants.ALEXA_ALL_SCOPE;
import static com.txtled.avs.utils.Constants.APP_SCOPES;
import static com.txtled.avs.utils.Constants.DEVICE_SERIAL_NUMBER;
import static com.txtled.avs.utils.Constants.MIN_CONNECT_PROGRESS_TIME_MS;
import static com.txtled.avs.utils.Constants.PRODUCT_ID;
import static com.txtled.avs.utils.Constants.PRODUCT_INSTANCE_ATTRIBUTES;
import static com.txtled.avs.utils.Constants.SERVICE_TYPE;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public class AVSPresenter extends RxPresenter<AVSContract.View> implements AVSContract.Presenter {
    private DataManagerModel mDataManagerModel;
    //private AmazonAuthorizationManager mAuthManager;
    private ProvisioningClient mProvisioningClient;
    private DeviceProvisioningInfo mDeviceProvisioningInfo;
    private Mdnser mdnser;
    private Activity activity;
    private RequestContext mRequestContext;


    @Inject
    public AVSPresenter(DataManagerModel mDataManagerModel) {
        this.mDataManagerModel = mDataManagerModel;
    }

    @Override
    public void refresh(OnSearchListener listener) {
        mdnser.ipInfos.clear();
        new getIpAsyncTask().execute();
    }

    @Override
    public void initAmazon(Activity activity, Context context) {
        this.activity = activity;

        /**********AMZON LOGIN ********************************/

        mRequestContext = RequestContext.create(activity);
        mRequestContext.registerListener(new AuthorizeListenerImpl());

//        try {
//            mAuthManager = new AmazonAuthorizationManager(context, Bundle.EMPTY);
//        } catch (IllegalArgumentException e) {
//            view.showAlertDialog(e);
//            Log.e(TAG, "Unable to use Amazon Authorization Manager. APIKey is incorrect or does not exist.", e);
//        }
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
        final String address = "http://" + mdnser.ipInfos.get(position).getHostip();
        mProvisioningClient.setEndpoint(address);

        new AsyncTask<Void, Void, DeviceProvisioningInfo>() {
            private Exception errorInBackground;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.e("TAG","connect is begin");
            }

            @Override
            protected DeviceProvisioningInfo doInBackground(Void... voids) {
                try {
                    long startTime = System.currentTimeMillis();
                    DeviceProvisioningInfo response = mProvisioningClient.getDeviceProvisioningInfo();
                    long duration = System.currentTimeMillis() - startTime;

                    if (duration < MIN_CONNECT_PROGRESS_TIME_MS) {
                        try {
                            Thread.sleep(MIN_CONNECT_PROGRESS_TIME_MS - duration);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
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
                    Log.e("TAG","CONNECT IS SUCCESS");

                    Log.e("TAG","Startlogin");

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


//                    Bundle options = new Bundle();
//                    JSONObject scopeData = new JSONObject();
//                    JSONObject productInfo = new JSONObject();
//                    JSONObject productInstanceAttributes = new JSONObject();
//                    try {
//                        productInstanceAttributes.put(DEVICE_SERIAL_NUMBER, mDeviceProvisioningInfo.getDsn());
//                        productInfo.put(PRODUCT_ID, mDeviceProvisioningInfo.getProductId());
//                        productInfo.put(PRODUCT_INSTANCE_ATTRIBUTES, productInstanceAttributes);
//                        scopeData.put(ALEXA_ALL_SCOPE, productInfo);
//
//                        String codeChallenge = mDeviceProvisioningInfo.getCodeChallenge();
//                        String codeChallengeMethod = mDeviceProvisioningInfo.getCodeChallengeMethod();
//                        options.putString(AuthzConstants.BUNDLE_KEY.SCOPE_DATA.val, scopeData.toString());
//                        Log.e(TAG, scopeData.toString());
//                        options.putBoolean(AuthzConstants.BUNDLE_KEY.GET_AUTH_CODE.val, true);
//                        options.putString(AuthzConstants.BUNDLE_KEY.CODE_CHALLENGE.val, codeChallenge);
//                        options.putString(AuthzConstants.BUNDLE_KEY.CODE_CHALLENGE_METHOD.val, codeChallengeMethod);
//                        AuthorizationManager.authorize(new AuthorizeRequest.Builder(mRequestContext)
//                                .addScope(ScopeFactory.scopeNamed(ALEXA_ALL_SCOPE, scopeData))
//                                .forGrantType(AuthorizeRequest.GrantType.AUTHORIZATION_CODE)
//                                .withProofKeyParameters(codeChallenge, codeChallengeMethod)
//                                .build());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }

                } else {
                    view.showAlertDialog(errorInBackground);
                }
            }
        }.execute();
    }

    @Override
    public void resume() {
        mRequestContext.onResume();
    }

    class getIpAsyncTask extends AsyncTask {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected Object doInBackground(Object[] objects) {

//            mdnser.initializeDiscoveryListener();
            mdnser.mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mdnser.mDiscoveryListener);
            try {
                Thread.sleep(2*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                mdnser.mNsdManager.stopServiceDiscovery(mdnser.mDiscoveryListener);
                getIpHandler.sendEmptyMessage(0);
            }

            return null;
        }
    }
    Handler getIpHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            mAdapter.notifyDataSetChanged();
            view.setAdapter(mdnser.ipInfos.size());

        }
    };

//    private class AuthListener implements AuthorizationListener {
//        @Override
//        public void onSuccess(Bundle response) {
//            try {
//                final String authorizationCode = response.getString(AuthzConstants.BUNDLE_KEY.AUTHORIZATION_CODE.val);
//                final String redirectUri = mAuthManager.getRedirectUri();
//                final String clientId = mAuthManager.getClientId();
//                final String sessionId = mDeviceProvisioningInfo.getSessionId();
//
//                final CompanionProvisioningInfo companionProvisioningInfo = new CompanionProvisioningInfo(sessionId, clientId, redirectUri, authorizationCode);
//
//                new AsyncTask<Void, Void, Void>() {
//                    private Exception errorInBackground;
//
//                    @Override
//                    protected void onPreExecute() {
//                        super.onPreExecute();
//                        Log.e("TAG","Logining...");
//                    }
//
//                    @Override
//                    protected Void doInBackground(Void... voids) {
//                        try {
//                            mProvisioningClient.postCompanionProvisioningInfo(companionProvisioningInfo);
//                        } catch (Exception e) {
//                            errorInBackground = e;
//                        }
//                        return null;
//                    }
//
//                    @Override
//                    protected void onPostExecute(Void result) {
//                        super.onPostExecute(result);
//                        if (errorInBackground != null) {
//                            Log.e("TAG","Logining...");
//                            view.showAlertDialog(errorInBackground);
//                        } else {
//                            Log.e("TAG","Login success");
//                            Toast.makeText(activity,"Reflash token is success!",Toast.LENGTH_LONG).show();
//                        }
//                    }
//                }.execute();
//            } catch (AuthError authError) {
//                authError.printStackTrace();
//            }
//        }
//
//        @Override
//        public void onError(final AuthError ae) {
//            Log.e(TAG, "AuthError during authorization", ae);
//            activity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    view.showAlertDialog(ae);
//                }
//            });
//        }
//        @Override
//        public void onCancel(Bundle cause) {
//            Log.e(TAG, "User cancelled authorization");
//        }
//    }

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
                    Log.e("TAG","Login...");
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
                        Log.e("TAG","Login success");
                        view.showSuccess();
                    }
                }
            }.execute();
        }

        @Override
        public void onError(final AuthError authError) {
            Log.e(TAG, "AuthError during authorization", authError);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.showAlertDialog(authError);
                }
            });
        }

        @Override
        public void onCancel(final AuthCancellation authCancellation) {
            Log.e(TAG, "User cancelled authorization");
        }
    }
}
