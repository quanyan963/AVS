package com.txtled.avs.bind.mvp;

import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.model.DataManagerModel;

import javax.inject.Inject;

/**
 * Created by Mr.Quan on 2020/4/16.
 */
public class BindPresneter extends RxPresenter<BindContract.View> implements BindContract.Presenter {
    private DataManagerModel dataManagerModel;

    @Inject
    public BindPresneter(DataManagerModel dataManagerModel) {
        this.dataManagerModel = dataManagerModel;
    }
}
