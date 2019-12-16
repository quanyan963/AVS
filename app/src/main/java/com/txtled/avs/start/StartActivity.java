package com.txtled.avs.start;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.core.app.ActivityOptionsCompat;

import com.txtled.avs.R;
import com.txtled.avs.base.MvpBaseActivity;
import com.txtled.avs.main.MainActivity;
import com.txtled.avs.start.mvp.StartContract;
import com.txtled.avs.start.mvp.StartPresenter;
import com.txtled.avs.utils.Utils;

import butterknife.BindView;

import static com.txtled.avs.utils.Constants.RB_ID;

/**
 * Created by Mr.Quan on 2019/12/10.
 */
public class StartActivity extends MvpBaseActivity<StartPresenter> implements StartContract.View
        , RadioGroup.OnCheckedChangeListener{
    @BindView(R.id.rb_main_avs)
    RadioButton rbMainAvs;
    @BindView(R.id.rb_main_wwa)
    RadioButton rbMainWwa;
    @BindView(R.id.rg_start_check)
    RadioGroup rgStartCheck;
    private boolean isReturn;

    @Override
    public void setInject() {
        getActivityComponent().inject(this);
    }

    @Override
    public void init() {
        rgStartCheck.setOnCheckedChangeListener(this);
    }

    @Override
    public int getLayout() {
        return R.layout.avtivity_start;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if (isReturn) return;
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra(RB_ID,i);
        //Bundle bundle;
        switch (i){
            case R.id.rb_main_avs:
                rbMainAvs.setCompoundDrawablesWithIntrinsicBounds(Utils.changeSVGColor(
                        R.drawable.avs, R.color.colorAccent, this),
                        null, null, null);
//                bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this,rbMainAvs
//                        ,getString(R.string.share_avs)).toBundle();
                startActivity(intent);
                isReturn = true;
                rgStartCheck.clearCheck();
                rbMainAvs.setCompoundDrawablesWithIntrinsicBounds(Utils.changeSVGColor(
                        R.drawable.avs, R.color.black, this), null, null, null);
                break;
            case R.id.rb_main_wwa:
                rbMainWwa.setCompoundDrawablesWithIntrinsicBounds(Utils.changeSVGColor(
                        R.drawable.wwa, R.color.colorAccent, this),
                        null, null, null);
//                bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this,rbMainWwa
//                        ,getString(R.string.share_wwa)).toBundle();
                startActivity(intent);
                isReturn = true;
                rgStartCheck.clearCheck();
                rbMainWwa.setCompoundDrawablesWithIntrinsicBounds(Utils.changeSVGColor(
                        R.drawable.wwa, R.color.black, this), null, null, null);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isReturn = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return onExitActivity(keyCode,event);
    }
}
