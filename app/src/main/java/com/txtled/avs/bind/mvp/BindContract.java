package com.txtled.avs.bind.mvp;

import com.txtled.avs.base.BasePresenter;
import com.txtled.avs.base.BaseView;

/**
 * Created by Mr.Quan on 2020/4/16.
 */
public interface BindContract {
    interface View extends BaseView{

    }
    interface Presenter extends BasePresenter<View>{

    }
}
