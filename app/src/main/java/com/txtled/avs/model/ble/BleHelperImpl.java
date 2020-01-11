package com.txtled.avs.model.ble;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.txtled.avs.application.MyApplication;
import com.txtled.avs.utils.BleUtils;
import com.txtled.avs.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;

/**
 * Created by Mr.Quan on 2018/4/17.
 */

public class BleHelperImpl implements BleHelper {
    public static final String TAG = BleHelperImpl.class.getSimpleName();
    public static final int DURATION = 10000;
    public static final int TIMES = 3;
    private BluetoothClient mBleClient;
    private SearchRequest mRequest;
    private BleConnectOptions mOptions;
    private String mAddress;
    private UUID mServiceUUID;
    private UUID mSendCharacterUUID;
    private UUID mNotifyCharacterUUID;
    private BleConnectStatusListener listener;
    private boolean conn;
    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothSocket socket;

    @Inject
    public BleHelperImpl() {
        mBleClient = new BluetoothClient(MyApplication.getInstance());
        mRequest = new SearchRequest.Builder()
                .searchBluetoothLeDevice(DURATION, TIMES).build();
        mOptions = new BleConnectOptions.Builder()
                .setConnectRetry(TIMES)   // 连接如果失败重试2次
                .setConnectTimeout(DURATION)   // 连接超时5s
                .setServiceDiscoverRetry(TIMES)  // 发现服务如果失败重试2次
                .setServiceDiscoverTimeout(DURATION)  // 发现服务超时5s
                .build();
    }

    @Override
    public void scanBle(Activity activity, boolean isSpecified, OnScanBleListener onScanBleListener, final OnConnBleListener onConnBleListener) {

    }

    private void connectA2DP(BluetoothAdapter adapter, Activity activity, BluetoothDevice device) {
        if(adapter.getProfileConnectionState(BluetoothProfile.A2DP)!= BluetoothProfile.STATE_CONNECTED){
            //在listener中完成A2DP服务的调用
            adapter.getProfileProxy(activity, new connServListener(device), BluetoothProfile.A2DP);
        }
    }

    public class connServListener implements BluetoothProfile.ServiceListener {
        private BluetoothDevice device;

        public connServListener(BluetoothDevice device) {
            this.device = device;
        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            //use reflect method to get the Hide method "connect" in BluetoothA2DP
            BluetoothA2dp a2dp = (BluetoothA2dp) proxy;
            //a2dp.isA2dpPlaying(mBTDevInThread);
            Class<? extends BluetoothA2dp> clazz = a2dp.getClass();
            Method method_Connect;
            //通过BluetoothA2DP隐藏的connect(BluetoothDevice btDev)函数，打开btDev的A2DP服务
            try {

                          /*
                           * 1.Reflect this method
                             public boolean connect(BluetoothDevice device);
                           *
                           * 2.function definition
                             getMethod(String methodName, Class <?>... paramType)
                           */
                //1.这步相当于定义函数
                method_Connect = clazz.getMethod("connect", BluetoothDevice.class);
                //invoke(object receiver,object... args)
                //2.这步相当于调用函数,invoke需要传入args：BluetoothDevice的实例
                method_Connect.invoke(a2dp, device);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(int profile) {
            // TODO Auto-generated method stub

        }

    }

    private void searchBleByAddress(final String address, final OnScanBleListener onScanBleListener, final OnConnBleListener onConnBleListener) {
        if (!conn){
            mBleClient.search(mRequest, new SearchResponse() {
                @Override
                public void onSearchStarted() {
                    onScanBleListener.onStart();
                }

                @Override
                public void onDeviceFounded(final SearchResult device) {
                    Utils.Logger(TAG,"scan ble name",device.getName());
                    Utils.Logger(TAG,"scan ble mac",device.getAddress());
                    String broadcastPack =Utils.bytesToHex(device.scanRecord);
                    String[] values = broadcastPack.split("FF");
                    if (values.length == 2){
                        broadcastPack = values[1].toString().substring(0,12);
                        //broadcastPack = values[1].toString().substring(0,4);
                        if (address.replace(":","").equals(broadcastPack)){//device.getAddress().substring(8))
                            mBleClient.stopSearch();

                            //broadcastPack = Utils.asciiToString(broadcastPack.substring(0,62));
                            if (!conn){
                                mAddress = device.getAddress();
                                onScanBleListener.onSuccess();
                                //connBle(onConnBleListener);
                                Utils.Logger("find:---","",device.getAddress().toString());
                                conn = true;
                            }
                        }
                    }
//                    if (device.getName().contains("mi")){
//
//                    }

//                if (!TextUtils.isEmpty(address)) {
//                    if (device.getAddress().equalsIgnoreCase(getMacAddress(address))) {
//                        mBleClient.stopSearch();
//                        mAddress = device.getAddress();
//                        onScanBleListener.onSuccess();
//                    }
//                } else if (device.getName().contains(BleUtils.BLE_NAME)) {
//                    mBleClient.stopSearch();
//                    mAddress = device.getAddress();
//                    onScanBleListener.onSuccess();
//                }
                }

                @Override
                public void onSearchStopped() {
                    onScanBleListener.onScanFailure();
                }

                @Override
                public void onSearchCanceled() {

                }
            });
        }else {
            mBleClient.stopSearch();
        }
    }

    private String getMacAddress(String macAddress) {
        String oldChar = macAddress.substring(macAddress.length() - 5, macAddress.length() - 3);
        String newChar = Integer.toHexString(Integer.parseInt(oldChar, 16) + 1);
        Utils.Logger(TAG,"newMac",macAddress.replace(oldChar, newChar));
        return macAddress.replace(oldChar, newChar);
    }

    @Override
    public void connBle(final OnConnBleListener onConnBleListener) {
        mBleClient.connect(mAddress, mOptions, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                if (code == REQUEST_SUCCESS) {
                    List<BleGattService> serviceList = profile.getServices();
                    for (BleGattService service : serviceList) {
                        Utils.Logger("service","UUID",service.getUUID().toString());
                        if (service.getUUID().toString().contains(BleUtils.SERVICE)) {
                            List<BleGattCharacter> characters = service.getCharacters();
                            for (BleGattCharacter character : characters) {
                                //save uuid
                                mServiceUUID = service.getUUID();
                                mSendCharacterUUID = character.getUuid();
                                mNotifyCharacterUUID = character.getUuid();
                                onConnBleListener.onSuccess();
                            }
                        }
                    }
                } else {
                    onConnBleListener.onFailure();
                }
            }
        });
    }

