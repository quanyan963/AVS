package com.txtled.avs.qr;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.txtled.avs.R;
import com.txtled.avs.base.MvpBaseActivity;
import com.txtled.avs.qr.mvp.QrContract;
import com.txtled.avs.qr.mvp.QrPresenter;

import butterknife.BindView;

/**
 * Created by Mr.Quan on 2020/1/13.
 */
public class QrActivity extends MvpBaseActivity<QrPresenter> implements QrContract.View,
        View.OnClickListener {

    @BindView(R.id.dbv)
    DecoratedBarcodeView dbv;
    private CaptureManager captureManager;

    @Override
    public void setInject() {
        getActivityComponent().inject(this);
    }

    @Override
    public void init() {
        captureManager = new CaptureManager(this, dbv);
        captureManager.initializeFromIntent(getIntent(),savedInstanceState);
        captureManager.decode();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_qr;
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        captureManager.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return dbv.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        super.onPause();
        captureManager.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        captureManager.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        captureManager.onDestroy();
    }
}
