package com.txtled.avs.bind;

import android.content.Intent;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.txtled.avs.R;
import com.txtled.avs.base.MvpBaseActivity;
import com.txtled.avs.bind.mvp.BindContract;
import com.txtled.avs.bind.mvp.BindPresenter;
import com.txtled.avs.config.WifiConfigActivity;
import com.txtled.avs.utils.AlertUtils;
import com.txtled.avs.web.WebViewActivity;

import butterknife.BindView;

import static com.txtled.avs.utils.Constants.BIND_URL;
import static com.txtled.avs.utils.Constants.CODE;
import static com.txtled.avs.utils.Constants.IP;
import static com.txtled.avs.utils.Constants.IS_AUTH;
import static com.txtled.avs.utils.Constants.PSK;
import static com.txtled.avs.utils.Constants.REQUEST_CODE_INTERNET_SETTINGS;
import static com.txtled.avs.utils.Constants.REQUEST_CODE_QR;
import static com.txtled.avs.utils.Constants.REQUEST_CODE_WIFI_SETTINGS;
import static com.txtled.avs.utils.Constants.RESET_DEVICE;
import static com.txtled.avs.utils.Constants.SSID;
import static com.txtled.avs.utils.Constants.TYPE;
import static com.txtled.avs.utils.Constants.WEB_URL;

/**
 * Created by Mr.Quan on 2020/4/16.
 */
public class BindActivity extends MvpBaseActivity<BindPresenter> implements BindContract.View,
        View.OnClickListener {
    @BindView(R.id.btn_bind)
    MaterialButton btnBind;
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
    @BindView(R.id.tv_bind_hint)
    TextView tvBindHint;
    private String ip,ssId,psk;
    private boolean isAuth,isVisible,isOk,hasResult;
    private AlertDialog dialog;
    private String code;
    @Override
    public void setInject() {
        getActivityComponent().inject(this);
    }

    @Override
    public void init() {
        initToolbar();
        setNavigationIcon(true);
        tvTitle.setText(R.string.bind);
        Intent intent = getIntent();
        ip = intent.getStringExtra(IP);
        ssId = intent.getStringExtra(SSID);
        psk = intent.getStringExtra(PSK);
        isAuth = intent.getBooleanExtra(IS_AUTH, false);
        if (isAuth) {
            tvBindHint.setText(R.string.bound);
            btnBind.setText(R.string.unbind_device);
        }else {
            tvBindHint.setText(R.string.second);
            btnBind.setText(R.string.bind_device);
        }
        btnBind.setOnClickListener(this);
        btnChangeWifi.setOnClickListener(this);
        tvAvsBind.setOnClickListener(this);
        presenter.init(this,ip);

        presenter.changeWifi(ssId,psk);
        //presenter.checkState();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_bind;
    }

    /**
     * 显示底部弹框
     * @param str 要显示的内容
     */
    @Override
    public void showSnack(int str) {
        hidSnackBar();
        hidProgress();
        showSnackBar(btnChangeWifi, str,
                R.string.ok, v -> hidSnackBar());
    }

    @Override
    public void showNetWorkError(int str) {
        hidSnackBar();
        hidProgress();
//        showSnackBar(btnChangeWifi, str, R.string.go, v -> {
//            Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//            startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
//        });
        Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
    }

    @Override
    public void hidSnackBar() {
        hideSnackBar();
    }

    /**
     * 跳转至wifi设置界面
     */
    @Override
    public void openWifi() {
        hideSnackBar();
        hidProgress();
//        showSnackBar(btnChangeWifi, R.string.no_conn_wifi, R.string.go, new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
//            }
//        });
        Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
    }

    @Override
    public void showLoadingView() {
        //isOk = true;
//        if (!isVisible && !btnBind.isEnabled()){
//            dialog = AlertUtils.showLoadingDialog(BindActivity.this,R.layout.alert_progress);
//            dialog.show();
//            presenter.findDevice();
//            hidSnackBar();
//            //isOk = false;
//        }
        if (dialog == null){
            dialog = AlertUtils.showLoadingDialog(BindActivity.this,R.layout.alert_progress);
            dialog.show();
        }
        presenter.findDevice();
        hidSnackBar();

    }

    @Override
    public void showToast(int str) {
        runOnUiThread(() -> {
            hidSnackBar();
            hidProgress();
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void showToast(String str) {
        hidSnackBar();
        hidProgress();
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    @Override
    public void hidProgress() {
        if (dialog != null){
            dialog.dismiss();
            dialog = null;
        }
    }

    /**
     * 显示验证码界面
     * @param mCode
     */
    @Override
    public void bindDevice(String mCode) {
        code = mCode;
        runOnUiThread(() -> {
            tvBindHint.setVisibility(View.GONE);
            setViewVisible(true);
            tvAvsCode.setText(mCode);
            hidProgress();

        });
    }

    private void setViewVisible(boolean b) {
        rlAvsBind.setVisibility(b ? View.VISIBLE : View.GONE);
        btnChangeWifi.setVisibility(b ? View.GONE : View.VISIBLE);
        btnBind.setVisibility(b ? View.GONE : View.VISIBLE);
        //tvAvsBind.setVisibility(b ? View.GONE : View.VISIBLE);
        if (b){
            rlAvsBind.bringToFront();
        }else {
            btnBind.bringToFront();
            btnChangeWifi.bringToFront();
            //tvAvsBind.bringToFront();
        }
    }

    @Override
    public void timeOut() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideSnackBar();
                hidProgress();
                //setEnabled(false);
                showSnackBar(btnChangeWifi, R.string.not_found_try_again, R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialog == null){
                            dialog = AlertUtils.showLoadingDialog(BindActivity.this,R.layout.alert_progress);
                            dialog.show();
                        }
                        presenter.findDevice();
                    }
                });
            }
        });
    }

    @Override
    public void showNetDisable() {
        hideSnackBar();
        hidProgress();
//        setEnabled(false);
//        showSnackBar(btnChangeWifi, R.string.net_unavailable, R.string.go, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
//            }
//        });
        Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
    }

