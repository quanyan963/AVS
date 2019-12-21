package com.txtled.avs.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

/**
 * Created by Mr.Quan on 2019/12/21.
 */
@Entity
public class WWAInfo implements Serializable {
    static final long serialVersionUID = 42L;
    @Id
    private Long id;

    private String bSSId;

    private String hostAddress;

    public WWAInfo() {
    }

    @Generated(hash = 1681514034)
    public WWAInfo(Long id, String bSSId, String hostAddress) {
        this.id = id;
        this.bSSId = bSSId;
        this.hostAddress = hostAddress;
    }

    public WWAInfo(String bSSId, String hostAddress) {
        this.bSSId = bSSId;
        this.hostAddress = hostAddress;
    }

    public String getbSSId() {
        return bSSId;
    }

    public void setbSSId(String bSSId) {
        this.bSSId = bSSId;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBSSId() {
        return this.bSSId;
    }

    public void setBSSId(String bSSId) {
        this.bSSId = bSSId;
    }
}
