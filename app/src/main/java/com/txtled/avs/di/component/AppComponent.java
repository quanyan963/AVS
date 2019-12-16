package com.txtled.avs.di.component;


import com.txtled.avs.application.MyApplication;
import com.txtled.avs.di.module.AppModule;
import com.txtled.avs.model.DataManagerModel;
import com.txtled.avs.model.ble.BleHelper;
import com.txtled.avs.model.db.DBHelper;
import com.txtled.avs.model.net.NetHelper;
import com.txtled.avs.model.operate.OperateHelper;
import com.txtled.avs.model.prefs.PreferencesHelper;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by KomoriWu
 * on 2017-09-01.
 */

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    MyApplication getContext();

    DataManagerModel getDataManagerModel();

    DBHelper getDbHelper();

    PreferencesHelper getPreferencesHelper();

    NetHelper getNetHelper();

    OperateHelper getOperateHelper();

    BleHelper getBleHelper();
}
