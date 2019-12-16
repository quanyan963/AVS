package com.txtled.avs.web;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txtled.avs.R;
import com.txtled.avs.base.MvpBaseActivity;
import com.txtled.avs.web.mvp.WebViewComtract;
import com.txtled.avs.web.mvp.WebViewPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.txtled.avs.utils.Constants.AVS_WIFI_URL;

/**
 * Created by Mr.Quan on 2019/12/9.
 */
public class WebViewActivity extends MvpBaseActivity<WebViewPresenter> implements WebViewComtract.View
        , View.OnClickListener {
    @BindView(R.id.web_main)
    WebView webMain;

    @Override
    public void setInject() {
        getActivityComponent().inject(this);
    }

    @Override
    public void init() {
        LinearLayout back = new LinearLayout(this);
        AbsoluteLayout.LayoutParams mBackLayoutParams = new AbsoluteLayout.LayoutParams(
                (int)getResources().getDimension(R.dimen.dp_60_x),
                (int)getResources().getDimension(R.dimen.dp_60_y), 0, 0);
        back.setOnClickListener(this);
        webMain.addView(back,mBackLayoutParams);

        TextView tvWebBack = new TextView(this);
        tvWebBack.setBackground(getDrawable(R.drawable.blue_back));
        AbsoluteLayout.LayoutParams mTitleBarLayoutParams = new AbsoluteLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, (int)getResources().getDimension(R.dimen.dp_12_x)
                , (int)getResources().getDimension(R.dimen.dp_10_y));

        webMain.addView(tvWebBack,mTitleBarLayoutParams);
        WebSettings settings = webMain.getSettings();
        // 此方法需要启用JavaScript
        settings.setJavaScriptEnabled(true);
        webMain.loadUrl(AVS_WIFI_URL);

    }

    @Override
    public int getLayout() {
        return R.layout.activity_web;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }
}
