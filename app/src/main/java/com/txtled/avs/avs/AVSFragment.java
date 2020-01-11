package com.txtled.avs.avs;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.txtled.avs.R;
import com.txtled.avs.avs.mvp.AVSContract;
import com.txtled.avs.avs.mvp.AVSPresenter;
import com.txtled.avs.base.MvpBaseFragment;
import com.txtled.avs.bean.DeviceHostInfo;
import com.txtled.avs.main.MainActivity;
import com.txtled.avs.utils.AlertUtils;
import com.txtled.avs.web.WebViewActivity;

import java.util.ArrayList;

import butterknife.BindView;

import static com.txtled.avs.utils.Constants.BIND_URL;
import static com.txtled.avs.utils.Constants.BUNDLE_KEY_EXCEPTION;
import static com.txtled.avs.utils.Constants.REQUEST_CODE_INTERNET_SETTINGS;
import static com.txtled.avs.utils.Constants.REQUEST_CODE_WIFI_SETTINGS;
import static com.txtled.avs.utils.Constants.WEB_URL;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public class AVSFragment extends MvpBaseFragment<AVSPresenter> implements AVSContract.View
        , View.OnClickListener, AVSAdapter.OnAVSItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.tv_avs_wifi)
    TextView tvAvsWifi;
    @BindView(R.id.rlv_avs_list)
    RecyclerView rlvAvsList;
    @BindView(R.id.tv_avs_hint)
    TextView tvAvsHint;
    @BindView(R.id.sfl_acs_refresh)
    SwipeRefreshLayout sflAcsRefresh;
    @BindView(R.id.tv_avs_bind)
    TextView tvAvsBind;
    //    @BindView(R.id.pb_avs_loading)
//    ProgressBar pbAvsLoading;
    @BindView(R.id.tv_avs_code)
    TextView tvAvsCode;
    @BindView(R.id.rl_avs_bind)
    RelativeLayout rlAvsBind;
    @BindView(R.id.tv_avs_code_hint)
    TextView tvAvsCodeHint;
    @BindView(R.id.rl_avs_wifi)
    RelativeLayout rlAvsWifi;
    @BindView(R.id.rl_avs_search)
    RelativeLayout rlAvsSearch;
    @BindView(R.id.iv_avs_refresh)
    ImageView ivAvsRefresh;
    private AVSAdapter adapter;
    private AlertDialog dialog;

    @Override
    protected void initInject() {
        getFragmentComponent().inject(this);
    }

    @Override
    public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_avs, container, false);
    }

    @Override
    public void init() {
        //开始NSD扫描
        presenter.initAmazon(getActivity(), getContext());
        rlAvsBind.setVisibility(View.GONE);
        //pbAvsLoading.setVisibility(View.GONE);
        tvAvsWifi.setOnClickListener(this);
        tvAvsBind.setOnClickListener(this);
        ivAvsRefresh.setOnClickListener(this);
        rlvAvsList.setHasFixedSize(true);
        rlvAvsList.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter.setListener(this);

        sflAcsRefresh.setOnRefreshListener(this);
        dialog = AlertUtils.showLoadingDialog(getContext(), R.layout.alert_progress);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_avs_wifi:
                //AVS配网
                ((MainActivity) getActivity()).getWifiSSID();
                break;
            case R.id.tv_avs_bind:
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(WEB_URL, BIND_URL);
                startActivityForResult(intent, REQUEST_CODE_INTERNET_SETTINGS);
                break;
            case R.id.iv_avs_refresh:
                sflAcsRefresh.setRefreshing(true);
                presenter.refresh();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_INTERNET_SETTINGS) {
            bindSuccess();
        }
    }

    /****************AMZ LOGIN FUNCTION************/
    @Override
    public void showAlertDialog(Exception exception) {
        hidProgress();
        //pbAvsLoading.setVisibility(View.GONE);
        exception.printStackTrace();
        MainActivity.ErrorDialogFragment dialogFragment = new MainActivity.ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUNDLE_KEY_EXCEPTION, exception);
        dialogFragment.setArguments(args);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        dialogFragment.show(fm, "error_dialog");
    }

    @Override
    public void setAdapter(int count) {
        if (count != 0) {
            tvAvsHint.setVisibility(View.GONE);
            rlvAvsList.setAdapter(adapter);
        } else {
            tvAvsHint.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void initAdapter(ArrayList<DeviceHostInfo> ipInfos) {
        adapter = new AVSAdapter(ipInfos, getContext());
        rlvAvsList.setAdapter(adapter);
    }

//    @Override
//    public void showSuccess() {
//        ((MainActivity) getActivity()).showSnackBar(rlvAvsList, R.string.author_success
//                , R.string.close, v -> ((MainActivity) getActivity()).hideSnackBar());
//    }

    @Override
    public void bindDevice(String mCode) {
        getActivity().runOnUiThread(() -> {
            rlAvsBind.setVisibility(View.VISIBLE);
            tvAvsCode.setText(mCode);
            hidProgress();
            //pbAvsLoading.setVisibility(View.GONE);
            sflAcsRefresh.setVisibility(View.GONE);
        });
    }

    @Override
    public void hidProgress() {
        if (dialog != null && dialog.isShowing()) {
            getActivity().runOnUiThread(() -> dialog.dismiss());
        }
        //pbAvsLoading.setVisibility(View.GONE);
    }

    @Override
    public void showNetWorkError(int str) {
        hidSnackBar();
        ((MainActivity) getActivity()).showSnackBar(sflAcsRefresh, str, R.string.go, v -> {
            Intent locationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivityForResult(locationIntent, REQUEST_CODE_WIFI_SETTINGS);
            hidSnackBar();
        });
    }

    @Override
    public void closeRefresh(int i) {
        sflAcsRefresh.postDelayed(() -> sflAcsRefresh.setRefreshing(false), i);
    }

    @Override
    public void showLoadingView() {
        getActivity().runOnUiThread(() -> dialog.show());

    }

    @Override
    public void showToast(int msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void hidSnackBar() {
        if (((MainActivity) getActivity()).snackbar != null &&
                ((MainActivity) getActivity()).snackbar.isShown()) {
            ((MainActivity) getActivity()).hideSnackBar();
        }
    }

    public void bindSuccess() {
        tvAvsCode.setVisibility(View.GONE);
        tvAvsBind.setVisibility(View.GONE);
        tvAvsCodeHint.setText(R.string.avs_success);
    }

    @Override
    public void onAVSClick(int position) {
        dialog.show();
        presenter.onItemClick(position);

        //pbAvsLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        presenter.refresh();

    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
        hidProgress();
    }

    @Override
    public void onDestroy() {
        presenter.destroy();
        dialog = null;
        super.onDestroy();
    }

    public void changeResetView(boolean avsSwitch) {
        presenter.setSwitch(avsSwitch);
        if (avsSwitch) {
            rlAvsWifi.setVisibility(View.GONE);
            rlAvsSearch.setVisibility(View.VISIBLE);
        } else {
            rlAvsSearch.setVisibility(View.GONE);
            rlAvsWifi.setVisibility(View.VISIBLE);
        }
    }
}
