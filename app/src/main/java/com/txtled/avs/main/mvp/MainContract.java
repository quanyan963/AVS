package com.txtled.avs.main.mvp;

import android.app.Activity;

import com.txtled.avs.base.BasePresenter;
import com.txtled.avs.base.BaseView;

/**
 * Created by Mr.Quan on 2019/12/9.
 */
public interface MainContract {

    interface View extends BaseView {

        void openWifi();

        void openLocation();

        void toWebView();

        void networkNoteMatch();

        void toAVS();

        void toWWA();
    }

    interface Presenter extends BasePresenter<View> {
        void getWifiSSid(Activity activity);

        void switchFragment(int i);
    }
}
