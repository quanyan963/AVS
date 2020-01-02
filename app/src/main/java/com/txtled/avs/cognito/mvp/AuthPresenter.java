package com.txtled.avs.cognito.mvp;

import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.model.DataManagerModel;

import javax.inject.Inject;

/**
 * Created by Mr.Quan on 2019/12/31.
 */
public class AuthPresenter extends RxPresenter<AuthContract.View> implements AuthContract.Presenter {
    private DataManagerModel mDataManagerModel;

    @Inject
    public AuthPresenter(DataManagerModel mDataManagerModel) {
        this.mDataManagerModel = mDataManagerModel;
    }

}
