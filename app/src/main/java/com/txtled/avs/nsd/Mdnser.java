package com.txtled.avs.nsd;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Locale;


/**
 * Created by zhenhua.he on 2017/9/27.
 */

public class Mdnser {
    public final String TAG = "Mdnser";
    public NsdManager mNsdManager;
    public final String SERVICE_TYPE = "_ssh._tcp.";
    public NsdManager.DiscoveryListener mDiscoveryListener;
    public mResloveListener mr;
    public ArrayList<NsdServiceInfo> nsdServiceInfos;
    public ArrayList<DeviceHostInfo> ipInfos;
    public int count;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public Mdnser(Context context){
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        ipInfos = new ArrayList<DeviceHostInfo>();
    }
    //扫描
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initializeDiscoveryListener() {
        count=0;
        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.e(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {


                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.e(TAG, "Unknown Service Type: " + service.getServiceType());
                }

                mr = new mResloveListener();

                mNsdManager.resolveService(service, mr);
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);

//                ipInfos.remove(service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.e(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }
    //连接
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    class mResloveListener implements ResolveListener {
        InetAddress inetAddress;



        @Override
        public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
            Log.e(TAG,"Resolve failed" + nsdServiceInfo + "code" + i);
            mDiscoveryListener.onServiceLost(nsdServiceInfo);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {

            String mac_s = "";
            inetAddress = nsdServiceInfo.getHost();

            if (inetAddress instanceof Inet4Address) {
                Log.e("SUCCESS","addr is "+ inetAddress.getHostAddress());

                mac_s = readArp(inetAddress.getHostAddress());
                ipInfos.add(new DeviceHostInfo(nsdServiceInfo.getServiceName(),inetAddress.getHostAddress(), mac_s));
            }
            else{
                Log.e("FAILED", "addr is IPV6!" + inetAddress.getHostAddress());
//               ipInfos.add(inetAddress.getHostAddress());
            }
       }
    }

    private String readArp(String ip_get) {
        try {
            BufferedReader br = new BufferedReader(
                    new FileReader("/proc/net/arp"));
            String line = "";
            String ip = "";
            String flag = "";
            String mac = "";

            while ((line = br.readLine()) != null) {
                try {
                    line = line.trim();
                    if (line.length() < 63) continue;
                    if (line.toUpperCase(Locale.US).contains("IP")) continue;
                    ip = line.substring(0, 17).trim();
                    flag = line.substring(29, 32).trim();
                    mac = line.substring(41, 63).trim();
                    if (ip.equals(ip_get)) {
                        return mac;
                    }
                    if (mac.contains("00:00:00:00:00:00")) continue;
                    Log.e("scanner", "readArp: mac= "+mac+" ; ip= "+ip+" ;flag= "+flag);
                } catch (Exception e) {
                }
            }
            br.close();

        } catch(Exception e) {
        }
        return "";
    }

    public class DeviceHostInfo{
        private final String hostname;
        private final String hostip;
        private final String hostmac;
        public DeviceHostInfo(String hostname, String hostip, String hostmac){
            this.hostname=hostname;
            this.hostip = hostip;
            this.hostmac = hostmac;
        }

        public String getHostip() {
            return hostip;
        }

        public String getHostname() {
            return hostname;
        }

        public String getHostmac() { return hostmac; }
    }
}
