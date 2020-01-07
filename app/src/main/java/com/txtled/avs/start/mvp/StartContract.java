package com.txtled.avs.start.mvp;

import com.txtled.avs.base.BasePresenter;
import com.txtled.avs.base.BaseView;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public interface StartContract {
    interface View extends BaseView{

    }

    interface Presenter extends BasePresenter<View>{

        void initUid();
    }
}
