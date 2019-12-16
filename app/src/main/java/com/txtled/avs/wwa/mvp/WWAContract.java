package com.txtled.avs.wwa.mvp;

import com.txtled.avs.base.BasePresenter;
import com.txtled.avs.base.BaseView;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public interface WWAContract {
    interface View extends BaseView{

    }
    interface Presenter extends BasePresenter<View>{

    }
}
