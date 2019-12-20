package com.txtled.avs.web;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txtled.avs.R;
import com.txtled.avs.base.MvpBaseActivity;
import com.txtled.avs.web.mvp.WebViewComtract;
import com.txtled.avs.web.mvp.WebViewPresenter;

import butterknife.BindView;

import static com.txtled.avs.utils.Constants.AVS_WIFI_URL;
import static com.txtled.avs.utils.Constants.WEB_URL;

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
        Intent intent = getIntent();
        String url = intent.getStringExtra(WEB_URL);
        webMain.clearMatches();
        webMain.clearHistory();
        webMain.clearCache(true);
        webMain.clearFormData();
        webMain.clearSslPreferences();
        if (url.equals(AVS_WIFI_URL)){
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
        }

        WebSettings settings = webMain.getSettings();
        // 此方法需要启用JavaScript
        settings.setJavaScriptEnabled(true);
        webMain.setWebViewClient(new WebViewClient(){

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
        webMain.loadUrl(url);

    }

    @Override
    public int getLayout() {
        return R.layout.activity_web;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        webMain.clearMatches();
        webMain.clearHistory();
        webMain.clearCache(true);
        webMain.clearFormData();
        webMain.clearSslPreferences();
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }
}
