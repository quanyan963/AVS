package com.txtled.avs.config;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.txtled.avs.R;
import com.txtled.avs.base.MvpBaseActivity;
import com.txtled.avs.bind.BindActivity;
import com.txtled.avs.config.mvp.WifiContract;
import com.txtled.avs.config.mvp.WifiPresenter;
import com.txtled.avs.utils.AlertUtils;
import com.txtled.avs.utils.HuaweiUtils;
import com.txtled.avs.utils.MeizuUtils;
import com.txtled.avs.utils.MiuiUtils;
import com.txtled.avs.utils.OppoUtils;
import com.txtled.avs.utils.QikuUtils;
import com.txtled.avs.utils.RomUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.txtled.avs.utils.Constants.AVS_WIFI_URL;
import static com.txtled.avs.utils.Constants.IP;
import static com.txtled.avs.utils.Constants.IS_AUTH;
import static com.txtled.avs.utils.Constants.PSK;
import static com.txtled.avs.utils.Constants.REQUEST_CODE_WIFI_SETTINGS;
import static com.txtled.avs.utils.Constants.SSID;
import static com.txtled.avs.utils.Constants.TYPE;

/**
 * Created by Mr.Quan on 2020/4/16.
 */
public class WifiConfigActivity extends MvpBaseActivity<WifiPresenter> implements
        WifiContract.View, View.OnClickListener {
    @BindView(R.id.pb_web)
    ProgressBar pbWeb;
    @BindView(R.id.web_main)
    WebView webMain;
    @BindView(R.id.tv_wifi_hint)
    TextView tvWifiHint;
    @BindView(R.id.btn_web_config)
    MaterialButton btnWebConfig;
    @BindView(R.id.tv_company)
    TextView tvCompany;
    private AlertDialog dialog;
    private boolean isAuth, isVisible, isOk;
    private String ip,ssId,psk;
    private int type;

    @Override
    public void setInject() {
        getActivityComponent().inject(this);
    }

    @Override
    public void init() {
        Intent intent = getIntent();
        type = intent.getIntExtra(TYPE,0);
        tvWifiHint.setVisibility(View.VISIBLE);
        btnWebConfig.setVisibility(View.VISIBLE);
        tvCompany.setVisibility(View.VISIBLE);
        webMain.setVisibility(View.GONE);
        try {
            tvCompany.setText(String.format(getString(R.string.designed_by),
                    getPackageManager().getPackageInfo(getPackageName(),0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tvWifiHint.setTextColor(getResources().getColor(R.color.colorPrimary));
        webMain.clearMatches();
        webMain.clearHistory();
        webMain.clearCache(true);
        webMain.clearFormData();
        webMain.clearSslPreferences();
        btnWebConfig.setOnClickListener(this);

        WebSettings settings = webMain.getSettings();
        // 此方法需要启用JavaScript
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        webMain.setWebViewClient(new WebViewClient() {

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                view.loadUrl(url);
                return true;
            }
        });
        webMain.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    pbWeb.setVisibility(View.GONE);
                } else {
                    pbWeb.setVisibility(View.VISIBLE);
                    pbWeb.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        webMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getX() < 250 && event.getY() < 250) {
                        webMain.setVisibility(View.GONE);
                        tvWifiHint.setVisibility(View.VISIBLE);
                        btnWebConfig.setVisibility(View.VISIBLE);
                        tvCompany.setVisibility(View.VISIBLE);
                        tvWifiHint.bringToFront();
                        btnWebConfig.bringToFront();
                        tvCompany.bringToFront();
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        });

        presenter.init(this);
        //presenter.checkState();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_web;
    }

    @Override
    public void onClick(View v) {
//        webMain.setVisibility(View.VISIBLE);
//        webMain.bringToFront();
//        tvWifiHint.setVisibility(View.GONE);
//        btnWebConfig.setVisibility(View.GONE);
//        tvCompany.setVisibility(View.GONE);
//        pbWeb.bringToFront();
//
//        webMain.loadUrl(AVS_WIFI_URL);

        //presenter.senResetIp();
//        if (ip != null){
//            presenter.senResetIp();
//            //toBindView();
//        }else
//        if (isOk){
//            presenter.senResetIp();
//        }else {
//            presenter.checkState();
//        }
        presenter.checkState();
    }

    @Override
    public void showSnack(int str) {
        isVisible = false;
        tvWifiHint.setVisibility(View.VISIBLE);
        btnWebConfig.setVisibility(View.VISIBLE);
        tvCompany.setVisibility(View.VISIBLE);
        webMain.setVisibility(View.GONE);
        tvWifiHint.bringToFront();
        btnWebConfig.bringToFront();
        tvCompany.bringToFront();
        hidSnackBar();
        showSnackBar(webMain, str,
                R.string.ok, v -> hidSnackBar());
    }

    @Override
    public void showNetWorkError(int str) {
        tvWifiHint.setVisibility(View.VISIBLE);
        btnWebConfig.setVisibility(View.VISIBLE);
        tvCompany.setVisibility(View.VISIBLE);
        webMain.setVisibility(View.GONE);
        tvWifiHint.bringToFront();
        btnWebConfig.bringToFront();
        tvCompany.bringToFront();
        if (!isVisible){
            Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
            hidSnackBar();
        }
        isVisible = false;
        //hidSnackBar();
//        showSnackBar(webMain, str, R.string.go, v -> {
//            Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//            startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
//            hidSnackBar();
//        });
    }

    @Override
    public void showPermissionHint() {
        isVisible = false;
        tvWifiHint.setVisibility(View.VISIBLE);
        btnWebConfig.setVisibility(View.VISIBLE);
        tvCompany.setVisibility(View.VISIBLE);
        webMain.setVisibility(View.GONE);
        tvWifiHint.bringToFront();
        btnWebConfig.bringToFront();
        tvCompany.bringToFront();
        hideSnackBar();
        showSnackBar(webMain, R.string.permission_hint, R.string.go, v -> {
            try {
                toPermissionSetting();
                hideSnackBar();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    private void toPermissionSetting() throws NoSuchFieldException, IllegalAccessException {
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                MiuiUtils.applyMiuiPermission(this);
            } else if (RomUtils.checkIsMeizuRom()) {
                MeizuUtils.applyPermission(this);
            } else if (RomUtils.checkIsHuaweiRom()) {
                HuaweiUtils.applyPermission(this);
            } else if (RomUtils.checkIs360Rom()) {
                QikuUtils.applyPermission(this);
            } else if (RomUtils.checkIsOppoRom()) {
                OppoUtils.applyOppoPermission(this);
            } else {
                RomUtils.getAppDetailSettingIntent(this);
            }
        } else {
            if (RomUtils.checkIsMeizuRom()) {
                MeizuUtils.applyPermission(this);
            } else {
                if (RomUtils.checkIsOppoRom() || RomUtils.checkIsVivoRom()
                        || RomUtils.checkIsHuaweiRom() || RomUtils.checkIsSamsunRom()) {
                    RomUtils.getAppDetailSettingIntent(this);
                } else if (RomUtils.checkIsMiuiRom()) {
                    MiuiUtils.toPermisstionSetting(this);
                } else {
                    RomUtils.commonROMPermissionApplyInternal(this);
                }
            }
        }
    }

    @Override
    public void hidSnackBar() {
        hideSnackBar();
    }

    @Override
    public void rightWifi() {
//        isOk = true;
//        if (!isVisible) {
//            hideSnackBar();
//            dialog = AlertUtils.showLoadingDialog(this, R.layout.alert_progress);
//            dialog.show();
//            presenter.findDevice();
//            isOk = false;
//        }
        isVisible = false;
        hideSnackBar();
        dialog = AlertUtils.showLoadingDialog(this, R.layout.alert_progress);
        dialog.show();
        presenter.findDevice();
    }

    @Override
    public void showToast(int str) {
        runOnUiThread(() -> {
            hideSnackBar();
            hidDialog();
            hideSnackBar();
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void showWeb() {
        isOk = true;
//        if (dialog != null) {
//            dialog.dismiss();
//            dialog = null;
//        }
        presenter.senResetIp();
//        if (ip != null){
//            //toBindView();
//            tvWifiHint.setText(R.string.has_configured);
//            btnWebConfig.setText(R.string.next);
//        }else {
//            tvWifiHint.setText(R.string.avs_wifi_hint);
//            btnWebConfig.setText(R.string.setting_avs_wifi_connection);
//        }

//        webMain.setVisibility(View.VISIBLE);
//        webMain.bringToFront();
//        tvWifiHint.setVisibility(View.GONE);
//        btnWebConfig.setVisibility(View.GONE);
//        tvCompany.setVisibility(View.GONE);
//        pbWeb.bringToFront();
//
//        webMain.loadUrl(AVS_WIFI_URL);
//        if (ip != null && type == 0) {
//            if (isAuth){
//                //toBindView();
//                btnWebConfig.setText(R.string.next);
//            }else {
//                btnWebConfig.setText(R.string.setting_avs_wifi_connection);
//            }
//        } else {
//            webMain.setVisibility(View.VISIBLE);
//            webMain.bringToFront();
//            tvWifiHint.setVisibility(View.GONE);
//            btnWebConfig.setVisibility(View.GONE);
//            tvCompany.setVisibility(View.GONE);
//            pbWeb.bringToFront();
//
//            webMain.loadUrl(AVS_WIFI_URL);
//        }
    }

    @Override
    public void toBindView() {
        runOnUiThread(() -> {
            hidDialog();
            hideSnackBar();
            webMain.clearMatches();
            webMain.clearHistory();
            webMain.clearCache(true);
            webMain.clearFormData();
            webMain.clearSslPreferences();
            presenter.destroy();
            startActivity(new Intent(WifiConfigActivity.this, BindActivity.class)
                    .putExtra(IS_AUTH, isAuth)
                    .putExtra(IP, ip)
                    .putExtra(SSID,ssId)
                    .putExtra(PSK,psk));
            WifiConfigActivity.this.finish();
        });
    }

    @Override
    public void getAuth(boolean b) {
        isAuth = b;
    }

    @Override
    public void getIp(String ip) {
        this.ip = ip;
    }

    @Override
    public void notFound() {
        hideSnackBar();
        hidDialog();
        showSnackBar(webMain, R.string.avs_wifi_available, R.string.go, v -> {
            Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
            hidSnackBar();
        });
    }

    private void hidDialog(){
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    public void webVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hidDialog();
                hideSnackBar();
                webMain.setVisibility(View.VISIBLE);
                webMain.bringToFront();
                tvWifiHint.setVisibility(View.GONE);
                btnWebConfig.setVisibility(View.GONE);
                tvCompany.setVisibility(View.GONE);
                pbWeb.bringToFront();

                webMain.loadUrl(AVS_WIFI_URL);
            }
        });

    }

    @Override
    public void getSsId(String ssId) {
        this.ssId = ssId;
    }

    @Override
    public void getPsk(String psk) {
        this.psk = psk;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (webMain.getVisibility() == View.VISIBLE) {
            webMain.setVisibility(View.GONE);
            tvWifiHint.setVisibility(View.VISIBLE);
            btnWebConfig.setVisibility(View.VISIBLE);
            tvCompany.setVisibility(View.VISIBLE);
            tvWifiHint.bringToFront();
            btnWebConfig.bringToFront();
            tvCompany.bringToFront();
            return false;
        } else if (snackbar != null && snackbar.isShown()) {
            hideSnackBar();
            return false;
        } else {
            return onExitActivity(keyCode, event);
        }

    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        isVisible = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (isVisible){
            //isVisible = false;
            presenter.checkState();
        }
        super.onResume();
    }
}
