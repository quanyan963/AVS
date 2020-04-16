package com.txtled.avs.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.txtled.avs.R;
import com.txtled.avs.avs.AVSFragment;
import com.txtled.avs.base.MvpBaseActivity;
import com.txtled.avs.main.mvp.MainContract;
import com.txtled.avs.main.mvp.MainPresenter;
import com.txtled.avs.utils.HuaweiUtils;
import com.txtled.avs.utils.MeizuUtils;
import com.txtled.avs.utils.MiuiUtils;
import com.txtled.avs.utils.OppoUtils;
import com.txtled.avs.utils.QikuUtils;
import com.txtled.avs.utils.RomUtils;
import com.txtled.avs.utils.Utils;
import com.txtled.avs.web.WebViewActivity;
import com.txtled.avs.wwa.WWAFragment;

import butterknife.BindView;

import static com.txtled.avs.utils.Constants.AVS_WIFI_URL;
import static com.txtled.avs.utils.Constants.BUNDLE_KEY_EXCEPTION;
import static com.txtled.avs.utils.Constants.RB_ID;
import static com.txtled.avs.utils.Constants.REQUEST_CODE_LOCATION_SETTINGS;
import static com.txtled.avs.utils.Constants.REQUEST_CODE_WIFI_SETTINGS;
import static com.txtled.avs.utils.Constants.WEB_URL;

