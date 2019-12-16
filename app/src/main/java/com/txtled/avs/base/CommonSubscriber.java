package com.txtled.avs.base;

import android.text.TextUtils;

import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by KomoriWu
 * on 2017-09-22.
 */

public abstract class CommonSubscriber<T> extends ResourceSubscriber<T> {
    private BaseView mView;
    private String mErrorMsg;
    private boolean isShowError = true;

    protected CommonSubscriber(BaseView view) {
        this.mView = view;
    }

    protected CommonSubscriber(BaseView view, String errorMsg) {
        this.mView = view;
        this.mErrorMsg = errorMsg;
    }

    protected CommonSubscriber(BaseView view, boolean isShowError) {
        this.mView = view;
        this.isShowError = isShowError;
    }

    protected CommonSubscriber(BaseView view, String errorMsg, boolean isShowError) {
        this.mView = view;
        this.mErrorMsg = errorMsg;
        this.isShowError = isShowError;
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onError(Throwable e) {
        if (mView == null) {
            return;
        }

        if (isShowError && mErrorMsg != null && !TextUtils.isEmpty(mErrorMsg)) {
//            mView.showErrorMsg(mErrorMsg);
        } else {
//            mView.showErrorMsg("未知错误");
        }
    }
}
