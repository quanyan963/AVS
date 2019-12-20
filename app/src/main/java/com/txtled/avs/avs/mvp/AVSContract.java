package com.txtled.avs.avs.mvp;

import android.app.Activity;
import android.content.Context;

import com.txtled.avs.avs.listener.OnSearchListener;
import com.txtled.avs.base.BasePresenter;
import com.txtled.avs.base.BaseView;
import com.txtled.avs.bean.DeviceHostInfo;

import java.util.ArrayList;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public interface AVSContract {
    interface View extends BaseView {
        void showAlertDialog(Exception exception);

        void setAdapter(int count);

        void initAdapter(ArrayList<DeviceHostInfo> infos);

        void bindDevice(String mCode);

        void hidProgress();

        void showNetWorkError();

        void closeRefresh(int i);
    }

    interface Presenter extends BasePresenter<View> {

        void refresh(Activity activity);

        void initAmazon(Activity activity, Context context);

        void onItemClick(int position);

        void resume();

        void startSocket();

        void destroy();
    }
}
