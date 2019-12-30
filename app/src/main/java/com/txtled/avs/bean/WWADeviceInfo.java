package com.txtled.avs.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.io.Serializable;

/**
 * Created by Mr.Quan on 2019/12/24.
 */

@Entity(
        indexes = {@Index(value = "ip DESC", unique = true)}
)
public class WWADeviceInfo implements Serializable {
    static final long serialVersionUID = 42L;

    @Id
    private Long id;

    private String ip;//设备ip地址

    private String netMask;//子网掩码

    private String gw;//网关地址

    private String host;//aws endpoint

    private String port;//endpoint端口

    private String cid;//clientId

    private String thing;//事物名称

    private boolean isConfigure;//是否配置

    public WWADeviceInfo() {
    }

    @Generated(hash = 2047763160)
    public WWADeviceInfo(Long id, String ip, String netMask, String gw, String host, String port,
            String cid, String thing, boolean isConfigure) {
        this.id = id;
        this.ip = ip;
        this.netMask = netMask;
        this.gw = gw;
        this.host = host;
        this.port = port;
        this.cid = cid;
        this.thing = thing;
        this.isConfigure = isConfigure;
    }

    public WWADeviceInfo(String ip, String netMask, String gw, String host, String port
            , String cid, String thing) {
        this.ip = ip;
        this.netMask = netMask;
        this.gw = gw;
        this.host = host;
        this.port = port;
        this.cid = cid;
        this.thing = thing;
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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isConfigure() {
        return isConfigure;
    }

    public void setConfigure(boolean configure) {
        isConfigure = configure;
    }

    public boolean getIsConfigure() {
        return this.isConfigure;
    }

    public void setIsConfigure(boolean isConfigure) {
        this.isConfigure = isConfigure;
    }
}
