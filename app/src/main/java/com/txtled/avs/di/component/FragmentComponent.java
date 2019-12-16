package com.txtled.avs.di.component;

import android.app.Activity;


import com.txtled.avs.avs.AVSFragment;
import com.txtled.avs.di.module.FragmentModule;
import com.txtled.avs.di.scope.FragmentScope;
import com.txtled.avs.wwa.WWAFragment;

import dagger.Component;

/**
 * Created by KomoriWu
 * on 2017-09-01.
 */

@FragmentScope
@Component(dependencies = AppComponent.class, modules = FragmentModule.class)
public interface FragmentComponent {
    Activity getActivity();

    void inject(AVSFragment avsFragment);
    void inject(WWAFragment wwaFragment);
}
