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
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ProfileScope;
import com.amazon.identity.auth.device.api.workflow.RequestContext;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.iot.AWSIot;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;
import com.amazonaws.services.iot.model.AttachThingPrincipalRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;
import com.amazonaws.services.iot.model.CreatePolicyRequest;
import com.amazonaws.services.iot.model.CreateThingRequest;
import com.amazonaws.services.iot.model.CreateThingResult;
import com.amazonaws.services.iot.model.KeyPair;
import com.txtled.avs.R;
import com.txtled.avs.application.MyApplication;
import com.txtled.avs.avs.amazonlogin.CompanionProvisioningInfo;
import com.txtled.avs.avs.mvp.AVSPresenter;
import com.txtled.avs.base.CommonSubscriber;
import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.bean.WWADeviceInfo;
import com.txtled.avs.model.DataManagerModel;
import com.txtled.avs.model.operate.OperateHelper;
import com.txtled.avs.utils.Constants;
import com.txtled.avs.utils.RxUtil;
import com.txtled.avs.utils.Utils;
import com.txtled.avs.wwa.WWAFragment;
import com.txtled.avs.wwa.listener.OnCreateThingListener;
import com.txtled.avs.wwa.listener.OnUdpSendRequest;
import com.txtled.avs.wwa.udp.UDPBuild;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.ArrayList;
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
import static com.txtled.avs.utils.Constants.CA;
import static com.txtled.avs.utils.Constants.REBOOT;
import static com.txtled.avs.utils.Constants.REST_API;
import static com.txtled.avs.utils.Constants.SEND_CA_ONE;
import static com.txtled.avs.utils.Constants.SEND_CA_TWO;
import static com.txtled.avs.utils.Constants.SEND_CERT_ONE;
import static com.txtled.avs.utils.Constants.SEND_CERT_TWO;
import static com.txtled.avs.utils.Constants.SEND_KEY_ONE;
import static com.txtled.avs.utils.Constants.SEND_KEY_TWO;
import static com.txtled.avs.utils.Constants.THING_DIR;
import static com.txtled.avs.utils.Constants.DB_NAME;
import static com.txtled.avs.utils.Constants.DISCOVERY;
import static com.txtled.avs.utils.Constants.SEND_THING_NAME;
import static com.txtled.avs.utils.Constants.USER_ID;
import static com.txtled.avs.utils.ForUse.ACCESS_KEY;
import static com.txtled.avs.utils.ForUse.SECRET_ACCESS_KEY;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public class WWAPresenter extends RxPresenter<WWAContract.View> implements WWAContract.Presenter {
    private DataManagerModel mDataManagerModel;
    private static final String TAG = WWAFragment.class.getSimpleName();
    private boolean mReceiverRegistered = false;
    private Activity context;
    private WWAFragment.EsptouchAsyncTask4 mTask;
    private UDPBuild udpBuild;
    private ArrayList<WWADeviceInfo> refreshData;
    private Disposable timeCount;
    private WifiManager my_wifiManager;
    private DhcpInfo dhcpInfo;
    private String broadCast = "";
    private AWSIot awsIot;
    private CognitoCachingCredentialsProvider provider;
    private AmazonDynamoDB client;
    private CreateThingResult iotThing;
    private CreateKeysAndCertificateResult keysAndCertificate;
    private RequestContext mRequestContext;
    private String userId;

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
    public void init(Activity context) {
        this.context = context;
        provider = MyApplication.getCredentialsProvider();
        client = new AmazonDynamoDBClient(provider);
        userId = mDataManagerModel.getUserId();
        mRequestContext = RequestContext.create(context);
        mRequestContext.registerListener(new AuthorizeListenerImpl());
    }

    @Override
    public void destroy() {
        if (mReceiverRegistered) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void executeTask(WWAFragment wwaFragment, byte[] ssid, byte[] bssid, byte[] password,
                            byte[] deviceCount, byte[] broadcast) {
        if (mTask != null) {
            mTask.cancelEsptouch();
        }
        mTask = new WWAFragment.EsptouchAsyncTask4(wwaFragment);
        mTask.execute(ssid, bssid, password, deviceCount, broadcast);
    }

    private void getBroadCastIp() {
        my_wifiManager = ((WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE));
        dhcpInfo = my_wifiManager.getDhcpInfo();
        String ip = Utils.getWifiIp(dhcpInfo.ipAddress);
        String netMask = Utils.getWifiIp(dhcpInfo.netmask);
        String[] ipTemp = ip.split("\\.");
        String[] maskTemp = netMask.split("\\.");
        for (int i = 0; i < maskTemp.length; i++) {
            if (maskTemp[i].equals("255")) {
                broadCast += ipTemp[i] + ".";
            } else {
                broadCast += (255 - Integer.parseInt(maskTemp[i])) + (i == maskTemp.length - 1 ? "" : ".");

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
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            //wifiManager.setWifiEnabled(true);
            view.setData(null);
        } else {
            getBroadCastIp();
            refreshData = new ArrayList<>();
            udpSend(DISCOVERY, result -> {});
        }
    }

    private void udpSend(String message, OnUdpSendRequest listener){
        udpBuild = UDPBuild.getUdpBuild(broadCast);
        my_wifiManager = ((WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE));
        dhcpInfo = my_wifiManager.getDhcpInfo();
        udpBuild.setIgnoreIp(Utils.getWifiIp(dhcpInfo.ipAddress));
        udpBuild.setUdpReceiveCallback(data -> {
            String strReceive = new String(data.getData(), 0, data.getLength());
            try {
                JSONObject deviceInfo = new JSONObject(strReceive);
                if (message.equals(DISCOVERY)){
                    WWADeviceInfo info = new WWADeviceInfo(
                            deviceInfo.optString("ip"),
                            deviceInfo.optString("netmask"),
                            deviceInfo.optString("gw"),
                            deviceInfo.optString("host"),
                            deviceInfo.optString("port"),
                            deviceInfo.optString("cid"),
                            deviceInfo.optString("thing")
                    );
                    refreshData.add(info);
                    setTime();
                }else {
                    listener.OnRequestListener(strReceive);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        udpBuild.sendMessage(message);
    }

    private void setTime() {
        if (timeCount != null) {
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

    @Override
    public void hasData() {
        Flowable.timer(5, TimeUnit.SECONDS).compose(RxUtil.rxSchedulerHelper())
                .subscribeWith(new CommonSubscriber<Long>(view) {
                    @Override
                    public void onNext(Long aLong) {
                        if (refreshData == null || refreshData.isEmpty()) {
                            view.closeRefresh();
                            if (udpBuild != null) {
                                udpBuild.stopUDPSocket();
                            }
                        }
                    }
                });
    }

    @Override
    public AWSIot getAmazonIotService() {
        //long startTime = System.currentTimeMillis();
        try {
            if (awsIot == null) {
                createIotService();
            }
        } catch (Exception e) {
            Utils.Logger(TAG, "IotServiceUtil.getAmazonIotService", e.getMessage());
        }
        //long endTime = System.currentTimeMillis();
        //LOGGER.info("IotServiceUtil.getAmazonIotService，创建连接时间={},accessKey={},secretAccessKey={}",(endTime - startTime),accessKey,secretAccessKey);
        return awsIot;
    }

    @Override
    public void createThing(int position, String friendlyName, OnCreateThingListener listener) {

        String[] values = friendlyName.split(" ");
        friendlyName = "";
        for (int i = 0; i < values.length; i++) {
            if (!values[i].isEmpty()){
                friendlyName += values[i] + (i == values.length - 1 ? "" : "-");
            }
        }
        String finalRealName = friendlyName;
        addSubscribe(Flowable.create((FlowableOnSubscribe<CreateKeysAndCertificateResult>) e -> {

                    createIotCore(finalRealName, position, listener);
                    CreateKeysAndCertificateResult request = new CreateKeysAndCertificateResult();
                    //写入数据库
                    e.onNext(request);

                },
                BackpressureStrategy.BUFFER).compose(RxUtil.rxSchedulerHelper())
                .subscribeWith(new CommonSubscriber<CreateKeysAndCertificateResult>(view) {
                    @Override
                    public void onNext(CreateKeysAndCertificateResult createThingResult) {
                        //refreshData.get(position).setConfigure(true);
                        //mDataManagerModel.upDataWWAInfo(refreshData.get(position));
                        view.createSuccess();
                        //listener.dismiss();
                        Utils.Logger(TAG, "AttachThingPrincipalResult", createThingResult.toString());

                    }
                }));

    }

    @Override
    public boolean checkUserHasLogin() {
        userId = mDataManagerModel.getUserId();
        if (userId.isEmpty()){
            AuthorizationManager.authorize(
                    new AuthorizeRequest.Builder(mRequestContext)
                            .addScopes(ProfileScope.profile())
                            .build()
            );
            return false;
        }else {
            return true;
        }

    }

    @Override
    public void onResume() {
        mRequestContext.onResume();
    }

    private void createIotCore(String friendlyName, int position, OnCreateThingListener listener) {

        //写入DDB
        try {
            listener.onStatueChange(R.string.associating_database);
            HashMap<String, AttributeValue> key = new HashMap<>();
            key.put(USER_ID, new AttributeValue().withS(userId));
            //获取数据
            GetItemResult itemResult = client.getItem(new GetItemRequest()
                    .withTableName(DB_NAME).withKey(key));
            if (itemResult.getItem() != null) {
                //更改数据
                Map<String, AttributeValue> resultItem = itemResult.getItem();
                AttributeValue cert_data = resultItem.get(THING_DIR);
                //数据查重
                String[] names = cert_data.getM().keySet().toArray(new String[cert_data.getM().size()]);
                for (int i = 0; i < names.length; i++) {
                    if (names[i].equals(friendlyName)){
                        //listener.onStatueChange(R.string.device_name_used);
                        //已存在则不创建事物，使用数据库数据
                        Map<String,AttributeValue> deviceData = cert_data.getM().get(friendlyName).getM();

                        keysAndCertificate = new CreateKeysAndCertificateResult();
                        iotThing = new CreateThingResult();
                        keysAndCertificate.withKeyPair(new KeyPair()
                                .withPrivateKey(deviceData.get("certKey").getS()))
                                .setCertificatePem(deviceData.get("certPem").getS());
                        iotThing.withThingName(userId+"_"+friendlyName)
                                .withThingId(deviceData.get("thingId").getS())
                                .setThingArn(deviceData.get("thingArn").getS());
                        //写入设备
                        writeToDevice(position, listener);
                        return;
                    }
                }
                //不重复则创建事物
                createIotThing(listener,friendlyName);

                HashMap<String, AttributeValue> cert = createData(keysAndCertificate,iotThing);
                cert_data.addMEntry(friendlyName, new AttributeValue().withM(cert));

                client.updateItem(new UpdateItemRequest().withTableName(DB_NAME)
                        .withKey(key).addAttributeUpdatesEntry(THING_DIR,
                                new AttributeValueUpdate()
                                        .withValue(cert_data)));
            } else {
                //创建事物
                createIotThing(listener,friendlyName);
                //创建数据
                HashMap<String, AttributeValue> certs = new HashMap<>();
                certs.put(friendlyName, new AttributeValue()
                        .withM(createData(keysAndCertificate,iotThing)));

                PutItemRequest request = new PutItemRequest();
                request.withTableName(Constants.DB_NAME);
                request.addItemEntry(USER_ID, new AttributeValue().withS(userId));
                request.addItemEntry(THING_DIR, new AttributeValue().withM(certs));
                client.putItem(request);
            }
        } catch (Exception e) {
            //创建表
            CreateTableRequest tableRequest = new CreateTableRequest()
                    .withTableName(DB_NAME)
                    .withKeySchema(new KeySchemaElement().withAttributeName(USER_ID)
                            .withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition()
                            .withAttributeName(USER_ID)
                            .withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(
                            new ProvisionedThroughput(10L, 10L));
            tableRequest.setGeneralProgressListener(progressEvent -> {
                if (progressEvent.getEventCode() == 4) {
                    //创建事物
                    createIotThing(listener,friendlyName);
                    //创建数据
                    HashMap<String, AttributeValue> certs = new HashMap<>();
                    certs.put(friendlyName, new AttributeValue()
                            .withM(createData(keysAndCertificate,iotThing)));

                    PutItemRequest request = new PutItemRequest();
                    request.withTableName(Constants.DB_NAME);
                    request.addItemEntry(USER_ID, new AttributeValue().withS(userId));
                    request.addItemEntry(THING_DIR, new AttributeValue().withM(certs));
                    client.putItem(request);
                }
            });
            client.createTable(tableRequest);
        }

        //写入设备
        writeToDevice(position, listener);
    }

    private void writeToDevice(int position, OnCreateThingListener listener) {
        listener.onStatueChange(R.string.transmitting_data);

        broadCast = refreshData.get(position).getIp();
        udpSend(String.format(SEND_THING_NAME, REST_API, userId,
                iotThing.getThingName()), result -> {
            if (result.contains("\"ca0\"")){
                udpBuild.sendMessage(result.contains("1") ?
                        String.format(SEND_CA_TWO,CA.substring(CA.length()/2)) :
                        String.format(SEND_CA_ONE,CA.substring(0,CA.length()/2)));

            }else if (result.contains("\"ca1\"")){
                udpBuild.sendMessage(result.contains("\"ca1\":1") ?
                        String.format(SEND_CERT_ONE,keysAndCertificate.getCertificatePem()
                                .substring(0,keysAndCertificate.getCertificatePem().length()/2)) :
                        String.format(SEND_CA_TWO,CA.substring(CA.length()/2)));

            }else if (result.contains("\"cert0\"")){
                udpBuild.sendMessage(result.contains("1") ?
                        String.format(SEND_CERT_TWO,keysAndCertificate.getCertificatePem()
                                .substring(keysAndCertificate.getCertificatePem().length()/2)):
                        String.format(SEND_CERT_ONE,keysAndCertificate.getCertificatePem()
                                .substring(0,keysAndCertificate.getCertificatePem().length()/2)));

            }else if (result.contains("\"cert1\"")){
                udpBuild.sendMessage(result.contains("\"cert1\":1") ?
                        String.format(SEND_KEY_ONE,keysAndCertificate.getKeyPair().getPrivateKey()
                                .substring(0,keysAndCertificate.getKeyPair().getPrivateKey().length()/2)) :
                        String.format(SEND_CERT_TWO,keysAndCertificate.getCertificatePem()
                                .substring(keysAndCertificate.getCertificatePem().length()/2)));

            }else if (result.contains("\"key0\"")){
                udpBuild.sendMessage(result.contains("1") ?
                        String.format(SEND_KEY_TWO,keysAndCertificate.getKeyPair().getPrivateKey()
                                .substring(keysAndCertificate.getKeyPair().getPrivateKey().length()/2)):
                        String.format(SEND_KEY_ONE,keysAndCertificate.getKeyPair().getPrivateKey()
                                .substring(0,keysAndCertificate.getKeyPair().getPrivateKey().length()/2)));

            }else if (result.contains("\"key1\"")){
                udpBuild.sendMessage(result.contains("\"key1\":1") ? REBOOT :
                        String.format(SEND_KEY_TWO,keysAndCertificate.getKeyPair().getPrivateKey()
                                .substring(keysAndCertificate.getKeyPair().getPrivateKey().length()/2)));
            }else if (result.contains("\"reboot\"")){
                if (result.contains("1")){
                    udpBuild.stopUDPSocket();
                    listener.onStatueChange(R.string.transmit_completed);
                    listener.dismiss();
                }else {
                    udpBuild.sendMessage(REBOOT);
                }
            } else {
                udpBuild.sendMessage(result.contains("1") ?
                        String.format(SEND_CA_ONE,CA.substring(0,CA.length()/2)) :
                        String.format(SEND_THING_NAME, REST_API, userId, iotThing.getThingName()));
            }
        });
    }

    private void createIotThing(OnCreateThingListener listener, String friendlyName){
        listener.onStatueChange(R.string.hint_create_thing);
        //创建事物
        iotThing = awsIot.createThing(new CreateThingRequest()
                .withThingName(userId+"_"+friendlyName));
        Utils.Logger(TAG, "CreateThingResult:", "\nthingArn:" + iotThing.getThingArn()
                + "\nname:" + iotThing.getThingName() + "\nid:" + iotThing.getThingId());
        //创建证书
        keysAndCertificate = awsIot
                .createKeysAndCertificate(new CreateKeysAndCertificateRequest()
                        .withSetAsActive(true));
        Utils.Logger(TAG, "KeysAndCertificateResult:",
                "\narn:" + keysAndCertificate.getCertificateArn()
                        + "\nCertificateId:" + keysAndCertificate.getCertificateId()
                        + "\nCertificatePem:" + keysAndCertificate.getCertificatePem()
                        + "\nPrivateKey:" + keysAndCertificate.getKeyPair().getPrivateKey()
                        + "\nPublicKey:" + keysAndCertificate.getKeyPair().getPublicKey());
        //关联证书
        awsIot.attachThingPrincipal(new AttachThingPrincipalRequest()
                .withThingName(iotThing.getThingName())
                .withPrincipal(keysAndCertificate.getCertificateArn()));

//                    awsIot.attachPrincipalPolicy(new AttachPrincipalPolicyRequest()
//                            .withPolicyName(keysAndCertificate.getCertificateId())
//                            .withPrincipal(keysAndCertificate.getCertificateArn()));
        //创建策略，以下是默认策略文档
        try {
            CreatePolicyRequest policyRequest = new CreatePolicyRequest();
            policyRequest.setPolicyName(Constants.MY_OIT_CE);
            policyRequest.setPolicyDocument(Constants.POLICY_JSON);
            awsIot.createPolicy(policyRequest);
        } catch (Exception e1) {

        }

        //证书附加策略
        awsIot.attachPolicy(new AttachPolicyRequest()
                .withPolicyName(Constants.MY_OIT_CE)
                .withTarget(keysAndCertificate.getCertificateArn()));
    }

    private HashMap<String, AttributeValue> createData(CreateKeysAndCertificateResult keysAndCertificate
            , CreateThingResult iotThing){
        HashMap<String, AttributeValue> cert = new HashMap<>();
        //cert.put("certId", new AttributeValue().withS(keysAndCertificate.getCertificateId()));
        cert.put("certKey", new AttributeValue().withS(keysAndCertificate.getKeyPair().getPrivateKey()));
        cert.put("certPem", new AttributeValue().withS(keysAndCertificate.getCertificatePem()));
        //cert.put("certArn", new AttributeValue().withS(keysAndCertificate.getCertificateArn()));
        cert.put("thingArn", new AttributeValue().withS(iotThing.getThingArn()));
        cert.put("thingId", new AttributeValue().withS(iotThing.getThingId()));
        //cert.put("friendlyName", new AttributeValue().withS(iotThing.getThingName()));
        return cert;
    }

    /**
     * 创建iot服务连接
     */
    public void createIotService() {
        try {
            AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_ACCESS_KEY);

            ClientConfiguration clientConfig = new ClientConfiguration();

            clientConfig.setProtocol(Protocol.HTTPS);

            awsIot = new AWSIotClient(credentials, clientConfig);

        } catch (Exception e) {
            Utils.Logger(TAG, "IotServiceUtil.createIotService aws-iot创建连接异常", e.getMessage());
            //LOGGER.error("IotServiceUtil.createIotService aws-iot创建连接异常",e);
            awsIot = null;
        }
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

    private class AuthorizeListenerImpl extends AuthorizeListener {
        @Override
        public void onSuccess(final AuthorizeResult authorizeResult) {

            //Utils.Logger(TAG,"userId:",authorizeResult.getUser().getUserId());
            String token = authorizeResult.getAccessToken();

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
            String[] values = authorizeResult.getUser().getUserId().split("\\.");
            mDataManagerModel.setUserId(values[values.length - 1]);
        }

        @Override
        public void onError(final AuthError authError) {
            Log.e(TAG, "AuthError during authorization", authError);
            //activity.runOnUiThread(() -> view.showAlertDialog(authError));
        }

        @Override
        public void onCancel(final AuthCancellation authCancellation) {
            Log.e(TAG, "User cancelled authorization");
            //view.hidProgress();
        }
    }
}