    @Override
    public void writeCommand(String command) {
//        if (mServiceUUID != null && mSendCharacterUUID != null) {
//            divideFrameBleSendData(command.getBytes(),context);
//        }
        divideFrameBleSendData(command.getBytes());
    }

    //分包

    private void divideFrameBleSendData(byte[] data) {
        Utils.Logger(TAG,"BLE Write Command",new String(data));
        int tmpLen = data.length;
        int start = 0;
        int end = 0;
        while (tmpLen > 0) {
            byte[] sendData = new byte[21];
            if (tmpLen >= 20) {
                end += 20;
                sendData = Arrays.copyOfRange(data, start, end);
                start += 20;
                tmpLen -= 20;
            } else {
                end += tmpLen;
                sendData = Arrays.copyOfRange(data, start, end);
                tmpLen = 0;
            }

            mBleClient.write(mAddress, mServiceUUID, mSendCharacterUUID, sendData,
                    code -> {

                    });

        }

        //spp
//        Intent actIntent = new Intent(BluetoothTools.ACTION_DATA_TO_GAME);
//        actIntent.putExtra("editViewData",data);
//        context.sendBroadcast(actIntent);
    }

    @Override
    public void notifyBle(final OnReadListener readListener) {
        if (mServiceUUID != null && mNotifyCharacterUUID != null) {
            mBleClient.notify(mAddress, mServiceUUID, mNotifyCharacterUUID, new BleNotifyResponse() {
                @Override
                public void onNotify(UUID service, UUID character, byte[] value) {
                    Utils.Logger(TAG,"BLE Notify",new String(value));
                    readListener.onRead(value);
                }

                @Override
                public void onResponse(int code) {
                    Utils.Logger(TAG,"BLE Notify code",code+"");
                }
            });
        }
    }

//    @Override
//    public void isBleConnected(final BleConnListener bleListener) {
//        listener = new BleConnectStatusListener(){
//
//            @Override
//            public void onConnectStatusChanged(String mac, int status) {
//                if (status == STATUS_CONNECTED){
//                    bleListener.onConn();
//                }else if (status == STATUS_DISCONNECTED){
//                    bleListener.onDisConn();
//                }
//            }
//        };
//        mBleClient.registerConnectStatusListener(mAddress,listener);
//    }

    @Override
    public void readCommand(final OnReadListener readListener) {
        mBleClient.read(mAddress, mServiceUUID, mSendCharacterUUID, new BleReadResponse() {
            @Override
            public void onResponse(int code, byte[] data) {
                if (code == REQUEST_SUCCESS){
                    readListener.onRead(data);
                }
            }
        });

    }

    @Override
    public void unRegisterConn() {
        mBleClient.disconnect(mAddress);
        mBleClient.unregisterConnectStatusListener(mAddress,listener);
        listener = null;
        conn = false;
    }
}
