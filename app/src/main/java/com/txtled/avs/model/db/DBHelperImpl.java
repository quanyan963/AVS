package com.txtled.avs.model.db;




import org.greenrobot.greendao.database.Database;

import java.util.List;

import javax.inject.Inject;


/**
 * Created by Mr.Quan on 2018/4/17.
 */

public class DBHelperImpl implements DBHelper {
    private static final String DB_NAME = "tsdm.db";
    //private DaoSession mDaoSession;

    @Inject
    public DBHelperImpl() {
//        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(MyApplication.
//                getInstance(), DB_NAME);
//        Database db = openHelper.getWritableDb();
//        mDaoSession = new DaoMaster(db).newSession();
    }
}
