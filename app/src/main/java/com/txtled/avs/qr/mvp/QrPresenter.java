package com.txtled.avs.qr.mvp;

import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.model.DataManagerModel;

import javax.inject.Inject;

/**
 * Created by Mr.Quan on 2020/1/13.
 */
public class QrPresenter extends RxPresenter<QrContract.View> implements QrContract.Presenter {
    private DataManagerModel mDataManagerModel;

    @Inject
    public QrPresenter(DataManagerModel mDataManagerModel) {
        this.mDataManagerModel = mDataManagerModel;
    }

}
