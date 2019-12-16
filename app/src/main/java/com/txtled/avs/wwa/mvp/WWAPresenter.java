package com.txtled.avs.wwa.mvp;

import android.app.Activity;

import com.txtled.avs.base.RxPresenter;
import com.txtled.avs.model.DataManagerModel;
import com.txtled.avs.model.ble.BleHelper;
import com.txtled.avs.model.operate.OperateHelper;
import com.txtled.avs.utils.Constants;

import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public class WWAPresenter extends RxPresenter<WWAContract.View> implements WWAContract.Presenter {
    private DataManagerModel mDataManagerModel;

    @Inject
    public WWAPresenter(DataManagerModel mDataManagerModel) {
        this.mDataManagerModel = mDataManagerModel;
    }

    @Override
    public void checkPermission(Activity activity) {
        String[] permissions = {Constants.permissions[0],Constants.permissions[1]};
        mDataManagerModel.requestPermissions(activity, permissions, new OperateHelper.OnPermissionsListener() {
            @Override
            public void onSuccess(String name) {
                if (name.equals(Constants.permissions[1])){
                    scanBle();
                }
            }

            @Override
            public void onFailure() {

            }

            @Override
            public void onAskAgain() {

            }
        });
    }

    private void scanBle(boolean isSpecified) {
        mDataManagerModel.scanBle(isSpecified, new BleHelper.OnScanBleListener() {
            @Override
            public void onStart() {
                view.startAnim();
            }

            @Override
            public void onSuccess() {
                mDataManagerModel.connBle(new BleHelper.OnConnBleListener() {
                    @Override
                    public void onSuccess() {
                        mDataManagerModel.notifyBle();
                        addSubscribe(Flowable.timer(DELAY, TimeUnit.MILLISECONDS)
                                .compose(RxUtil.<Long>rxSchedulerHelper())
                                .doOnSubscribe(new Consumer<Subscription>() {
                                    @Override
                                    public void accept(Subscription subscription) throws Exception {
                                        view.connected();
                                    }
                                })
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) throws Exception {
                                        view.toMainView();
                                    }
                                }));
                    }

                    @Override
                    public void onFailure() {
                        view.connFailure();
                    }
                });
            }

            @Override
            public void onScanFailure() {
                view.scanFailure();
            }

            @Override
            public void onDisOpenDevice() {
                if (!mDataManagerModel.getInitDialog()) {
                    view.showOpenDeviceDialog();
                }else {
                    scanBle(false);
                }
            }

            @Override
            public void onDisOpenBle() {
                view.onShowOpenBleDialog();
            }

            @Override
            public void onDisSupported() {
                view.onShowError();
            }
        });
    }
}
