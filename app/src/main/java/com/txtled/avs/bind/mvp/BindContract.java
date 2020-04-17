package com.txtled.avs.bind.mvp;

import android.app.Activity;

import com.txtled.avs.base.BasePresenter;
import com.txtled.avs.base.BaseView;
import com.txtled.avs.bind.BindActivity;

/**
 * Created by Mr.Quan on 2020/4/16.
 */
public interface BindContract {
    interface View extends BaseView{

        void showSnack(int str);

        void showNetWorkError(int str);

        void hidSnackBar();

        void openWifi();

        void showLoadingView();

        void showToast(int str);

        void hidProgress();

        void bindDevice(String mCode);

        void timeOut();

        void showNetDisable();

        //void find();

        void showAuthLoadingView();
    }
    interface Presenter extends BasePresenter<View>{

        void init(Activity activity, String ip);

        void findDevice();

        void onDestroy();

        void connSocket(String sendMsg);

        void checkState();

        void resume();
    }
}
