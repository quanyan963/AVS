package com.txtled.avs.wwa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txtled.avs.R;
import com.txtled.avs.base.MvpBaseFragment;
import com.txtled.avs.wwa.mvp.WWAContract;
import com.txtled.avs.wwa.mvp.WWAPresenter;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public class WWAFragment extends MvpBaseFragment<WWAPresenter> implements WWAContract.View {
    @Override
    protected void initInject() {
        getFragmentComponent().inject(this);
    }

    @Override
    public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wwa, null);
    }

    @Override
    public void init() {

    }
}
