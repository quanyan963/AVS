package com.txtled.avs.start.mvp;

import com.txtled.avs.R;
import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.model.DataManagerModel;

import javax.inject.Inject;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public class StartPresenter extends RxPresenter<StartContract.View> implements StartContract.Presenter {
    private DataManagerModel mDataManagerModel;

    @Inject
    public StartPresenter(DataManagerModel mDataManagerModel) {
        this.mDataManagerModel = mDataManagerModel;
    }
}
