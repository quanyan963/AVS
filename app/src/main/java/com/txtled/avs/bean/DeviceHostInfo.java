package com.txtled.avs.bean;

/**
 * Created by Mr.Quan on 2019/12/11.
 */
public class DeviceHostInfo {
    private final String hostname;
    private final String hostip;
    private final String hostmac;

    public DeviceHostInfo(String hostname, String hostip, String hostmac) {
        this.hostname = hostname;
        this.hostip = hostip;
        this.hostmac = hostmac;
    }

    public String getHostip() {
        return hostip;
    }

    public String getHostname() {
        return hostname;
    }

    public String getHostmac() {
        return hostmac;
    }
}
