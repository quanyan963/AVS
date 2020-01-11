package com.txtled.avs.bean;

import java.io.Serializable;

/**
 * Created by Mr.Quan on 2019/12/24.
 */

public class WWADeviceInfo implements Serializable {

    private String ip;//设备ip地址

    private String netMask;//子网掩码

    private String gw;//网关地址

    private String host;//aws endpoint  a311cdvk7hqtsk-ats.iot.us-east-1.amazonaws.com

    private String port;//endpoint端口 8883  443

    private String cid;//userId  随机

    private String thing;//事物名称  thingName

    private String friendlyNames;//用户对应名称

    private String ver;//avs版本号

   public WWADeviceInfo() {
    }

    public WWADeviceInfo(String ip, String netMask, String gw, String host, String port
            , String cid, String thing, String friendlyNames, String ver) {
        this.ip = ip;
        this.netMask = netMask;
        this.gw = gw;
        this.host = host;
        this.port = port;
        this.cid = cid;
        this.thing = thing;
        this.friendlyNames = friendlyNames;
        this.ver = ver;
    }

    public String getFriendlyNames() {
        return friendlyNames;
    }

    public void setFriendlyNames(String friendlyNames) {
        this.friendlyNames = friendlyNames;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNetMask() {
        return netMask;
    }

    public void setNetMask(String netMask) {
        this.netMask = netMask;
    }

    public String getGw() {
        return gw;
    }

    public void setGw(String gw) {
        this.gw = gw;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getThing() {
        return thing;
    }

    public void setThing(String thing) {
        this.thing = thing;
    }

    public String getVer() {
        return this.ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }
}
