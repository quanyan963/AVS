package com.txtled.avs.wwa.mvp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.iot.AWSIot;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;
import com.amazonaws.services.iot.model.AttachThingPrincipalRequest;
import com.amazonaws.services.iot.model.AttachThingPrincipalResult;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;
import com.amazonaws.services.iot.model.CreatePolicyRequest;
import com.amazonaws.services.iot.model.CreateThingRequest;
import com.amazonaws.services.iot.model.CreateThingResult;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.txtled.avs.R;
import com.txtled.avs.base.CommonSubscriber;
import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.bean.WWADeviceInfo;
import com.txtled.avs.bean.WWAInfo;
import com.txtled.avs.model.DataManagerModel;
import com.txtled.avs.model.operate.OperateHelper;
import com.txtled.avs.utils.Constants;
import com.txtled.avs.utils.RxUtil;
import com.txtled.avs.utils.Utils;
import com.txtled.avs.wwa.WWAFragment;
import com.txtled.avs.wwa.udp.UDPBuild;

import org.json.JSONException;
import org.json.JSONObject;

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
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.WIFI_SERVICE;
import static com.inuker.bluetooth.library.utils.BluetoothUtils.registerReceiver;
import static com.inuker.bluetooth.library.utils.BluetoothUtils.unregisterReceiver;
import static com.txtled.avs.utils.Constants.ACCESS_KEY;
import static com.txtled.avs.utils.Constants.DISCOVERY;
import static com.txtled.avs.utils.Constants.SECRET_ACCESS_KEY;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public class WWAPresenter extends RxPresenter<WWAContract.View> implements WWAContract.Presenter {
    private DataManagerModel mDataManagerModel;
    private static final String TAG = WWAFragment.class.getSimpleName();
    private boolean mReceiverRegistered = false;
    private Context context;
    private WWAFragment.EsptouchAsyncTask4 mTask;
    private UDPBuild udpBuild;
    private ArrayList<WWADeviceInfo> refreshData;
    private Disposable timeCount;
    private WifiManager my_wifiManager;
    private DhcpInfo dhcpInfo;
    private String broadCast = "";
    private AWSIot awsIot;

    @Inject
    public WWAPresenter(DataManagerModel mDataManagerModel) {
        this.mDataManagerModel = mDataManagerModel;
    }

    @Override
    public void checkPermission(Activity activity) {
        String[] permissions = {Constants.permissions[0], Constants.permissions[1]};
        mDataManagerModel.requestPermissions(activity, permissions, new OperateHelper.OnPermissionsListener() {
            @Override
            public void onSuccess(String name) {
                if (name.equals(Constants.permissions[1])) {
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
        switch (id) {
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

    public boolean isSDKAtLeastP() {
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

    private void getBroadCastIp(){
        my_wifiManager = ((WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE));
        dhcpInfo = my_wifiManager.getDhcpInfo();
        String ip = Utils.getWifiIp(dhcpInfo.ipAddress);
        String netMask = Utils.getWifiIp(dhcpInfo.netmask);
        String[] ipTemp = ip.split("\\.");
        String[] maskTemp = netMask.split("\\.");
        for (int i = 0; i < maskTemp.length; i++) {
            if (maskTemp[i].equals("255")){
                broadCast += ipTemp[i] + ".";
            }else {
                broadCast += (255 - Integer.parseInt(maskTemp[i])) + (i == maskTemp.length - 1 ? "" : ".") ;

            }
        }
    }

    @Override
    public boolean getIsConfigured() {
        return mDataManagerModel.isConfigured();
    }

    @Override
    public void setConfigured(boolean b) {
        mDataManagerModel.setIsConfigured(b);
    }

    @Override
    public void onRefresh() {
        //获取wifi服务
        WifiManager wifiManager =(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            //wifiManager.setWifiEnabled(true);
            view.setData(null);
        }else {
            getBroadCastIp();
            refreshData = new ArrayList<>();
            udpBuild = UDPBuild.getUdpBuild(broadCast);
            udpBuild.setUdpReceiveCallback(data -> {
                String strReceive = new String(data.getData(), 0, data.getLength());
                try {
                    JSONObject deviceInfo = new JSONObject(strReceive);
                    WWADeviceInfo info = new WWADeviceInfo(
                            deviceInfo.optString("ip"),
                            deviceInfo.optString("netmask"),
                            deviceInfo.optString("gw"),
                            deviceInfo.optString("host"),
                            deviceInfo.optString("port"),
                            deviceInfo.optString("cid"),
                            deviceInfo.optString("thing"));
                    refreshData.add(info);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setTime();
            });
            udpBuild.sendMessage(DISCOVERY);
        }
    }

    @Override
    public void insertInfo(List<IEsptouchResult> data) {
        List<WWAInfo> infoList = new ArrayList<>();
        for (IEsptouchResult result: data) {
            infoList.add(new WWAInfo(result.getBssid(),result.getInetAddress().getHostAddress()));
        }
        //mDataManagerModel.insertWWAInfo(infoList);
    }

    @Override
    public void hasData() {
        Flowable.timer(5,TimeUnit.SECONDS).compose(RxUtil.rxSchedulerHelper())
                .subscribeWith(new CommonSubscriber<Long>(view) {
            @Override
            public void onNext(Long aLong) {
                if (refreshData == null || refreshData.isEmpty()){
                    view.closeRefresh();
                    if (udpBuild != null){
                        udpBuild.stopUDPSocket();
                    }
                }
            }
        });
    }

    @Override
    public AWSIot getAmazonIotService(){
        //long startTime = System.currentTimeMillis();
        try {
            if(awsIot == null){
                createIotService();
            }
        }catch (Exception e){
            Utils.Logger(TAG,"IotServiceUtil.getAmazonIotService",e.getMessage());
        }
        //long endTime = System.currentTimeMillis();
        //LOGGER.info("IotServiceUtil.getAmazonIotService，创建连接时间={},accessKey={},secretAccessKey={}",(endTime - startTime),accessKey,secretAccessKey);
        return awsIot;
    }

    @Override
    public void createThing() {
        addSubscribe(Flowable.create(new FlowableOnSubscribe<CreateKeysAndCertificateResult>() {
                    @Override
                    public void subscribe(FlowableEmitter<CreateKeysAndCertificateResult> e) throws Exception {
                        //创建事物
                        CreateThingResult result = awsIot.createThing(new CreateThingRequest()
                                .withThingName("fff"));
                        Utils.Logger(TAG, "CreateThingResult:", "\nthingArn:" + result.getThingArn()
                                + "\nname:" + result.getThingName() + "\nid:" + result.getThingId());
                        //创建证书
                        CreateKeysAndCertificateResult request = awsIot
                                .createKeysAndCertificate(new CreateKeysAndCertificateRequest()
                                        .withSetAsActive(true));
                        //关联证书
                        AttachThingPrincipalResult thingResult = awsIot.attachThingPrincipal(new AttachThingPrincipalRequest()
                                .withThingName(result.getThingName())
                                .withPrincipal(request.getCertificateArn()));
                        //证书附加策略
//                    awsIot.attachPrincipalPolicy(new AttachPrincipalPolicyRequest()
//                            .withPolicyName(request.getCertificateId())
//                            .withPrincipal(request.getCertificateArn()));
                        //创建策略，以下是默认策略文档
                        try {
                            CreatePolicyRequest policyRequest = new CreatePolicyRequest();
                            policyRequest.setPolicyName(Constants.MY_OIT_CE);
                            policyRequest.setPolicyDocument(Constants.POLICY_JSON);
                            awsIot.createPolicy(policyRequest);
                        } catch (Exception e1) {

                        }

                        awsIot.attachPolicy(new AttachPolicyRequest()
                                .withPolicyName(Constants.MY_OIT_CE)
                                .withTarget(request.getCertificateArn()));
                        e.onNext(request);

                    }
                },
                BackpressureStrategy.BUFFER).compose(RxUtil.rxSchedulerHelper())
                .subscribeWith(new CommonSubscriber<CreateKeysAndCertificateResult>(view) {
            @Override
            public void onNext(CreateKeysAndCertificateResult createThingResult) {
//                Utils.Logger(TAG,"KeysAndCertificateResult:","\narn:"+createThingResult.getCertificateArn()
//                        +"\nCertificateId:"+createThingResult.getCertificateId()
//                        +"\nCertificatePem:"+createThingResult.getCertificatePem()
//                        +"\nPrivateKey:"+createThingResult.getKeyPair().getPrivateKey()
//                        +"\nPublicKey:"+createThingResult.getKeyPair().getPublicKey());
                Utils.Logger(TAG,"AttachThingPrincipalResult",createThingResult.toString());

            }
        }));

    }

    /**
     * 创建iot服务连接
     */
    public void createIotService(){
        try {
            AWSCredentials credentials=new BasicAWSCredentials(ACCESS_KEY,SECRET_ACCESS_KEY);

            ClientConfiguration clientConfig = new ClientConfiguration();

            clientConfig.setProtocol(Protocol.HTTPS);

            awsIot = new AWSIotClient(credentials, clientConfig);

        }catch (Exception e){
            Utils.Logger(TAG,"IotServiceUtil.createIotService aws-iot创建连接异常",e.getMessage());
            //LOGGER.error("IotServiceUtil.createIotService aws-iot创建连接异常",e);
            awsIot = null;
        }
    }

    private void setTime() {
        if (timeCount != null){
            timeCount.dispose();
        }
        timeCount = Observable.timer(1, TimeUnit.SECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
                    view.setData(refreshData);
                    mDataManagerModel.insertWWAInfo(refreshData);
                    udpBuild.stopUDPSocket();
                }
                );
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
                view.showAlertDialog(R.string.configure_wifi_change_message, android.R.string.cancel, null);
            }
        } else {
            String ssid = info.getSSID();
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            view.setInfo(ssid, info);
        }
    }
}
