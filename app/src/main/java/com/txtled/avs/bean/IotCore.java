package com.txtled.avs.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

/**
 * Created by Mr.Quan on 2019/12/26.
 */
@Entity
public class IotCore implements Serializable {
    static final long serialVersionUID = 42L;

    @Id
    private Long id;

    private String endpoint;//thingName

    private String thingName;//friendlyName

    private String ca;

    private String cert;

    private String key;

    @Generated(hash = 854865897)
    public IotCore(Long id, String endpoint, String thingName, String ca, String cert, String key) {
        this.id = id;
        this.endpoint = endpoint;
        this.thingName = thingName;
        this.ca = ca;
        this.cert = cert;
        this.key = key;
    }

    public IotCore() {
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getThingName() {
        return thingName;
    }

    public void setThingName(String thingName) {
        this.thingName = thingName;
    }

    public String getCa() {
        return ca;
    }

    public void setCa(String ca) {
        this.ca = ca;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