//    @Override
//    public void find() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                hidProgress();
//                //setEnabled(true);
//            }
//        });
//    }

    @Override
    public void showAuthLoadingView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog == null){
                    dialog = AlertUtils.showLoadingDialog(BindActivity.this,R.layout.alert_progress);
                    dialog.show();
                }
                hidSnackBar();
            }
        });
    }

    @Override
    public void noResult() {
        hidProgress();
        hidSnackBar();
        Toast.makeText(this,R.string.request_timeout,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void unBind() {
        runOnUiThread(() -> tvBindHint.setText(R.string.second));
    }

//    private void setEnabled(boolean b){
//        btnBind.setEnabled(b);
//        btnChangeWifi.setEnabled(b);
//        tvAvsBind.setEnabled(b);
//    }

    @Override
    protected void onPause() {
        isVisible = true;
        super.onPause();
    }

    @Override
    protected void onResume() {

//        if (isVisible){
//            presenter.checkState();
//            //showLoadingView();
//            isVisible = false;
//        }
        presenter.resume();
        if (isVisible = true && dialog != null && dialog.isShowing()){
            isVisible = false;
            presenter.startCount();
        }
        super.onResume();
    }

    /**
     * 所有按钮点击事件
     * @param v 控件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_bind:
                showAuthLoadingView();
                presenter.checkState();
                //presenter.connSocket(RESET_DEVICE);
                break;
            case R.id.btn_change_wifi:
                startActivity(new Intent(this, WifiConfigActivity.class).putExtra(TYPE,1));
                this.finish();
                break;
            case R.id.tv_avs_bind:
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WEB_URL, BIND_URL);
                intent.putExtra(CODE,code);
                startActivityForResult(intent, REQUEST_CODE_INTERNET_SETTINGS);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_INTERNET_SETTINGS) {
            bindSuccess();
        }else if (requestCode == REQUEST_CODE_QR){
            //扫描结果
            IntentResult scanResult = IntentIntegrator.parseActivityResult(resultCode, data);
            final String qrContent = scanResult.getContents();
            Toast.makeText(this, "扫描结果:" + qrContent, Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void bindSuccess() {
        tvAvsCode.setVisibility(View.GONE);
        tvAvsBind.setVisibility(View.GONE);
        tvAvsCodeHint.setText(R.string.avs_success);
    }

    @Override
    public void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,WifiConfigActivity.class));
        this.finish();
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (snackbar != null && snackbar.isShown()){
            hidSnackBar();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
