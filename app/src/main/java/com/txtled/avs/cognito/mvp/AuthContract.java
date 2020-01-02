package com.txtled.avs.cognito.mvp;

import com.txtled.avs.base.BasePresenter;
import com.txtled.avs.base.BaseView;

/**
 * Created by Mr.Quan on 2019/12/31.
 */
public interface AuthContract {
    interface View extends BaseView{

    }
    interface Presenter extends BasePresenter<View>{

    }
}
