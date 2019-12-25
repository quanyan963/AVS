package com.txtled.avs.start;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.txtled.avs.R;
import com.txtled.avs.base.MvpBaseActivity;
import com.txtled.avs.main.MainActivity;
import com.txtled.avs.start.mvp.StartContract;
import com.txtled.avs.start.mvp.StartPresenter;
import com.txtled.avs.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.txtled.avs.utils.Constants.RB_ID;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public class StartActivity extends MvpBaseActivity<StartPresenter> implements StartContract.View, View.OnClickListener {
    @BindView(R.id.rb_main_avs)
    RadioButton rbMainAvs;
    @BindView(R.id.rb_main_wwa)
    RadioButton rbMainWwa;
    @BindView(R.id.rg_start_check)
    RadioGroup rgStartCheck;
    @BindView(R.id.rb_main_avs_img)
    RadioButton rbMainAvsImg;
    @BindView(R.id.rb_main_wwa_img)
    RadioButton rbMainWwaImg;

    @Override
    public void setInject() {
        getActivityComponent().inject(this);
    }

    @Override
    public void init() {
        //rgStartCheck.setOnCheckedChangeListener(this);
        rbMainAvs.setOnClickListener(this);
        rbMainWwa.setOnClickListener(this);
        rbMainAvsImg.setOnClickListener(this);
        rbMainWwaImg.setOnClickListener(this);
    }

    @Override
    public int getLayout() {
        return R.layout.avtivity_start;
    }

    @Override
    protected void onResume() {
        super.onResume();
        rbMainAvs.setCompoundDrawablesWithIntrinsicBounds(Utils.changeSVGColor(
                R.drawable.avs, R.color.black, this),
                null, null, null);
        rbMainAvs.setTextColor(getResources().getColor(R.color.black));
        rbMainWwa.setCompoundDrawablesWithIntrinsicBounds(Utils.changeSVGColor(
                R.drawable.wwa, R.color.black, this),
                null, null, null);
        rbMainWwa.setTextColor(getResources().getColor(R.color.black));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return onExitActivity(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        //Bundle bundle;
        switch (v.getId()) {
            case R.id.rb_main_avs_img:
            case R.id.rb_main_avs:
                intent.putExtra(RB_ID, R.id.rb_main_avs);
                rbMainAvs.setCompoundDrawablesWithIntrinsicBounds(Utils.changeSVGColor(
                        R.drawable.avs, R.color.colorAccent, this),
                        null, null, null);
                rbMainAvs.setTextColor(getResources().getColor(R.color.colorAccent));
//                bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this,rbMainAvs
//                        ,getString(R.string.share_avs)).toBundle();
                startActivity(intent);
                break;
            case R.id.rb_main_wwa_img:
            case R.id.rb_main_wwa:
                intent.putExtra(RB_ID, R.id.rb_main_wwa);
                rbMainWwa.setCompoundDrawablesWithIntrinsicBounds(Utils.changeSVGColor(
                        R.drawable.wwa, R.color.colorAccent, this),
                        null, null, null);
                rbMainWwa.setTextColor(getResources().getColor(R.color.colorAccent));
//                bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this,rbMainWwa
//                        ,getString(R.string.share_wwa)).toBundle();
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
