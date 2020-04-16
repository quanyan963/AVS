package com.txtled.avs.config.mvp;

import android.app.Activity;

import com.txtled.avs.base.BasePresenter;
import com.txtled.avs.base.BaseView;

/**
 * Created by Mr.Quan on 2020/4/16.
 */
public interface WifiContract {
    interface View extends BaseView{

        void showSnack(int str);

        void showNetWorkError(int str);

        void showPermissionHint();

        void hidSnackBar();

        void rightWifi();

        void showToast(int str);

        void showWeb();

        void toBindView();

        void getAuth(boolean b);

        void getIp(String ip);
    }

    interface Presenter extends BasePresenter<View>{

        void init(Activity activity);

        void findDevice();

        void checkState();

        void destroy();
    }
}
