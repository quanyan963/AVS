package com.txtled.avs.base;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.txtled.avs.R;
import com.txtled.avs.application.MyApplication;

import butterknife.ButterKnife;


public abstract class BaseActivity extends AppCompatActivity {
    public static final String TAG = BaseActivity.class.getSimpleName();
    public TextView tvTitle;
    public boolean isBack = true;
    private long mExitTime;
    private MyApplication mApplication;
    Toolbar toolbar;
    public Snackbar snackbar;
    private ImageView ivRight;

    public abstract void init();

    public abstract int getLayout();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        ButterKnife.bind(this);

        mApplication = MyApplication.getInstance();
        addActivity();
        onCreateView();
        init();
    }

    public void onCreateView() {

    }


    public void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            tvTitle = (TextView) findViewById(R.id.tv_title);
            ivRight = (ImageView) findViewById(R.id.iv_right);
            setSupportActionBar(toolbar);
            setTitle("");

            toolbar.setOnMenuItemClickListener(onMenuItemClick);
            toolbar.setNavigationOnClickListener(v -> {
                if (isBack) {
                    onBackPressed();
                } else {
                    onLeftClick();
                }

            });
        }
    }

    public void onLeftClick() {
    }

    public void setNavigationIcon(boolean isBack) {
        this.isBack = isBack;
        if (isBack) {
            toolbar.setNavigationIcon(R.drawable.black_back);
        } else {
            toolbar.setNavigationIcon(R.drawable.reset);
        }

    }

    public void removeNavigationIcon() {
        toolbar.setNavigationIcon(null);
    }

    public void setRightImg(Drawable drawable, View.OnClickListener listener) {
        ivRight.setImageDrawable(drawable);
        ivRight.setOnClickListener(listener);
    }

    public void isShowRightImg(boolean isShow){
        if (isShow){
            ivRight.setVisibility(View.VISIBLE);
        }else {
            ivRight.setVisibility(View.GONE);
        }
    }

    public Toolbar.OnMenuItemClickListener onMenuItemClick = menuItem -> {
        OnMenuItemClick(menuItem.getItemId());
        return true;
    };

    public void OnMenuItemClick(int itemId) {

    }

    public boolean onExitActivity(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, R.string.exit_program_hint,
                        Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                removeAllActivity();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void addActivity() {
        mApplication.addActivity(this);
    }


    public void removeAllActivity() {
        mApplication.removeAllActivity();
    }

    public void showSnackBar(View view, int str) {
        if (snackbar == null) {
            snackbar = Snackbar.make(view, str, Snackbar.LENGTH_INDEFINITE);
            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.black));
        }
        snackbar.show();
    }

    public void showSnackBar(View view, String str) {
        if (snackbar == null) {
            snackbar = Snackbar.make(view, str, Snackbar.LENGTH_INDEFINITE);
            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.black));
        }
        snackbar.show();
    }

    public void showSnackBar(View view, int str, int btnStr, View.OnClickListener listener) {
        if (snackbar == null) {
            snackbar = Snackbar.make(view, str, Snackbar.LENGTH_INDEFINITE).setAction(btnStr, listener);
            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.black));
        }
        snackbar.show();
    }

    public void hideSnackBar() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
            snackbar = null;
        }
    }
}
