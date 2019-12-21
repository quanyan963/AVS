package com.txtled.avs.model.db;




import com.txtled.avs.application.MyApplication;
import com.txtled.avs.bean.WWAInfo;
import com.txtled.avs.bean.dao.DaoMaster;
import com.txtled.avs.bean.dao.DaoSession;

import org.greenrobot.greendao.database.Database;

import java.util.List;

import javax.inject.Inject;


/**
 * Created by Mr.Quan on 2018/4/17.
 */

public class DBHelperImpl implements DBHelper {
    private static final String DB_NAME = "tsdm.db";
    private DaoSession mDaoSession;

    @Inject
    public DBHelperImpl() {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(MyApplication.
                getInstance(), DB_NAME);
        Database db = openHelper.getWritableDb();
        mDaoSession = new DaoMaster(db).newSession();
    }

    @Override
    public void insertWWAInfo(List<WWAInfo> infoList) {
        mDaoSession.getWWAInfoDao().insertInTx(infoList);
    }

    @Override
    public List<WWAInfo> getWWAInfo() {
        return mDaoSession.getWWAInfoDao().loadAll();
    }

    @Override
    public void deleteWWAInfo() {
        mDaoSession.getWWAInfoDao().deleteAll();
    }

    @Override
    public void upDataWWAInfo(WWAInfo info) {
        mDaoSession.getWWAInfoDao().update(info);
    }
}
