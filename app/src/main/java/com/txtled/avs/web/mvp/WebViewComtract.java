package com.txtled.avs.web.mvp;

import com.txtled.avs.base.BasePresenter;
import com.txtled.avs.base.BaseView;

/**
 * Created by Mr.Quan on 2019/12/9.
 */
public interface WebViewComtract {
    interface View extends BaseView {

    }

    interface Presenter extends BasePresenter<View> {

    }
}
