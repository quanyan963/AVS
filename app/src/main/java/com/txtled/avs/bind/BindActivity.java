package com.txtled.avs.bind;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.txtled.avs.R;
import com.txtled.avs.base.MvpBaseActivity;
import com.txtled.avs.bind.mvp.BindContract;
import com.txtled.avs.bind.mvp.BindPresneter;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.txtled.avs.utils.Constants.IP;
import static com.txtled.avs.utils.Constants.IS_AUTH;

/**
 * Created by Mr.Quan on 2020/4/16.
 */
public class BindActivity extends MvpBaseActivity<BindPresneter> implements BindContract.View {
    @BindView(R.id.btn_bind)
    MaterialButton btnBind;
    @BindView(R.id.btn_unbind)
    MaterialButton btnUnbind;
    @BindView(R.id.rl_btn)
    RelativeLayout rlBtn;
    @BindView(R.id.btn_change_wifi)
    MaterialButton btnChangeWifi;
    @BindView(R.id.tv_avs_code)
    TextView tvAvsCode;
    @BindView(R.id.tv_avs_code_hint)
    TextView tvAvsCodeHint;
    @BindView(R.id.tv_avs_bind)
    MaterialButton tvAvsBind;
    @BindView(R.id.rl_avs_bind)
    RelativeLayout rlAvsBind;
    private String ip;
    private boolean isAuth;

    @Override
    public void setInject() {
        getActivityComponent().inject(this);
    }

    @Override
    public void init() {
        initToolbar();
        tvTitle.setText(R.string.bind);
        Intent intent = getIntent();
        ip = intent.getStringExtra(IP);
        isAuth = intent.getBooleanExtra(IS_AUTH, false);
        if ()
    }

    @Override
    public int getLayout() {
        return R.layout.activity_bind;
    }

}
