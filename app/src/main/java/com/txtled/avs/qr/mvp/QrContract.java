package com.txtled.avs.qr.mvp;

import com.txtled.avs.base.BasePresenter;
import com.txtled.avs.base.BaseView;

/**
 * Created by Mr.Quan on 2020/1/13.
 */
public interface QrContract {
    interface View extends BaseView{

    }

    interface Presenter extends BasePresenter<View>{

    }
}
