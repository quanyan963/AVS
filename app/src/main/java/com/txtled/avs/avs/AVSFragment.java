package com.txtled.avs.avs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.txtled.avs.R;
import com.txtled.avs.avs.listener.OnSearchListener;
import com.txtled.avs.avs.mvp.AVSContract;
import com.txtled.avs.avs.mvp.AVSPresenter;
import com.txtled.avs.base.MvpBaseFragment;
import com.txtled.avs.bean.DeviceHostInfo;
import com.txtled.avs.main.MainActivity;

import java.util.ArrayList;

import butterknife.BindView;

import static com.txtled.avs.utils.Constants.BUNDLE_KEY_EXCEPTION;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public class AVSFragment extends MvpBaseFragment<AVSPresenter> implements AVSContract.View
        , View.OnClickListener, AVSAdapter.OnAVSItemClickListener, SwipeRefreshLayout.OnRefreshListener
        , OnSearchListener {
    @BindView(R.id.tv_avs_wifi)
    TextView tvAvsWifi;
    @BindView(R.id.rlv_avs_list)
    RecyclerView rlvAvsList;
    @BindView(R.id.tv_avs_hint)
    TextView tvAvsHint;
    @BindView(R.id.sfl_acs_refresh)
    SwipeRefreshLayout sflAcsRefresh;
    private AVSAdapter adapter;

    @Override
    protected void initInject() {
        getFragmentComponent().inject(this);
    }

    @Override
    public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_avs, null);
    }

    @Override
    public void init() {
        //开始NSD扫描
        presenter.initAmazon(getActivity(),getContext());
        tvAvsHint.setVisibility(View.GONE);
        tvAvsWifi.setOnClickListener(this);

        rlvAvsList.setHasFixedSize(true);
        rlvAvsList.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter.setListener(this);

        sflAcsRefresh.setOnRefreshListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_avs_wifi:
                //AVS配网
                ((MainActivity) getActivity()).getWifiSSID();
                break;
        }
    }

    /****************AMZ LOGIN FUNCTION************/
    @Override
    public void showAlertDialog(Exception exception) {
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
        if (count != 0){
            tvAvsHint.setVisibility(View.GONE);
            rlvAvsList.setAdapter(adapter);
        }else {
            tvAvsHint.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void initAdapter(ArrayList<DeviceHostInfo> ipInfos) {
        adapter = new AVSAdapter(ipInfos, getContext());
        rlvAvsList.setAdapter(adapter);
    }

    @Override
    public void showSuccess() {
        ((MainActivity)getActivity()).showSnackBar(rlvAvsList, R.string.author_success
                , R.string.close, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).hideSnackBar();
            }
        });
    }

    @Override
    public void onAVSClick(int position) {
        presenter.onItemClick(position);
    }

    @Override
    public void onRefresh() {
        presenter.refresh(AVSFragment.this);
        sflAcsRefresh.postDelayed(new Runnable() {
            @Override
            public void run() {
                sflAcsRefresh.setRefreshing(false);
            }
        },2000);
    }



    @Override
    public void onError() {

    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }
}
