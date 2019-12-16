package com.txtled.avs.web.mvp;

import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.model.DataManagerModel;

import javax.inject.Inject;

/**
 * Created by Mr.Quan on 2019/12/9.
 */
public class WebViewPresenter extends RxPresenter<WebViewComtract.View> implements WebViewComtract.Presenter {
    private DataManagerModel mDataManagerModel;

    @Inject
    public WebViewPresenter(DataManagerModel mDataManagerModel) {
        this.mDataManagerModel = mDataManagerModel;
    }
}
