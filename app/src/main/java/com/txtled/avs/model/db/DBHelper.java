package com.txtled.avs.model.db;

import com.txtled.avs.bean.WWADeviceInfo;
import com.txtled.avs.bean.WWAInfo;

import java.util.List;

/**
 * Created by Mr.Quan on 2018/4/17.
 */

public interface DBHelper {
    void insertWWAInfo(List<WWADeviceInfo> infoList);

    List<WWADeviceInfo> getWWAInfo();

    void deleteWWAInfo();

    void upDataWWAInfo(WWADeviceInfo info);

}
