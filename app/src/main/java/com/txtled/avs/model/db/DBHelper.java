package com.txtled.avs.model.db;

import com.txtled.avs.bean.WWAInfo;

import java.util.List;

/**
 * Created by Mr.Quan on 2018/4/17.
 */

public interface DBHelper {
    void insertWWAInfo(List<WWAInfo> infoList);

    List<WWAInfo> getWWAInfo();

    void deleteWWAInfo();

    void upDataWWAInfo(WWAInfo info);

}
