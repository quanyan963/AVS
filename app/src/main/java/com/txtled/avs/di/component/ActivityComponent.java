package com.txtled.avs.di.component;

import android.app.Activity;

import com.txtled.avs.cognito.AuthenticatorActivity;
import com.txtled.avs.di.module.ActivityModule;
import com.txtled.avs.di.scope.ActivityScope;
import com.txtled.avs.main.MainActivity;
import com.txtled.avs.start.StartActivity;
import com.txtled.avs.web.WebViewActivity;

import dagger.Component;

/**
 * Created by KomoriWu
 * on 2017-09-01.
 */

@ActivityScope
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {
    Activity getActivity();

    void inject(StartActivity startActivity);

    void inject(MainActivity mainActivity);

    void inject(WebViewActivity webViewActivity);

    void inject(AuthenticatorActivity authenticatorActivity);
}
