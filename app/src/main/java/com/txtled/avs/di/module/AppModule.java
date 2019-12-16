package com.txtled.avs.di.module;




import com.txtled.avs.application.MyApplication;
import com.txtled.avs.model.DataManagerModel;
import com.txtled.avs.model.ble.BleHelper;
import com.txtled.avs.model.ble.BleHelperImpl;
import com.txtled.avs.model.db.DBHelper;
import com.txtled.avs.model.db.DBHelperImpl;
import com.txtled.avs.model.net.NetHelper;
import com.txtled.avs.model.net.NetHelperImpl;
import com.txtled.avs.model.operate.OperateHelper;
import com.txtled.avs.model.operate.OperateHelperImpl;
import com.txtled.avs.model.prefs.PreferencesHelper;
import com.txtled.avs.model.prefs.PreferencesHelperImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by KomoriWu
 * on 2017/9/15.
 */
@Module
public class AppModule {
    private MyApplication myApplication;

    public AppModule(MyApplication myApplication) {
        this.myApplication = myApplication;
    }

    @Provides
    @Singleton
    MyApplication provideMyApplication() {
        return myApplication;
    }

    @Provides
    @Singleton
    DBHelper provideDBHelper(DBHelperImpl dbHelper) {
        return dbHelper;
    }

    @Provides
    @Singleton
    PreferencesHelper providePreferencesHelper(PreferencesHelperImpl preferencesHelper) {
        return preferencesHelper;
    }

    @Provides
    @Singleton
    NetHelper provideNetHelper(NetHelperImpl netHelper) {
        return netHelper;
    }

    @Provides
    @Singleton
    OperateHelper provideOperateHelper(OperateHelperImpl operateHelper) {
        return operateHelper;
    }

    @Provides
    @Singleton
    BleHelper provideBleHelper(BleHelperImpl bleHelper) {
        return bleHelper;
    }

    @Provides
    @Singleton
    DataManagerModel provideDataManagerModel(DBHelperImpl dbHelper,
                                             PreferencesHelperImpl preferencesHelper,
                                             NetHelperImpl netHelper, OperateHelperImpl operateHelper,
                                             BleHelperImpl bleHelper) {
        return new DataManagerModel(dbHelper, preferencesHelper,netHelper,operateHelper,bleHelper);
    }
}
