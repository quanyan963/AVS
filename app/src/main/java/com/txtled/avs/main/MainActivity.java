package com.txtled.avs.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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
        , RadioGroup.OnCheckedChangeListener {


    @BindView(R.id.fl_main_fragment)
    FrameLayout flMainFragment;
    @BindView(R.id.rb_main_avs)
    RadioButton rbMainAvs;
    @BindView(R.id.rb_main_wwa)
    RadioButton rbMainWwa;
    @BindView(R.id.rg_main_bottom)
    RadioGroup rgMainBottom;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.dl_main_right)
    DrawerLayout dlMainRight;
    private Fragment mCurrentFragment;
    private AVSFragment mAVSFragment;
    private WWAFragment mWWAFragment;
    private boolean navigationSwitch;

    @Override
    public void init() {
        Intent intent = getIntent();
        int checkId = intent.getIntExtra(RB_ID, R.id.rb_main_avs);
        initToolbar();
        mCurrentFragment = new AVSFragment();
        rgMainBottom.setOnCheckedChangeListener(this);
        rgMainBottom.check(checkId);

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
        showSnackBar(rgMainBottom, R.string.no_conn_wifi, R.string.go, new View.OnClickListener() {
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
        showSnackBar(rgMainBottom, R.string.no_open_location, R.string.go, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_SETTINGS);
                hideSnackBar();
            }
        });
    }

    @Override
    public void onLeftClick() {
        navigationSwitch = navigationSwitch == false ? true : false;
        ((WWAFragment)mCurrentFragment).changeResetView(navigationSwitch);

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
        showSnackBar(rgMainBottom, R.string.wifi_no_match, R.string.go, view -> {
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
        setTitle(R.string.avs);
        removeNavigationIcon();
        rbMainAvs.setCompoundDrawablesWithIntrinsicBounds(null, Utils.changeSVGColor(
                R.drawable.avs, R.color.colorPrimaryDark, this), null, null);
        rbMainWwa.setCompoundDrawablesWithIntrinsicBounds(null, Utils.changeSVGColor(
                R.drawable.wwa, R.color.black, this), null, null);
    }

    @Override
    public void toWWA() {
        if (mWWAFragment == null){
            mWWAFragment = new WWAFragment();
        }
        switchContent(mCurrentFragment, mWWAFragment);
        setTitle(R.string.wwa);
        if (presenter.isConfigured()){
            setNavigationIcon(false);
        }
        rbMainWwa.setCompoundDrawablesWithIntrinsicBounds(null, Utils.changeSVGColor(
                R.drawable.wwa, R.color.colorPrimaryDark, this), null, null);
        rbMainAvs.setCompoundDrawablesWithIntrinsicBounds(null, Utils.changeSVGColor(
                R.drawable.avs, R.color.black, this), null, null);
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
        if (snackbar.isShown()){
            hideSnackBar();
        }else {
            super.onBackPressed();
        }
    }
}