public class MainActivity extends MvpBaseActivity<MainPresenter> implements MainContract.View
        , RadioGroup.OnCheckedChangeListener, View.OnClickListener {


    @BindView(R.id.fl_main_fragment)
    FrameLayout flMainFragment;
//    @BindView(R.id.rb_main_avs)
//    RadioButton rbMainAvs;
//    @BindView(R.id.rb_main_wwa)
//    RadioButton rbMainWwa;
//    @BindView(R.id.rg_main_bottom)
//    RadioGroup rgMainBottom;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.dl_main_right)
    DrawerLayout dlMainRight;
    private Fragment mCurrentFragment;
    private AVSFragment mAVSFragment;
    private WWAFragment mWWAFragment;
    private boolean wwaSwitch;
    private boolean avsSwitch;

    @Override
    public void init() {
//        Intent intent = getIntent();
//        int checkId = intent.getIntExtra(RB_ID, R.id.rb_main_avs);
//
//        try{
//            String data = intent.getDataString(); // 接收到网页传过来的数据：scheme://data/xxx
//            String[] split = data.split("data/");
//            String param = split[1]; // 获取到网页传过来的参数
//        }catch (Exception e){
//
//        }

        initToolbar();
        isShowRightImg(true);
        setRightImg(null,new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlMainRight.openDrawer(navView);
            }
        });

        try {
            View view = navView.getHeaderView(0);
            Menu menu = navView.getMenu();
            MenuItem item = menu.getItem(0);
            item.setTitle(String.format(getString(R.string.version_s),getPackageManager()
                    .getPackageInfo(getPackageName(),0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mCurrentFragment = new AVSFragment();
//        rgMainBottom.setOnCheckedChangeListener(this);
//        rgMainBottom.check(checkId);
//        setNavigationIcon(true);
//        isShowRightImg(true);

        //setRightImg(getDrawable(R.drawable.reset),this);
        toAVS();
    }

    public void setAvsSwitch(boolean b){
        avsSwitch = b;
        if (avsSwitch){
            setNavigationIcon(true);
        }else {
            setNavigationIcon(false);
        }
    }

    @Override
    public int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void setInject() {
        getActivityComponent().inject(this);
    }

    //打开wifi设置界面
    @Override
    public void openWifi() {
        hideSnackBar();
        showSnackBar(flMainFragment, R.string.no_conn_wifi, R.string.go, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
                hideSnackBar();
            }
        });
    }

    //跳转地理位置
    @Override
    public void openLocation() {
        hideSnackBar();
        showSnackBar(flMainFragment, R.string.no_open_location, R.string.go, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
                hideSnackBar();
            }
        });
    }

    @Override
    public void toWebView() {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WEB_URL,AVS_WIFI_URL);
        startActivity(intent);
    }

    //跳转wifi设置
    @Override
    public void networkNoteMatch() {
        hideSnackBar();
        showSnackBar(flMainFragment, R.string.wifi_no_match, R.string.go, view -> {
            Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
            hideSnackBar();
        });
    }

    @Override
    public void toAVS() {
        if (mAVSFragment == null) {
            mAVSFragment = new AVSFragment();
        }
        switchContent(mCurrentFragment, mAVSFragment);
        tvTitle.setText(R.string.avs);
        //setTitle(R.string.avs);
        //setRightImg(getDrawable(R.drawable.avs_change), this);
//        rbMainAvs.setCompoundDrawablesWithIntrinsicBounds(null, Utils.changeSVGColor(
//                R.drawable.avs, R.color.colorPrimaryDark, this), null, null);
//        rbMainWwa.setCompoundDrawablesWithIntrinsicBounds(null, Utils.changeSVGColor(
//                R.drawable.wwa, R.color.black, this), null, null);
    }

    @Override
    public void toWWA() {
        if (mWWAFragment == null){
            mWWAFragment = new WWAFragment();
        }
        switchContent(mCurrentFragment, mWWAFragment);
        setTitle(R.string.wwa);
        setRightImg(getDrawable(R.drawable.reset), this);
//        rbMainWwa.setCompoundDrawablesWithIntrinsicBounds(null, Utils.changeSVGColor(
//                R.drawable.wwa, R.color.colorPrimaryDark, this), null, null);
//        rbMainAvs.setCompoundDrawablesWithIntrinsicBounds(null, Utils.changeSVGColor(
//                R.drawable.avs, R.color.black, this), null, null);
    }

    @Override
    public void showPermissionHint() {
        hideSnackBar();
        showSnackBar(flMainFragment, R.string.permission_hint, R.string.go, v -> {
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

    private void toPermissionSetting() throws NoSuchFieldException, IllegalAccessException{
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

    private void switchContent(Fragment from, Fragment to) {
        if (mCurrentFragment != to) {
            mCurrentFragment = to;

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            if (!to.isAdded()) {
                // 隐藏当前的fragment，add下一个到Activity中
                transaction.hide(from).add(R.id.fl_main_fragment, to).commit();
            } else {
                // 隐藏当前的fragment，显示下一个
                transaction.hide(from).show(to).commit();
            }
        }
    }

    public void getWifiSSID(){
        presenter.getWifiSSid(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            presenter.getWifiSSid(this);
        } else if (requestCode == REQUEST_CODE_WIFI_SETTINGS) {
            presenter.getWifiSSid(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_right:
                if (mCurrentFragment instanceof WWAFragment){
                    wwaSwitch = wwaSwitch == false ? true : false;
                    ((WWAFragment)mCurrentFragment).changeResetView(wwaSwitch);
                }else {
                    avsSwitch = avsSwitch == false ? true : false;
                    ((AVSFragment)mCurrentFragment).changeResetView(avsSwitch);
                }

                break;
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            Exception exception = (Exception) args.getSerializable(BUNDLE_KEY_EXCEPTION);
            String message = exception.getMessage();

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dismiss();
                        }
                    })
                    .create();
        }
    }

    //底部导航栏监听
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        presenter.switchFragment(i);
    }

    @Override
    public void onBackPressed() {
        if (snackbar != null && snackbar.isShown()){
            hideSnackBar();
        }else if (avsSwitch == true){
            ((AVSFragment)mCurrentFragment).changeResetView(false);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (snackbar != null && snackbar.isShown()){
            hideSnackBar();
            return false;
        }else if (avsSwitch == true){
            ((AVSFragment)mCurrentFragment).changeResetView(false);
            return false;
        }else {
            return onExitActivity(keyCode, event);
        }
    }
}
